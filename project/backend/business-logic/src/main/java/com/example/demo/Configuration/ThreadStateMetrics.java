package com.example.demo.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

@Slf4j
@Component
public class ThreadStateMetrics {

    private final MeterRegistry registry;
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    public ThreadStateMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        // Gauge for each thread state
        log.info("Registering thread state metrics");
        registry.gauge("jvm_threads_state_runnable", this, t -> getThreadCountByState(Thread.State.RUNNABLE));
        registry.gauge("jvm_threads_state_waiting", this, t -> getThreadCountByState(Thread.State.WAITING));
        registry.gauge("jvm_threads_state_blocked", this, t -> getThreadCountByState(Thread.State.BLOCKED));
        registry.gauge("jvm_threads_state_timed_waiting", this, t -> getThreadCountByState(Thread.State.TIMED_WAITING));
        registry.gauge("jvm_threads_state_new", this, t -> getThreadCountByState(Thread.State.NEW));
        registry.gauge("jvm_threads_state_terminated", this, t -> getThreadCountByState(Thread.State.TERMINATED));
    }

    private int getThreadCountByState(Thread.State state) {
        return (int) Thread.getAllStackTraces().keySet().stream()
                .filter(t -> t.getState() == state)
                .count();
    }
}
