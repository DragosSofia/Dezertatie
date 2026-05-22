package com.example.demo.Services;

import com.example.demo.Configuration.InfluxProperties;
import com.example.demo.Models.PointData;
import com.example.demo.auth.AuthService;
import com.example.demo.dto.request.AdditionalQueryInfo;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MeasurementsService {
    private final AuthService authService;
    private final InfluxDBClient influxDBClient;
    private final QueryBuilderService queryBuilderService;
    private final Executor ioExecutor;

    public MeasurementsService(AuthService authService,
                               InfluxDBClient influxDBClient,
                               QueryBuilderService queryBuilderService,
                               @Qualifier("ioExecutor") Executor ioExecutor) {
        this.authService = authService;
        this.influxDBClient = influxDBClient;
        this.queryBuilderService = queryBuilderService;
        this.ioExecutor = ioExecutor;
    }

    public List<String> getMeasurements(String token) {
        log.debug("Fetching measurements list (parallel auth + query)");

        CompletableFuture<Void> authFuture = CompletableFuture.runAsync(
                () -> authService.getUser(token), ioExecutor);

        CompletableFuture<List<String>> queryFuture = CompletableFuture.supplyAsync(() -> {
            String query = queryBuilderService.buildMeasurementsQuery();
            List<FluxTable> fluxTables = influxDBClient.getQueryApi().query(query);
            return fluxTables.stream()
                    .flatMap(table -> table.getRecords().stream())
                    .map(record -> record.getValue().toString())
                    .collect(Collectors.toList());
        }, ioExecutor);

        List<String> measurements = joinBoth(authFuture, queryFuture);
        log.info("Fetched {} measurements", measurements.size());
        return measurements;
    }

    public List<String> getFields(String measurement, String token) {
        log.debug("Fetching fields for measurement={} (parallel auth + query)", measurement);

        CompletableFuture<Void> authFuture = CompletableFuture.runAsync(
                () -> authService.getUser(token), ioExecutor);

        CompletableFuture<List<String>> queryFuture = CompletableFuture.supplyAsync(() -> {
            String query = queryBuilderService.buildFieldsForMeasurementsQuery(measurement);
            List<FluxTable> fluxTables = influxDBClient.getQueryApi().query(query);
            return fluxTables.stream()
                    .flatMap(table -> table.getRecords().stream())
                    .map(record -> record.getValueByKey("_field") != null
                            ? record.getValueByKey("_field").toString()
                            : "Unknown")
                    .collect(Collectors.toList());
        }, ioExecutor);

        List<String> fields = joinBoth(authFuture, queryFuture);
        log.info("Fetched {} fields for measurement={}", fields.size(), measurement);
        return fields;
    }

    public List<PointData> getData(String measurement, List<String> fields, AdditionalQueryInfo additionalQueryInfo, String token) {
        log.debug("Fetching data for measurement={}, fields={}, range=[{} - {}], aggregation={}/{} (parallel auth + query)",
                measurement, fields,
                additionalQueryInfo.getStartDate(), additionalQueryInfo.getEndDate(),
                additionalQueryInfo.getAggregationTime(), additionalQueryInfo.getAggregationType());

        CompletableFuture<Void> authFuture = CompletableFuture.runAsync(
                () -> authService.getUser(token), ioExecutor);

        CompletableFuture<List<PointData>> queryFuture = CompletableFuture.supplyAsync(() -> {
            QueryApi queryApi = influxDBClient.getQueryApi();
            String query = queryBuilderService.buildGetDataFromMeasurements(
                    measurement, fields,
                    additionalQueryInfo.getStartDate(),
                    additionalQueryInfo.getEndDate(),
                    additionalQueryInfo.getAggregationTime(),
                    additionalQueryInfo.getAggregationType());

            List<PointData> result = new ArrayList<>();
            queryApi.query(query).forEach(table ->
                    table.getRecords().forEach(record ->
                            result.add(new PointData(
                                    record.getField(),
                                    Objects.requireNonNull(record.getValue()).toString(),
                                    record.getTime()))));
            return result;
        }, ioExecutor);

        List<PointData> data = joinBoth(authFuture, queryFuture);
        log.info("Fetched {} data points for measurement={}", data.size(), measurement);
        return data;
    }

    private <T> T joinBoth(CompletableFuture<Void> authFuture, CompletableFuture<T> queryFuture) {
        try {
            CompletableFuture.allOf(authFuture, queryFuture).join();
            return queryFuture.join();
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            log.error("Parallel auth/query execution failed: {}", cause.getMessage(), cause);
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(cause);
        }
    }
}
