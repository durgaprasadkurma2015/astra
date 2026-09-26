package com.astra.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class MetricsConfig {
    private final AtomicLong heartbeat = new AtomicLong();
    private final MeterRegistry meterRegistry;

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        meterRegistry.gauge("astra.scheduler.heartbeat", heartbeat);
    }

    @Scheduled(fixedDelayString = "${astra.metrics.heartbeat-ms:60000}")
    public void heartbeat() {
        heartbeat.incrementAndGet();
    }
}
