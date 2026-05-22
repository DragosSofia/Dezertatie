package org.example.auth.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class AuthMetrics {

    private final MeterRegistry registry;

    private final Timer loginTimer;
    private final Timer registerTimer;
    private final Timer tokenLookupTimer;

    private final Counter loginSuccess;
    private final Counter loginFailure;
    private final Counter registerSuccess;
    private final Counter registerFailure;
    private final Counter jwtErrors;
    private final Counter rateLimitHits;

    public AuthMetrics(MeterRegistry registry) {
        this.registry = registry;

        this.loginTimer = Timer.builder("auth.login.duration")
                .description("Time spent processing /auth/login (DB lookup + bcrypt + JWT sign)")
                .publishPercentileHistogram()
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        this.registerTimer = Timer.builder("auth.register.duration")
                .description("Time spent processing /auth/register")
                .publishPercentileHistogram()
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        this.tokenLookupTimer = Timer.builder("auth.token.lookup.duration")
                .description("Time spent processing /auth/me (JWT parse + DB lookup)")
                .publishPercentileHistogram()
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        this.loginSuccess = Counter.builder("auth.login.attempts")
                .tag("result", "success")
                .description("Successful login attempts")
                .register(registry);

        this.loginFailure = Counter.builder("auth.login.attempts")
                .tag("result", "failure")
                .description("Failed login attempts")
                .register(registry);

        this.registerSuccess = Counter.builder("auth.register.attempts")
                .tag("result", "success")
                .description("Successful user registrations")
                .register(registry);

        this.registerFailure = Counter.builder("auth.register.attempts")
                .tag("result", "failure")
                .description("Failed user registrations")
                .register(registry);

        this.jwtErrors = Counter.builder("auth.jwt.errors")
                .description("JWT parsing or validation errors")
                .register(registry);

        this.rateLimitHits = Counter.builder("auth.rate_limit.hits")
                .description("Requests rejected by the per-user rate limiter (HTTP 429)")
                .register(registry);
    }

    public Timer loginTimer() { return loginTimer; }
    public Timer registerTimer() { return registerTimer; }
    public Timer tokenLookupTimer() { return tokenLookupTimer; }

    public void recordLogin(boolean success) {
        (success ? loginSuccess : loginFailure).increment();
    }

    public void recordRegister(boolean success) {
        (success ? registerSuccess : registerFailure).increment();
    }

    public void recordJwtError() {
        jwtErrors.increment();
    }

    public void recordRateLimitHit() {
        rateLimitHits.increment();
    }

    public MeterRegistry registry() {
        return registry;
    }
}
