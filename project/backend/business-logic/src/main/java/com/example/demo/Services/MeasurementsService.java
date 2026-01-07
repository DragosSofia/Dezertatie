package com.example.demo.Services;

import com.example.demo.Configuration.InfluxProperties;
import com.example.demo.Models.PointData;
import com.example.demo.auth.AuthService;
import com.example.demo.dto.request.AdditionalQueryInfo;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxTable;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        String query = queryBuilderService.buildMeasurementsQuery();
        List<FluxTable> fluxTables = influxDBClient.getQueryApi().query(query);

        //get user
        authService.getUser(token);

        return fluxTables.stream()
                .flatMap(table -> table.getRecords().stream())
                .map(record -> record.getValue().toString()) // Adjusted for the 'name' key
                .collect(Collectors.toList());
    }

    public List<String> getFields( String measurement, String token) {
        //get user
         authService.getUser(token);
        // Query to fetch fields from a specific measurement in the bucket
        String query = queryBuilderService.buildFieldsForMeasurementsQuery(measurement);

        List<FluxTable> fluxTables = influxDBClient.getQueryApi().query(query);

        return fluxTables.stream()
                .flatMap(table -> table.getRecords().stream())
                .map(record -> record.getValueByKey("_field") != null ? record.getValueByKey("_field").toString() : "Unknown")
                .collect(Collectors.toList());
    }

    public List<PointData> getData(String measurement, List<String> fields, AdditionalQueryInfo additionalQueryInfo, String token){
        //get user
        authService.getUser(token);

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<PointData> result = new ArrayList<>();
        // Build the base Flux query
        String query = queryBuilderService.buildGetDataFromMeasurements(
                measurement, fields,
                additionalQueryInfo.getStartDate(),
                additionalQueryInfo.getEndDate());

        queryApi.query(query).forEach(table -> {
            table.getRecords().forEach(record -> {
               // System.out.println(record.getTime() + " " + record.getField() + "=" + record.getValue());
                result.add(new PointData(record.getField(), Objects.requireNonNull(record.getValue()).toString(),  record.getTime()));
            });

        });
        return result;
    }
}
