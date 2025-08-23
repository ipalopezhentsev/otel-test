package ru.ipal.otel.demo.service;

import java.time.Duration;
import java.time.Instant;

/**
 * Starts from 0 target rps, linearly goes to mainPhaseTargetRps in
 * warmupPhaseDuration.
 * Then stays at constant mainPhaseTargetRps for mainPhaseDuration.
 * Then linearly goes to 0 target rps in warmupPhaseDuration
 */
public class WarmingUpTargetRpsSupplier implements TargetRpsSupplier {
    private double mainPhaseTargetRps;
    private Instant startWarmup;
    private Instant startMainPhase;
    private Instant startWinddown;
    private Instant endWinddown;
    private double warmupPhaseDurationSeconds;

    public WarmingUpTargetRpsSupplier(
            double mainPhaseTargetRps,
            Instant start,
            Duration mainPhaseDuration,
            Duration warmupPhaseDuration) {
        this.mainPhaseTargetRps = mainPhaseTargetRps;
        this.startWarmup = start;
        this.startMainPhase = startWarmup.plus(warmupPhaseDuration);
        this.startWinddown = startMainPhase.plus(mainPhaseDuration);
        this.endWinddown = startWinddown.plus(warmupPhaseDuration);
        this.warmupPhaseDurationSeconds = warmupPhaseDuration.toMillis() / 1000.0;
    }

    @Override
    public double targetRpsAt(Instant now) {
        if (now.isBefore(startWarmup) || now.isAfter(endWinddown)) {
            return 0.0;
        }
        if (now.isBefore(startMainPhase)) {
            /**
             * rps = k*t
             * targetRps = k * warmupDuratonSec
             * k = targetRps / warmupDuratonSec
             */
            double secondsSinceWarmupStart = Duration.between(startWarmup, now).toMillis() / 1000.0;
            return secondsSinceWarmupStart * mainPhaseTargetRps / warmupPhaseDurationSeconds;
        } else if (now.isAfter(startWinddown)) {
            double secondsSinceWinddownStart = Duration.between(startWinddown, now).toMillis() / 1000.0;
            /**
             * rps = k*t + b
             * rps(0)= b = targetRps
             * rps(warmupDuratonSec) = k*warmupDuratonSec + b = 0
             * k = -targetRps/warmupDuratonSec
             * 
             * rps(t) = (-targetRps/warmupDuratonSec)*t + targetRps
             */
            return secondsSinceWinddownStart * (-mainPhaseTargetRps / warmupPhaseDurationSeconds) + mainPhaseTargetRps;
        } else {
            return mainPhaseTargetRps;
        }
    }
}
