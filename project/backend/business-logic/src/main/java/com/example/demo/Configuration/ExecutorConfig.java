package com.example.demo.Configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Slf4j
@Configuration
public class ExecutorConfig {

    @Bean(name = "ioExecutor", destroyMethod = "close")
    public Executor ioExecutor() {
        ThreadFactory factory = Thread.ofVirtual().name("io-vt-", 0).factory();
        log.info("Initialized ioExecutor backed by virtual threads (one per task)");
        return Executors.newThreadPerTaskExecutor(factory);
    }
}
