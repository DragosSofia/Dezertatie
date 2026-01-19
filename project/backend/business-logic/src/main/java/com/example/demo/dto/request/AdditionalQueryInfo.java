package com.example.demo.dto.request;

import com.example.demo.Configuration.ValidDateRange;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ValidDateRange
public class AdditionalQueryInfo {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate = LocalDateTime.now().minusDays(1);
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate = LocalDateTime.now();
    private String aggregationTime;
    private String aggregationType;
}
