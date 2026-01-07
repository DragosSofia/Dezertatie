package com.example.demo.errors;

import lombok.extern.java.Log;

public class InvalidTokenException extends LoginFailedException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
