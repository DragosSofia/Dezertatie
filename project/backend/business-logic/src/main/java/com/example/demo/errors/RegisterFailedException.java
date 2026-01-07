package com.example.demo.errors;

public class RegisterFailedException extends RuntimeException{
    public RegisterFailedException(String message) {
        super(message);
    }
}
