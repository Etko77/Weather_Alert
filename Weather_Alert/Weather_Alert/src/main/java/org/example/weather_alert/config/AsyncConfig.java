package org.example.weather_alert.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${async.core-pool-size:2}")
    private int corePoolSize;

    @Value("${async.max-pool-size:5}")
    private int maxPoolSize;

    @Value("${async.queue-capacity:100}")
    private int queueCapacity;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("Creating Async Task Executor with corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                corePoolSize, maxPoolSize, queueCapacity);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size: minimum number of threads to keep alive
        executor.setCorePoolSize(corePoolSize);

        // Max pool size: maximum number of threads to create
        executor.setMaxPoolSize(maxPoolSize);

        // Queue capacity: how many tasks to queue before creating new threads
        executor.setQueueCapacity(queueCapacity);

        // Thread name prefix for easier debugging
        executor.setThreadNamePrefix("GeoTag-");

        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // Maximum wait time for tasks on shutdown
        executor.setAwaitTerminationSeconds(60);

        // Initialize the executor
        executor.initialize();

        return executor;
    }
}
