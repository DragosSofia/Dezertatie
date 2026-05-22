package com.example.demo.Configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
public class ExecutorConfig {

    @Value("${app.executor.core-pool-size:16}")
    private int corePoolSize;

    @Value("${app.executor.max-pool-size:64}")
    private int maxPoolSize;

    @Value("${app.executor.queue-capacity:200}")
    private int queueCapacity;

    @Value("${app.executor.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Bean(name = "ioExecutor", destroyMethod = "shutdown")
    public Executor ioExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("io-pool-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("Initialized ioExecutor: core={}, max={}, queue={}, keepAlive={}s",
                corePoolSize, maxPoolSize, queueCapacity, keepAliveSeconds);
        return executor;
    }
}
