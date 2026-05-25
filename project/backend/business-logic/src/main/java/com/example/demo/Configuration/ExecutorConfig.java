package com.example.demo.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Slf4j
@Configuration
public class ExecutorConfig {

    @Value("${app.executor.permits:128}")
    private int permits;

    @Bean(name = "ioExecutor", destroyMethod = "close")
    public SemaphoreBoundedExecutor ioExecutor(MeterRegistry registry) {
        ThreadFactory factory = Thread.ofVirtual().name("io-vt-", 0).factory();
        ExecutorService vt = Executors.newThreadPerTaskExecutor(factory);
        SemaphoreBoundedExecutor bounded = new SemaphoreBoundedExecutor(vt, permits);

        registry.gauge("io.executor.permits.available", bounded, SemaphoreBoundedExecutor::availablePermits);
        registry.gauge("io.executor.permits.queue", bounded, SemaphoreBoundedExecutor::queueLength);

        log.info("Initialized ioExecutor: virtual-thread-per-task, bounded by semaphore (permits={})", permits);
        return bounded;
    }
}
