package org.example.auth.services;

import io.micrometer.core.instrument.Timer;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthMetrics metrics;
    private final ExecutorService bcryptExecutor;

    public AuthService(UserRepository userRepo,
                       RoleRepository roleRepo,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       UserRepository userRepository,
                       AuthMetrics metrics,
                       @Qualifier("bcryptExecutor") ExecutorService bcryptExecutor) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.metrics = metrics;
        this.bcryptExecutor = bcryptExecutor;
    }

    public void register(RegisterDto dto) {
        Timer.Sample sample = Timer.start(metrics.registry());
        boolean success = false;
        try {
            if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("User already exists");
            }

            Role userRole = roleRepo.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            String encoded = hashOnBcryptPool(dto.getPassword());

            User user = new User();
            user.setEmail(dto.getEmail());
            user.setPassword(encoded);
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

            if (!matchesOnBcryptPool(rawPassword, user.getPassword())) {
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

    private String hashOnBcryptPool(String raw) {
        try {
            return bcryptExecutor.submit(() -> passwordEncoder.encode(raw)).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while hashing password", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof RuntimeException re) throw re;
            throw new RuntimeException(cause);
        }
    }

    private boolean matchesOnBcryptPool(String raw, String hashed) {
        try {
            return bcryptExecutor.submit(() -> passwordEncoder.matches(raw, hashed)).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while verifying password", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof RuntimeException re) throw re;
            throw new RuntimeException(cause);
        }
    }
}
