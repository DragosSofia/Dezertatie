package com.example.demo.Controllers;

import com.example.demo.Models.PointData;
import com.example.demo.Services.MeasurementsService;
import com.example.demo.Services.WeatherService;
import com.example.demo.constants.AppConstants;
import com.example.demo.dto.request.AdditionalQueryInfo;
import com.example.demo.dto.request.TimeRangeInfo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/resource/")
public class ResourceController {

    private final WeatherService weatherService;
    private final MeasurementsService measurementsService;

    public ResourceController(WeatherService weatherService, MeasurementsService measurementsService) {
        this.weatherService = weatherService;
        this.measurementsService = measurementsService;
    }


    @GetMapping("weather")
    @ResponseBody
    public String weather() {
        log.info("GET /resource/weather received");
        String result = weatherService.getWeatherData();
        log.debug("Weather response: {}", result);
        return result;
    }

    @GetMapping("/measurements")
    @ResponseBody
    public List<String> getMeasurements(@RequestHeader(AppConstants.tokenName) String token) {
        log.info("GET /resource/measurements received");
        List<String> measurements = measurementsService.getMeasurements(token);
        log.info("Returning {} measurements", measurements.size());
        return measurements;
    }

    @PostMapping("/measurements/{measurementName}/fields")
    @ResponseBody
    public List<String> getFields(@PathVariable String measurementName,
                                   @RequestBody(required = false) TimeRangeInfo timeRangeInfo,
                                   @RequestHeader(AppConstants.tokenName) String token) {
        log.info("POST /resource/measurements/{}/fields received with timeRange={}", measurementName, timeRangeInfo);
        List<String> fields = measurementsService.getFields(measurementName, timeRangeInfo, token);
        log.info("Returning {} fields for measurement={}", fields.size(), measurementName);
        return fields;
    }

    @GetMapping("/measurements/{measurementName}/data")
    @ResponseBody
    public List<PointData> getData(@PathVariable String measurementName,
                                   @RequestParam(required = false) List<String> fields,
                                   @Valid @RequestBody AdditionalQueryInfo additionalQueryInfo,
                                   @RequestHeader(AppConstants.tokenName) String token
                                   ) {
        log.info("GET /resource/measurements/{}/data received with fields={}, queryInfo={}",
                measurementName, fields, additionalQueryInfo);
        List<PointData> data = measurementsService.getData(measurementName, fields, additionalQueryInfo, token);
        log.info("Returning {} data points for measurement={}", data.size(), measurementName);
        return data;
    }
}
