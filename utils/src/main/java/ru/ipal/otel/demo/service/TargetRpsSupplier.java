package ru.ipal.otel.demo.service;

import java.time.Instant;

@FunctionalInterface
public interface TargetRpsSupplier {
    double targetRpsAt(Instant now);
}
