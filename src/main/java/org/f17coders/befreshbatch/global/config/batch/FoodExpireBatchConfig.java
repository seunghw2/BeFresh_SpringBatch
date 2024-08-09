package org.f17coders.befreshbatch.global.config.batch;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.f17coders.befreshbatch.module.domain.food.Food;
import org.f17coders.befreshbatch.module.domain.food.Freshness;
import org.f17coders.befreshbatch.module.domain.food.repository.FoodRepository;
import org.f17coders.befreshbatch.module.domain.notification.Notification;
import org.f17coders.befreshbatch.module.domain.notification.service.NotificationService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
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
    private final NotiItemWriter notiItemWriter;

    private final int chunkSize = 1000; // TODO : Chunk Size 고민 필요

    @Bean
    public Job processExpiredFoodJob() {
        return new JobBuilder("processExpiredFoodJob", jobRepository)
            .start(manageExpiredFoodStep())
            .next(sendDangerNotificationStep())   // TODO : Retry, 실패 시 로직 추가 구현 필요
            .build();
    }

    @Bean
    public Step manageExpiredFoodStep() {
        return new StepBuilder("manageExpiredFoodStep", jobRepository)
            .<Food, Food>chunk(chunkSize, transactionManager)
            .reader(expiredFoodReader())
            .processor(expiredFoodProcessor())
            .writer(customItemWriter)
            .build();
    }

    @Bean
    public JpaPagingItemReader<Food> expiredFoodReader() {
        return new JpaPagingItemReaderBuilder<Food>()
            .name(BEAN_PREFIX + "Reader")
            .entityManagerFactory(emf)
            .pageSize(chunkSize)
            .queryString(
                "SELECT f FROM Food f WHERE f.expirationDate < CURRENT_DATE")  // TODO : DB INDEXING?
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
    public Step sendDangerNotificationStep() {
        return new StepBuilder("sendDangerNotificationStep", jobRepository)
            .<Food, Notification>chunk(chunkSize, transactionManager)
            .reader(notiReader())
            .processor(notiProcessor())
            .writer(notiItemWriter)
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
            // TODO : DB INDEXING?
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
}