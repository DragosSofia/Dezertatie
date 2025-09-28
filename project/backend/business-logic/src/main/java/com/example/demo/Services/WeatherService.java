package com.example.demo.Services;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    private final InfluxDBClient influxDBClient;

    public WeatherService(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    public String getWeatherData() {
        QueryApi queryApi = influxDBClient.getQueryApi();

        String flux = "from(bucket: \"mesurements\") |> range(start: -10h)";

        queryApi.query(flux).forEach(table -> {
            table.getRecords().forEach(record -> {

                System.out.println(record.getTime() + " " + record.getField() + "=" + record.getValue());
            });
        });

        return "Query done!";
    }
}
