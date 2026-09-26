package com.astra.resilience;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class CircuitBreakerAspect {
    private final Map<String, State> states = new ConcurrentHashMap<>();

    @Around("@annotation(circuitBreaker)")
    public Object protect(ProceedingJoinPoint pjp, CircuitBreaker circuitBreaker) throws Throwable {
        State state = states.computeIfAbsent(circuitBreaker.name(), key -> new State());
        long now = System.currentTimeMillis();
        synchronized (state) {
            if (state.openUntil > now) {
                throw new IllegalStateException("Circuit is open: " + circuitBreaker.name());
            }
        }
        try {
            Object result = pjp.proceed();
            synchronized (state) {
                state.failures = 0;
                state.openUntil = 0;
            }
            return result;
        } catch (Throwable ex) {
            synchronized (state) {
                state.failures++;
                if (state.failures >= circuitBreaker.failureThreshold()) {
                    state.openUntil = now + circuitBreaker.openDurationMs();
                    state.failures = 0;
                }
            }
            throw ex;
        }
    }

    private static final class State {
        int failures;
        long openUntil;
    }
}
