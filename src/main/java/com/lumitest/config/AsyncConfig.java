package com.lumitest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "testTaskExecutor")
    public Executor testTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Số luồng chạy song song tối thiểu
        executor.setMaxPoolSize(10); // Số luồng chạy song song tối đa
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("LumiTest-");
        executor.initialize();
        return executor;
    }
}
