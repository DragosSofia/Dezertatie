package org.example.auth.cotrollers;

import lombok.AllArgsConstructor;
import org.example.auth.dtos.RegisterDto;
import org.example.auth.mappers.RegisterMapper;
import org.example.auth.request.LoginRequest;
import org.example.auth.request.RegisterRequest;
import org.example.auth.request.TokenRequest;
import org.example.auth.response.LoginResponse;
import org.example.auth.response.UserResponse;
import org.example.auth.services.AuthService;
import org.example.auth.services.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final RegisterMapper registerMapper;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        RegisterDto dto = new RegisterDto(req.getEmail(), req.getEmailUpb(), req.getPassword());

        authService.register(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String token = authService.login(
                request.username(),
                request.password()
        );
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/me")
    public ResponseEntity<UserResponse> me(@RequestHeader("jwtCookie") String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserResponse response = authService.getUserFromToken(token);
        return ResponseEntity.ok(response);
    }
}