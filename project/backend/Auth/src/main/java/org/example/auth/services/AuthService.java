package org.example.auth.services;

import io.micrometer.core.instrument.Timer;
import lombok.AllArgsConstructor;
import org.example.auth.config.AuthMetrics;
import org.example.auth.dtos.RegisterDto;
import org.example.auth.mappers.RegisterMapper;
import org.example.auth.mappers.UserMapperToUserResponse;
import org.example.auth.models.Role;
import org.example.auth.models.User;
import org.example.auth.repositories.RoleRepository;
import org.example.auth.repositories.UserRepository;
import org.example.auth.response.RoleResponse;
import org.example.auth.response.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthMetrics metrics;

    public void register(RegisterDto dto) {
        Timer.Sample sample = Timer.start(metrics.registry());
        boolean success = false;
        try {
            if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("User already exists");
            }

            Role userRole = roleRepo.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            User user = new User();
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setRoles(new java.util.HashSet<>());
            user.getRoles().add(userRole);

            userRepo.save(user);
            success = true;
        } finally {
            sample.stop(metrics.registerTimer());
            metrics.recordRegister(success);
        }
    }

    public String login(String username, String rawPassword) {
        Timer.Sample sample = Timer.start(metrics.registry());
        boolean success = false;
        try {
            User user = userRepo.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }

            String token = jwtService.generateToken(user);
            success = true;
            return token;
        } finally {
            sample.stop(metrics.loginTimer());
            metrics.recordLogin(success);
        }
    }

    public UserResponse getUserFromToken(String token) {
        Timer.Sample sample = Timer.start(metrics.registry());
        try {
            String username;
            try {
                username = jwtService.extractUsername(token);
            } catch (RuntimeException ex) {
                metrics.recordJwtError();
                throw ex;
            }

            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Set<RoleResponse> roles = user.getRoles().stream()
                    .map(role -> new RoleResponse(role.getName()))
                    .collect(Collectors.toSet());
            return new UserResponse(user.getEmailUpb(), user.getEmail(), roles);
        } finally {
            sample.stop(metrics.tokenLookupTimer());
        }
    }
}
