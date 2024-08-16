package org.f17coders.befreshbatch.global.config.batch;

import jakarta.persistence.EntityManagerFactory;
import java.util.Arrays;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.f17coders.befreshbatch.module.domain.food.Food;
import org.f17coders.befreshbatch.module.domain.food.Freshness;
import org.f17coders.befreshbatch.module.domain.notification.Notification;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class FoodExpireBatchConfig {

    public static final String JOB_NAME = "FoodExpireBatch";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory emf;
    private final NotiSendItemWriter notiSendItemWriter;
    private final DataSource dataSource;

    private final int chunkSize = 1000; // TODO : Chunk Size 고민 필요

    @Bean
    public Job processExpiredFoodJob() {
        return new JobBuilder("processExpiredFoodJob", jobRepository)
            .start(manageExpiredFoodStep())
            .next(sendDangerNotificationStep())
            .build();
    }

    @Bean
    public Step manageExpiredFoodStep() {
        return new StepBuilder("manageExpiredFoodStep", jobRepository)
            .<Food, Food>chunk(chunkSize, transactionManager)
            .reader(expiredFoodReader())
            .processor(expiredFoodProcessor())
            .writer(expiredFoodWriter())   // TODO : Bulk Insert 고려 필요
            .build();
    }

    @Bean
    public JpaPagingItemReader<Food> expiredFoodReader() {
        return new JpaPagingItemReaderBuilder<Food>()
            .name(BEAN_PREFIX + "Reader")
            .entityManagerFactory(emf)
            .pageSize(chunkSize)
            .queryString(
                "SELECT f FROM Food f " +
                    "WHERE f.expirationDate < CURRENT_DATE "
                    + "AND f.freshness != org.f17coders.befreshbatch.module.domain.food.Freshness.BAD"
            )
            .build();
    }

    @Bean
    public ItemProcessor<Food, Food> expiredFoodProcessor() {
        return food -> {
            food.setFreshness(Freshness.BAD);
            return food;
        };
    }

    @Bean
    public JdbcBatchItemWriter<Food> expiredFoodWriter() {
        return new JdbcBatchItemWriterBuilder<Food>()
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql("UPDATE food SET freshness = 'BAD' WHERE food_id = :id")
            .dataSource(dataSource)
            .build();
    }

    @Bean
    public Step sendDangerNotificationStep() {
        return new StepBuilder("sendDangerNotificationStep", jobRepository)
            .<Food, Notification>chunk(chunkSize, transactionManager)
            .reader(notiReader())
            .processor(notiProcessor())
            .writer(notiCompositeItemWriter())
            .listener(new StepTimeListener())  // 시간 측정 Listener 추가
            .faultTolerant()
            .retryLimit(5)
            .retry(InternalServerError.class)
            .taskExecutor(simpleTaskExecutor())
//            .taskExecutor(threadPoolTaskExecutor())
            .build();
    }

    @Bean
    public JpaPagingItemReader<Food> notiReader() {
        return new JpaPagingItemReaderBuilder<Food>()
            .name(BEAN_PREFIX + "NotiReader")
            .entityManagerFactory(emf)
            .pageSize(chunkSize)
            .queryString(
                "SELECT f FROM Food f " +
                    "JOIN FETCH f.refrigerator r " +
                    "JOIN FETCH r.member m " +
                    "JOIN FETCH m.memberTokenSet mt " +
                    "WHERE f.freshness = org.f17coders.befreshbatch.module.domain.food.Freshness.BAD")
            .build();
    }

    @Bean
    public ItemProcessor<Food, Notification> notiProcessor() {
        return food -> {
            String title = "[" + food.getName() + "]" + " 위험 상태입니다!";
            String body = "주의해서 먹으세요!";
            return Notification.createNotification("danger", title, body,
                food.getRefrigerator());
        };
    }

    @Bean
    public CompositeItemWriter<Notification> notiCompositeItemWriter() {
        final CompositeItemWriter<Notification> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(
            Arrays.asList(notiSendItemWriter, notiJDBCBatchWriter())); // Writer 등록
        return compositeItemWriter;
    }

    @Bean
    public JdbcBatchItemWriter<Notification> notiJDBCBatchWriter() {
        return new JdbcBatchItemWriterBuilder<Notification>()
            .sql(
                "insert into notification (category, title, message, refrigerator_id) values (:category, :title, :message, :refrigerator.id)")
            .dataSource(dataSource)
            .beanMapped()
            .build();
    }

    @Bean
    public TaskExecutor simpleTaskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setVirtualThreads(true);
        return taskExecutor;
    }

    @Bean
    public TaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(1000); // 동시 실행을 제어하기 위한 설정
        taskExecutor.setMaxPoolSize(Integer.MAX_VALUE);
        taskExecutor.setQueueCapacity(25);
        taskExecutor.initialize();
        return taskExecutor;
    }
}