package com.example.demo.Configuration;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class InfluxConfig {
    private final InfluxProperties influxProperties;

    @Value("${app.influx.okhttp.max-requests:64}")
    private int maxRequests;

    @Value("${app.influx.okhttp.max-requests-per-host:5}")
    private int maxRequestsPerHost;

    @Value("${app.influx.okhttp.max-idle-connections:5}")
    private int maxIdleConnections;

    @Value("${app.influx.okhttp.keep-alive-minutes:5}")
    private long keepAliveMinutes;

    @Value("${app.influx.okhttp.connect-timeout-ms:10000}")
    private long connectTimeoutMs;

    @Value("${app.influx.okhttp.read-timeout-ms:10000}")
    private long readTimeoutMs;

    @Value("${app.influx.okhttp.write-timeout-ms:10000}")
    private long writeTimeoutMs;

    public InfluxConfig(InfluxProperties influxProperties) {
        this.influxProperties = influxProperties;
    }

    @Bean(name = "customInfluxConfig")
    public InfluxDBClient influxDBClient() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(maxRequests);
        dispatcher.setMaxRequestsPerHost(maxRequestsPerHost);

        ConnectionPool connectionPool = new ConnectionPool(
                maxIdleConnections, keepAliveMinutes, TimeUnit.MINUTES);

        OkHttpClient.Builder okHttp = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeoutMs, TimeUnit.MILLISECONDS);

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url(influxProperties.getUrl())
                .authenticateToken(influxProperties.getToken().toCharArray())
                .org(influxProperties.getOrg())
                .bucket(influxProperties.getBucket())
                .okHttpClient(okHttp)
                .build();

        log.info("Initialized InfluxDBClient: dispatcher[maxRequests={}, perHost={}], pool[idle={}, keepAlive={}m], timeouts[connect={}ms, read={}ms, write={}ms]",
                maxRequests, maxRequestsPerHost,
                maxIdleConnections, keepAliveMinutes,
                connectTimeoutMs, readTimeoutMs, writeTimeoutMs);
        return InfluxDBClientFactory.create(options);
    }
}
