package org.f17coders.befreshbatch.global.config.batch;

import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.f17coders.befreshbatch.module.domain.food.Food;
import org.f17coders.befreshbatch.module.domain.food.Freshness;
import org.f17coders.befreshbatch.module.domain.food.repository.FoodRepository;
import org.f17coders.befreshbatch.module.domain.notification.service.NotificationService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class FoodExpireBatchConfig {

    public static final String JOB_NAME = "FoodExpireBatch";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final FoodRepository foodRepository;
    private final NotificationService notificationService;
    private final EntityManagerFactory emf;
    private final CustomItemWriter customItemWriter;

    private final int chunkSize = 1000; // TODO : Chunk Size 고민 필요

    @Bean
    public Job processExpiredFoodJob() {
        return new JobBuilder("processExpiredFoodJob", jobRepository)
            .start(manageExpiredFoodStep())
            .next(sendExpireNotificationStep())   // TODO : Retry, 실패 시 로직 추가 구현 필요
            .build();
    }

    @Bean
    public Step manageExpiredFoodStep() {
        return new StepBuilder("manageExpiredFoodStep", jobRepository)
            .<Food, Food>chunk(chunkSize, transactionManager)
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(customItemWriter)
            .listener(promotionListener())
            .build();
    }

    @Bean
    public JpaPagingItemReader<Food> itemReader() {
        return new JpaPagingItemReaderBuilder<Food>()
            .name(BEAN_PREFIX + "Reader")
            .entityManagerFactory(emf)
            .pageSize(chunkSize)
            .queryString(
                "SELECT f FROM Food f WHERE f.expirationDate < CURRENT_DATE")  // TODO : DB INDEXING?
            .build();
    }

    @Bean
    public ItemProcessor<Food, Food> itemProcessor() {
        return food -> {
            food.setFreshness(Freshness.GOOD);
            return food;
        };
    }

    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"foods"}); // 자동으로 승격시킬 키 목록
        return listener;
    }

    @Bean
    public Step sendExpireNotificationStep() {
        return new StepBuilder("sendExpireNotificationStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                ExecutionContext jobExecutionContext = chunkContext.getStepContext()
                    .getStepExecution().getJobExecution().getExecutionContext();

                List<Long> expireFoodIdList = (List<Long>) jobExecutionContext.get("foods");

                List<Food> expireFoodList = foodRepository.findNotiFood(expireFoodIdList);
                notificationService.sendExpireNotification(expireFoodList, "danger");

                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}