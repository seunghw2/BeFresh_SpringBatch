package org.f17coders.befreshbatch.global.config.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class BatchScheduleConfig {

    private final JobLauncher jobLauncher;
    private final Job processExpiredFoodJob;

    //    @Scheduled(cron = "0 0 9 * * ?") // 매일 오전 9시에 실행
    @Scheduled(cron = "0 0/1 * * * ?") // 매 1분마다 실행
    public void runExpiredFoodJob() {
        runJob(processExpiredFoodJob, "processExpiredFoodJob");
    }

    private void runJob(Job job, String jobName) {
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();
        try {
            jobLauncher.run(job, jobParameters);
            log.info("Job '{}' was successfully executed.", jobName);
        } catch (Exception e) {
            log.error("Error running job '{}'", jobName, e);
        }
    }
}