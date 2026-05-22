package com.example.demo.Configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Centralized custom metrics for the business-logic service.
 *
 * Exposes timers + counters that the Grafana dashboard reads via Prometheus.
 * Lookup is done by tag so cardinality stays bounded (measurement names, op names,
 * remote endpoints are all low-cardinality in this app).
 */
@Component
public class BusinessMetrics {

    private final MeterRegistry registry;

    private final ConcurrentMap<String, Timer> influxTimers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> influxErrors = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> authClientTimers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> authClientErrors = new ConcurrentHashMap<>();

    private final DistributionSummary pointsReturned;
    private final DistributionSummary measurementsReturned;
    private final DistributionSummary fieldsReturned;

    public BusinessMetrics(MeterRegistry registry) {
        this.registry = registry;

        this.pointsReturned = DistributionSummary.builder("resource.data.points")
                .description("Number of PointData rows returned by /resource/measurements/*/data")
                .baseUnit("rows")
                .publishPercentileHistogram()
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        this.measurementsReturned = DistributionSummary.builder("resource.measurements.size")
                .description("Number of measurement names returned by /resource/measurements")
                .baseUnit("items")
                .register(registry);

        this.fieldsReturned = DistributionSummary.builder("resource.fields.size")
                .description("Number of fields returned by /resource/measurements/*/fields")
                .baseUnit("items")
                .register(registry);
    }

    public Timer influxTimer(String operation) {
        return influxTimers.computeIfAbsent(operation, op -> Timer.builder("resource.influx.query.duration")
                .tag("operation", op)
                .description("Time spent executing a Flux query against InfluxDB")
                .publishPercentileHistogram()
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry));
    }

    public void recordInfluxError(String operation) {
        influxErrors.computeIfAbsent(operation, op -> Counter.builder("resource.influx.query.errors")
                .tag("operation", op)
                .description("Errors thrown while querying InfluxDB")
                .register(registry)).increment();
    }

    public Timer authClientTimer(String endpoint) {
        return authClientTimers.computeIfAbsent(endpoint, ep -> Timer.builder("resource.auth_client.duration")
                .tag("endpoint", ep)
                .description("Time spent on inter-service calls to the Auth service")
                .publishPercentileHistogram()
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry));
    }

    public void recordAuthClientError(String endpoint, String kind) {
        String key = endpoint + "|" + kind;
        authClientErrors.computeIfAbsent(key, k -> Counter.builder("resource.auth_client.errors")
                .tag("endpoint", endpoint)
                .tag("kind", kind)
                .description("Errors while calling the Auth service")
                .register(registry)).increment();
    }

    public void recordPointsReturned(long count) {
        pointsReturned.record(count);
    }

    public void recordMeasurementsReturned(long count) {
        measurementsReturned.record(count);
    }

    public void recordFieldsReturned(long count) {
        fieldsReturned.record(count);
    }

    public MeterRegistry registry() {
        return registry;
    }
}
