package com.example.demo.Configuration;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxConfig {
    private final InfluxProperties influxProperties;

    public InfluxConfig(InfluxProperties influxProperties) {
        this.influxProperties = influxProperties;
    }

    @Bean(name = "customInfluxConfig")  // Rename the bean here
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(
                influxProperties.getUrl(),
                influxProperties.getToken().toCharArray(),
                influxProperties.getOrg(),
                influxProperties.getBucket()
        );
    }
}