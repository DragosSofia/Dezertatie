package com.example.demo.auth;

import com.example.demo.auth.requests.LoginRequest;
import com.example.demo.auth.requests.RegisterRequest;
import com.example.demo.auth.responses.LoginResponse;
import com.example.demo.auth.responses.UserResponse;
import com.example.demo.constants.AppConstants;
import com.example.demo.errors.InvalidTokenException;
import com.example.demo.errors.LoginFailedException;
import com.example.demo.errors.RegisterFailedException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class AuthService {

    private final WebClient authClient;

    public void registerUser(RegisterRequest registerRequest) {
        callRegisterEndpoint(registerRequest);
    }

    private void callRegisterEndpoint(RegisterRequest registerRequest) {
        authClient.post()
                .uri("/auth/register")
                .bodyValue(registerRequest)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("No error body")
                                .map(body -> new RegisterFailedException(
                                        "Registration failed (" + response.statusCode() + "): " + body
                                ))
                )
                .bodyToMono(Void.class)
                .block();
    }

    public LoginResponse loginUser(LoginRequest loginRequest){
        log.info("Logging in user with username: " + loginRequest.getUsername());
        return authClient.post()
                .uri("/auth/login") // fixed typo: "loign" → "login"
                .bodyValue(loginRequest)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,   // <-- any non-2xx
                        clientResponse -> clientResponse
                                .bodyToMono(String.class)
                                .defaultIfEmpty("No error body")
                                .map(body -> new LoginFailedException(
                                        "Login failed (" + clientResponse.statusCode() + "): " + body
                                ))
                )
                .bodyToMono(LoginResponse.class)
                .block();
    }

    public UserResponse getUser(String token){
        UserResponse userResponse =
                authClient.post()
                .uri("/auth/me")
                        .header(AppConstants.tokenName, token)
                .retrieve()
                        .onStatus(
                                status -> status.value() == 429, // handle 429
                                clientResponse -> {
                                    log.error("Rate limit exceeded: 429 Too Many Requests");
                                    return Mono.error(new InvalidTokenException("Too many requests to auth server"));
                                })
                .bodyToMono(UserResponse.class)
                .block();

        log.info("Got authenticated user: " + userResponse);

        if (!isAuthenticated(userResponse)) {
            throw new InvalidTokenException("Token does not match any user");
        }

        return userResponse;
    }

    public boolean isAuthenticated(UserResponse user){
        if(user==null){
            log.warn("Got null user from auth server");
            return false;
        }

        return true;
    }
}
