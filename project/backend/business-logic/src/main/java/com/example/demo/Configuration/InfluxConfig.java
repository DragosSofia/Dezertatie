package com.example.demo.Configuration;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class InfluxConfig {
    private final InfluxProperties influxProperties;

    @Value("${app.influx.okhttp.max-requests:64}")
    private int maxRequests;

    @Value("${app.influx.okhttp.max-requests-per-host:5}")
    private int maxRequestsPerHost;

    public InfluxConfig(InfluxProperties influxProperties) {
        this.influxProperties = influxProperties;
    }

    @Bean(name = "customInfluxConfig")
    public InfluxDBClient influxDBClient() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(maxRequests);
        dispatcher.setMaxRequestsPerHost(maxRequestsPerHost);

        OkHttpClient.Builder okHttp = new OkHttpClient.Builder()
                .dispatcher(dispatcher);

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(influxProperties.getUrl())
                .authenticateToken(influxProperties.getToken().toCharArray())
                .org(influxProperties.getOrg())
                .bucket(influxProperties.getBucket())
                .okHttpClient(okHttp)
                .build();

        log.info("Initialized InfluxDBClient: OkHttp dispatcher maxRequests={}, maxRequestsPerHost={}",
                maxRequests, maxRequestsPerHost);
        return InfluxDBClientFactory.create(options);
    }
}
