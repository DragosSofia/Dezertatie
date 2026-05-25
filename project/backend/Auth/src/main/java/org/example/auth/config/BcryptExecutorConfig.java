package org.example.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
public class BcryptExecutorConfig {

    @Value("${app.bcrypt.threads:0}")
    private int configuredThreads;

    @Bean(name = "bcryptExecutor", destroyMethod = "shutdown")
    public ExecutorService bcryptExecutor() {
        int n = configuredThreads > 0
                ? configuredThreads
                : Math.max(2, Runtime.getRuntime().availableProcessors());

        AtomicInteger counter = new AtomicInteger();
        ThreadFactory tf = r -> {
            Thread t = new Thread(r, "bcrypt-" + counter.incrementAndGet());
            t.setDaemon(true);
            return t;
        };

        log.info("Initialized bcryptExecutor: bounded platform-thread pool of size {}", n);
        return Executors.newFixedThreadPool(n, tf);
    }
}
