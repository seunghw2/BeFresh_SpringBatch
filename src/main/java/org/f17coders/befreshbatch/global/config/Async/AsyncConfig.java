package org.f17coders.befreshbatch.global.config.Async;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 동시에 처리할 스레드 수
        executor.setMaxPoolSize(50);  // 최대 스레드 수
        executor.setQueueCapacity(Integer.MAX_VALUE); // 대기 큐 용량
        executor.initialize();
        return executor;
    }
}
