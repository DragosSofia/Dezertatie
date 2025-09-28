package com.example.demo.Controllers;

import com.example.demo.Models.PointData;
import com.example.demo.Services.MeasurementsService;
import com.example.demo.Services.WeatherService;
import com.example.demo.dto.request.AdditionalQueryInfo;
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
        return weatherService.getWeatherData();
    }

    @GetMapping("/measurements")
    @ResponseBody
    public List<String> getMeasurements() {
        return measurementsService.getMeasurements();
    }

    @GetMapping("/measurements/{measurementName}/fields")
    @ResponseBody
    public List<String> getFields(@PathVariable String measurementName) {
        return measurementsService.getFields(measurementName);
    }

    @GetMapping("/measurements/{measurementName}/data")
    @ResponseBody
    public List<PointData> getData(@PathVariable String measurementName,
                                   @RequestParam(required = false) List<String> fields,
                                   @Valid @RequestBody AdditionalQueryInfo additionalQueryInfo
                                   ) {
        log.info("Received data request with: measurement:{}, fields:{}, body:{}", measurementName,  fields, additionalQueryInfo);
        return measurementsService.getData(measurementName, fields, additionalQueryInfo);
    }
}
