package com.example.demo.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;


@Data
public class PointData {
    private String name;
    private String value;
    private Instant time;

    public PointData(String name, String value, Instant time) {
        this.name = name;
        this.value = value;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public Instant getTime() {
        return time;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "PointData{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", time=" + time +
                '}';
    }
}
