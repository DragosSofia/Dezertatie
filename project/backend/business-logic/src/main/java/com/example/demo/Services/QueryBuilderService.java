package com.example.demo.Services;

import com.example.demo.Configuration.InfluxProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueryBuilderService {
    private final InfluxProperties properties;

    public QueryBuilderService(InfluxProperties properties) {
        this.properties = properties;
    }

    public String buildMeasurementsQuery() {
        return String.format("import \"influxdata/influxdb/schema\"\n schema.measurements(bucket: \"%s\")", properties.getBucket());
    }

    public String buildFieldsForMeasurementsQuery(String measurement) {
        return String.format("from(bucket: \"mesurements\") |> range(start: -1d) |> filter(fn: (r) => r._measurement == \"%s\") |> group(columns: [\"_measurement\", \"_field\"]) |> distinct(column: \"_field\")", measurement);
    }

    public String buildGetDataFromMeasurements(String measurement, List<String> fields, LocalDateTime start, LocalDateTime end){
        // Format start and end to ISO-8601 (RFC3339) required by InfluxDB
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        String formattedStart = start.format(formatter) + "Z"; // Influx expects Zulu time (UTC)
        String formattedEnd = end.format(formatter) + "Z";

        // Build the base Flux query
        StringBuilder fluxBuilder = new StringBuilder(String.format("""
        from(bucket: "mesurements")
        |> range(start: %s, stop: %s)
        |> filter(fn: (r) => r._measurement == "%s")
        """, formattedStart, formattedEnd, measurement));

        // Add field filters if fields list is not empty
        if (fields != null && !fields.isEmpty()) {
            String fieldConditions = fields.stream()
                    .map(f -> String.format("r._field == \"%s\"", f))
                    .collect(Collectors.joining(" or "));
            fluxBuilder.append("\n|> filter(fn: (r) => ").append(fieldConditions).append(")");
        }

        return fluxBuilder.toString();
    }
}
