package com.example.demo.dto.request;

import com.example.demo.Configuration.ValidDateRange;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@ValidDateRange
public class AdditionalQueryInfo {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate = LocalDateTime.now().minusDays(1);
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate = LocalDateTime.now();

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "AdditionalQueryInfo{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
