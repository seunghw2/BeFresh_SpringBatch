package org.f17coders.befreshbatch.global.config.batch;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.f17coders.befreshbatch.module.domain.food.Food;
import org.f17coders.befreshbatch.module.domain.food.repository.FoodRepository;
import org.f17coders.befreshbatch.module.domain.notification.service.NotificationService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class FoodExpireBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final FoodRepository foodRepository;
    private final NotificationService notificationService;

    @Bean
    public Job processExpiredFoodJob() {
        return new JobBuilder("processExpiredFoodJob", jobRepository)
                .start(findExpireFoodStep())
                .next(updateFoodRefreshStep())
                .next(sendExpireNotificationStep())
                .build();
    }

    @Bean
    public Step findExpireFoodStep() {
        return new StepBuilder("findExpireFoodStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("dangerFoodIdList");
                    ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

                    List<Long> dangerFoodIdList = foodRepository.findDangerFood();

                    jobExecutionContext.put("dangerFoodIdList", dangerFoodIdList);

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step updateFoodRefreshStep() {
        return new StepBuilder("updateFoodRefreshStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

                    List<Long> dangerFoodIdList = (List<Long>) jobExecutionContext.get("dangerFoodIdList");

                    List<Food> dangerFoodList = foodRepository.findUpdateFood(dangerFoodIdList);
                    for (Food food : dangerFoodList) {
                        foodRepository.save(food);
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step sendExpireNotificationStep() {
        return new StepBuilder("sendExpireNotificationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

                    List<Long> dangerFoodIdList = (List<Long>) jobExecutionContext.get("dangerFoodIdList");

                    List<Food> dangerFoodList = foodRepository.findNotiFood(dangerFoodIdList);

                    notificationService.sendExpireNotification(dangerFoodList, "danger");

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}