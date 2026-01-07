package com.example.demo.auth.requests;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String emailUpb;
    private String password;
}
