package com.example.demo.Services;

import com.example.demo.Configuration.InfluxProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QueryBuilderService {
    private final InfluxProperties properties;

    public QueryBuilderService(InfluxProperties properties) {
        this.properties = properties;
    }

    public String buildMeasurementsQuery() {
        String query = String.format("import \"influxdata/influxdb/schema\"\n schema.measurements(bucket: \"%s\")", properties.getBucket());
        log.debug("Built measurements query: {}", query);
        return query;
    }

    public String buildFieldsForMeasurementsQuery(String measurement, LocalDateTime start, LocalDateTime end) {
        // Format start and end to ISO-8601 (RFC3339) required by InfluxDB
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        String formattedStart = start.format(formatter) + "Z"; // Influx expects Zulu time (UTC)
        String formattedEnd = end.format(formatter) + "Z";

        String query = String.format(
                "from(bucket: \"mesurements\") |> range(start: %s, stop: %s) |> filter(fn: (r) => r._measurement == \"%s\") |> group(columns: [\"_measurement\", \"_field\"]) |> distinct(column: \"_field\")",
                formattedStart, formattedEnd, measurement);
        log.info("Built fields query for measurement={}, range=[{} - {}]: {}", measurement, formattedStart, formattedEnd, query);
        return query;
    }

    public String buildGetDataFromMeasurements(String measurement, List<String> fields, LocalDateTime start, LocalDateTime end, String aggregationTime, String aggregationFunction){
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

        // Aggregation (ONLY if both are provided)
        if (aggregationTime != null && !aggregationTime.isBlank()
                && aggregationFunction != null && !aggregationFunction.isBlank()) {

            fluxBuilder.append(String.format("""
            |> aggregateWindow(every: %s, fn: %s, createEmpty: false)
            """, aggregationTime, aggregationFunction));
        }

        String query = fluxBuilder.toString();
        log.debug("Built data query for measurement={}, range=[{} - {}]: {}",
                measurement, formattedStart, formattedEnd, query);
        return query;
    }
}
