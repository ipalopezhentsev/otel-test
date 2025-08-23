package ru.ipal.otel.demo.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;
import ru.ipal.otel.demo.service.TargetRpsMaintainer;
import ru.ipal.otel.demo.service.WarmingUpTargetRpsSupplier;

@RestController
@Slf4j
public class TargetRpsTestController {
    @Autowired
    private RestClient restClient;

    @PostMapping("/targetRps")
    public void testRps(
            @RequestParam int targetRps,
            @RequestParam int testSeconds,
            @RequestParam int warmupSeconds) throws InterruptedException, ExecutionException {
        var targetRpsSupplier = new WarmingUpTargetRpsSupplier(targetRps, Instant.now(),
                Duration.ofSeconds(testSeconds), Duration.ofSeconds(warmupSeconds));
        Runnable requestTask = () -> {
            try {
                restClient.get()
                        .uri(bld -> bld.path("greet")
                                .queryParam("name", "testName")
                                .queryParam("delayMillis", 0)
                                .build())
                        .retrieve()
                        .body(String.class);
            } catch (Exception e) {
                log.warn("Request failed", e);
            }
        };
        TargetRpsMaintainer maintainer = new TargetRpsMaintainer(targetRpsSupplier, requestTask);
        maintainer.start();
    }

}
