package com.example.demo.constants;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth/")
public class QueryConstants {
    public static final String Start = "-1d";
}
