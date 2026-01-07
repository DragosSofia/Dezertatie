package org.example.auth.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.auth.exceptions.RateLimitExceededException;
import org.example.auth.exceptions.UnauthorizedException;
import org.example.auth.services.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final JwtService jwtService;

    @Value("${rate-limit.requests}")
    private int maxRequests;

    @Value("${rate-limit.minutes}")
    private int windowMinutes;

    public RateLimitFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.classic(
                                maxRequests,
                                Refill.greedy(
                                        maxRequests,
                                        Duration.ofMinutes(windowMinutes)
                                )
                        )
                )
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return uri.startsWith("/auth/login")
                || uri.startsWith("/auth/register")
                || uri.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = request.getHeader("jwtCookie");

        if (token == null || token.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String email;
        try {
            email = jwtService.extractUsername(token);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid JWT token");
        }

        Bucket bucket = buckets.computeIfAbsent(email, k -> createBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write("""
                    {
                      "status": 429,
                      "error": "Too Many Requests",
                      "message": "Too many requests. Please try again later."
                    }
                    """);
        }
    }
}
