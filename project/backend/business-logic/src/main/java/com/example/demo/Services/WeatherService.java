package com.example.demo.Services;

import com.example.demo.Configuration.BusinessMetrics;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WeatherService {

    private final InfluxDBClient influxDBClient;
    private final BusinessMetrics metrics;

    public WeatherService(InfluxDBClient influxDBClient, BusinessMetrics metrics) {
        this.influxDBClient = influxDBClient;
        this.metrics = metrics;
    }

    public String getWeatherData() {
        log.debug("Executing weather query against InfluxDB");
        Timer.Sample sample = Timer.start(metrics.registry());

        try {
            QueryApi queryApi = influxDBClient.getQueryApi();

            String flux = "from(bucket: \"mesurements\") |> range(start: -10h)";

            queryApi.query(flux).forEach(table -> {
                table.getRecords().forEach(record ->
                        log.trace("Record: time={} field={} value={}",
                                record.getTime(), record.getField(), record.getValue())
                );
            });

            log.info("Weather query completed");
            return "Query done!";
        } catch (Exception ex) {
            metrics.recordInfluxError("weather");
            log.error("Error querying weather data: {}", ex.getMessage(), ex);
            throw ex;
        } finally {
            sample.stop(metrics.influxTimer("weather"));
        }
    }
}
