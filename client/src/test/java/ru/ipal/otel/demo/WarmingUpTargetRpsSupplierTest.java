package ru.ipal.otel.demo;

import org.junit.jupiter.api.Test;

import ru.ipal.otel.demo.service.WarmingUpTargetRpsSupplier;

import java.time.Duration;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class WarmingUpTargetRpsSupplierTest {

    private static final double EPSILON = 1e-6;

    @Test
    void testBeforeWarmupReturnsZero() {
        Instant start = Instant.now();
        Duration mainPhase = Duration.ofSeconds(10);
        Duration warmup = Duration.ofSeconds(5);
        var supplier = new WarmingUpTargetRpsSupplier(100.0, start, mainPhase, warmup);

        Instant beforeWarmup = start.minusSeconds(1);
        assertEquals(0.0, supplier.targetRpsAt(beforeWarmup), EPSILON);
    }

    @Test
    void testWarmupPhaseLinearity() {
        Instant start = Instant.now();
        Duration mainPhase = Duration.ofSeconds(10);
        Duration warmup = Duration.ofSeconds(5);
        double targetRps = 100.0;
        var supplier = new WarmingUpTargetRpsSupplier(targetRps, start, mainPhase, warmup);

        // At start
        assertEquals(0.0, supplier.targetRpsAt(start), EPSILON);

        // Halfway through warmup
        Instant halfway = start.plusSeconds(2);
        double expectedHalfway = targetRps * 2.0 / 5.0;
        assertEquals(expectedHalfway, supplier.targetRpsAt(halfway), EPSILON);

        // At end of warmup
        Instant endWarmup = start.plus(warmup);
        assertEquals(targetRps, supplier.targetRpsAt(endWarmup), EPSILON);
    }

    @Test
    void testMainPhaseConstant() {
        Instant start = Instant.now();
        Duration mainPhase = Duration.ofSeconds(10);
        Duration warmup = Duration.ofSeconds(5);
        double targetRps = 100.0;
        WarmingUpTargetRpsSupplier supplier = new WarmingUpTargetRpsSupplier(targetRps, start, mainPhase, warmup);

        Instant mainPhaseStart = start.plus(warmup);
        Instant mainPhaseMiddle = mainPhaseStart.plusSeconds(5);
        Instant mainPhaseEnd = mainPhaseStart.plus(mainPhase);

        assertEquals(targetRps, supplier.targetRpsAt(mainPhaseStart), EPSILON);
        assertEquals(targetRps, supplier.targetRpsAt(mainPhaseMiddle), EPSILON);
        assertEquals(targetRps, supplier.targetRpsAt(mainPhaseEnd), EPSILON);
    }

    @Test
    void testWinddownPhaseLinearity() {
        Instant start = Instant.now();
        Duration mainPhase = Duration.ofSeconds(10);
        Duration warmup = Duration.ofSeconds(5);
        double targetRps = 100.0;
        WarmingUpTargetRpsSupplier supplier = new WarmingUpTargetRpsSupplier(targetRps, start, mainPhase, warmup);

        Instant winddownStart = start.plus(warmup).plus(mainPhase);
        Instant halfway = winddownStart.plusSeconds(2);
        Instant winddownEnd = winddownStart.plus(warmup);

        // At start of winddown
        assertEquals(targetRps, supplier.targetRpsAt(winddownStart), EPSILON);

        // Halfway through winddown
        double expectedHalfway = targetRps * (1 - 2.0 / 5.0);
        assertEquals(expectedHalfway, supplier.targetRpsAt(halfway), EPSILON);

        // At end of winddown
        assertEquals(0.0, supplier.targetRpsAt(winddownEnd), EPSILON);
    }

    @Test
    void testAfterWinddownReturnsZero() {
        Instant start = Instant.now();
        Duration mainPhase = Duration.ofSeconds(10);
        Duration warmup = Duration.ofSeconds(5);
        WarmingUpTargetRpsSupplier supplier = new WarmingUpTargetRpsSupplier(100.0, start, mainPhase, warmup);

        Instant afterWinddown = start.plus(warmup).plus(mainPhase).plus(warmup).plusSeconds(1);
        assertEquals(0.0, supplier.targetRpsAt(afterWinddown), EPSILON);
    }
}