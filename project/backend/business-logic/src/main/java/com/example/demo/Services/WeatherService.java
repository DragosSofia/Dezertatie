package com.example.demo.Services;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WeatherService {

    private final InfluxDBClient influxDBClient;

    public WeatherService(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    public String getWeatherData() {
        log.debug("Executing weather query against InfluxDB");
        QueryApi queryApi = influxDBClient.getQueryApi();

        String flux = "from(bucket: \"mesurements\") |> range(start: -10h)";

        try {
            queryApi.query(flux).forEach(table -> {
                table.getRecords().forEach(record ->
                        log.trace("Record: time={} field={} value={}",
                                record.getTime(), record.getField(), record.getValue())
                );
            });
        } catch (Exception ex) {
            log.error("Error querying weather data: {}", ex.getMessage(), ex);
            throw ex;
        }

        log.info("Weather query completed");
        return "Query done!";
    }
}
