package ru.ipal.otel.demo.service;

import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TargetRpsMaintainer {
    private final TargetRpsSupplier targetRpsSupplier;
    private final Runnable requestTask;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ExecutorService execSvc = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicInteger completedRequests = new AtomicInteger(0);
    private volatile boolean running = false;
    private volatile int lastCompleted = 0;

    public TargetRpsMaintainer(TargetRpsSupplier targetRpsSupplier, Runnable requestTask) {
        this.targetRpsSupplier = targetRpsSupplier;
        this.requestTask = requestTask;
    }

    public void start() {
        running = true;
        scheduler.scheduleAtFixedRate(this::adjustRate, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        running = false;
        scheduler.shutdownNow();
    }

    private void adjustRate() {
        if (!running) {
            return;
        }
        Instant now = Instant.now();
        double targetRps = targetRpsSupplier.targetRpsAt(now);
        int completed = completedRequests.get();
        int observedRps = completed - lastCompleted;
        lastCompleted = completed;

        // Always start the full targetRps per interval, plus a small correction for drift
        int correction = (int) Math.round(targetRps - observedRps); // optional, can be tuned
        int toStart = (int) Math.round(targetRps) + (correction > 0 ? correction : 0);
        if (toStart < 0) toStart = 0;
        for (int i = 0; i < toStart; i++) {
            execSvc.submit(() -> {
                try {
                    requestTask.run();
                } finally {
                    completedRequests.incrementAndGet();
                }
            });
        }
        log.info("Target RPS: {} | Observed RPS: {} | Starting {} requests", 
            targetRps, observedRps, toStart);
    }
}
