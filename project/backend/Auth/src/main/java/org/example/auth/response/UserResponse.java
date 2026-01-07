package org.example.auth.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.auth.models.Role;

import java.util.Set;

@Data
@AllArgsConstructor
public class UserResponse {
    private String emailUpb;
    private String email;
    private Set<RoleResponse> roles;
}