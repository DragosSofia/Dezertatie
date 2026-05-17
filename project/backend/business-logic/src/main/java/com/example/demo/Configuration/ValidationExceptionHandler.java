package com.example.demo.Configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        // Handle global (class-level) errors too
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                errors.put(error.getObjectName(), error.getDefaultMessage())
        );

        log.warn("Method argument validation failed: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }
}