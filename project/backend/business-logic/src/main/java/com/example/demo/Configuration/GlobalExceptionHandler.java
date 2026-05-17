package com.example.demo.Configuration;
import com.example.demo.errors.LoginFailedException;
import com.example.demo.errors.RegisterFailedException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage()));

        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Validation failed", "details", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;

            if (ife.getTargetType() == LocalDateTime.class) {
                log.warn("Invalid date format in request: {}", ife.getMessage());
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Invalid date format",
                        "message", "Date and time must be in the format yyyy-MM-dd'T'HH:mm:ss (e.g., 2025-12-17T10:00:00)"
                ));
            }
        }

        log.warn("Malformed request body: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Malformed request",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String field = cv.getPropertyPath().toString();
            String message = cv.getMessage();
            errors.put(field, message);
        });

        log.warn("Constraint violation: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Validation failed", "details", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericError(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "An unexpected error occurred", "error", ex.getMessage()));
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<?> handleLoginFailed(LoginFailedException ex) {
        log.warn("Login failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.UNAUTHORIZED.value(),
                        "error", "Login failed",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(RegisterFailedException.class)
    public ResponseEntity<?> handleRegisterFailed(RegisterFailedException ex) {
        log.warn("Registration failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.UNAUTHORIZED.value(),
                        "error", "Login failed",
                        "message", ex.getMessage()
                ));
    }

}