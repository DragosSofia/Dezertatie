package com.example.demo.auth.responses;

import lombok.Data;

import java.util.List;

@Data
public class UserResponse {
    public String email;
    public String emailUpb;
    List<RoleResponse> roles;
}
