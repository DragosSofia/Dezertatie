package com.example.demo.Services;

import com.example.demo.Configuration.InfluxProperties;
import com.example.demo.Models.PointData;
import com.example.demo.auth.AuthService;
import com.example.demo.dto.request.AdditionalQueryInfo;
import com.example.demo.dto.request.TimeRangeInfo;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxTable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MeasurementsService {
    private final AuthService authService;
    private final InfluxDBClient influxDBClient;
    private final QueryBuilderService queryBuilderService;

    public MeasurementsService(AuthService authService, InfluxDBClient influxDBClient, QueryBuilderService queryBuilderService) {
        this.authService = authService;
        this.influxDBClient = influxDBClient;
        this.queryBuilderService = queryBuilderService;
    }

    public List<String> getMeasurements(String token) {
        log.debug("Fetching measurements list");
        String query = queryBuilderService.buildMeasurementsQuery();
        List<FluxTable> fluxTables = influxDBClient.getQueryApi().query(query);

        //get user
        authService.getUser(token);

        List<String> measurements = fluxTables.stream()
                .flatMap(table -> table.getRecords().stream())
                .map(record -> record.getValue().toString()) // Adjusted for the 'name' key
                .collect(Collectors.toList());
        log.info("Fetched {} measurements", measurements.size());
        return measurements;
    }

    public List<String> getFields( String measurement, TimeRangeInfo timeRangeInfo, String token) {
        // Use default time range if not provided
        if (timeRangeInfo == null) {
            timeRangeInfo = new TimeRangeInfo();
        }

        log.debug("Fetching fields for measurement={}, range=[{} - {}]",
                measurement, timeRangeInfo.getStartDate(), timeRangeInfo.getEndDate());
        //get user
         authService.getUser(token);
        // Query to fetch fields from a specific measurement in the bucket
        String query = queryBuilderService.buildFieldsForMeasurementsQuery(
                measurement,
                timeRangeInfo.getStartDate(),
                timeRangeInfo.getEndDate());

        List<FluxTable> fluxTables = influxDBClient.getQueryApi().query(query);

        List<String> fields = fluxTables.stream()
                .flatMap(table -> table.getRecords().stream())
                .map(record -> record.getValueByKey("_field") != null ? record.getValueByKey("_field").toString() : "Unknown")
                .collect(Collectors.toList());
        log.info("Fetched {} fields for measurement={}", fields.size(), measurement);
        return fields;
    }

    public List<PointData> getData(String measurement, List<String> fields, AdditionalQueryInfo additionalQueryInfo, String token){
        log.debug("Fetching data for measurement={}, fields={}, range=[{} - {}], aggregation={}/{}",
                measurement, fields,
                additionalQueryInfo.getStartDate(), additionalQueryInfo.getEndDate(),
                additionalQueryInfo.getAggregationTime(), additionalQueryInfo.getAggregationType());
        //get user
        authService.getUser(token);

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<PointData> result = new ArrayList<>();
        // Build the base Flux query
        String query = queryBuilderService.buildGetDataFromMeasurements(
                measurement, fields,
                additionalQueryInfo.getStartDate(),
                additionalQueryInfo.getEndDate(),
                additionalQueryInfo.getAggregationTime(),
                additionalQueryInfo.getAggregationType());

        try {
            queryApi.query(query).forEach(table -> {
                table.getRecords().forEach(record -> {
                    result.add(new PointData(record.getField(), Objects.requireNonNull(record.getValue()).toString(),  record.getTime()));
                });

            });
        } catch (Exception ex) {
            log.error("Error querying InfluxDB for measurement={}: {}", measurement, ex.getMessage(), ex);
            throw ex;
        }
        log.info("Fetched {} data points for measurement={}", result.size(), measurement);
        return result;
    }
}
