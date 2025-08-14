package ru.ipal.otel.demo;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ServerCallingApplicationRunner implements ApplicationRunner {
    private final RestClient restClient;

    public ServerCallingApplicationRunner(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var tmStart = Instant.now();
        final var execSvc = Executors.newVirtualThreadPerTaskExecutor();
        final int n = 10000;
        final var futs = new ArrayList<Future<String>>(n);
        final String name = "test";
        for (int i = 0; i < n; i++) {
            final var fut = execSvc.submit(() -> {
                var res = restClient.get().uri("greet?name=" + name).retrieve().body(String.class);
                //var res = "Hello " + name;
                return res;
            });
            futs.add(fut);
        }
        log.info("Submitted everything in {}, start waiting.", Duration.between(tmStart, Instant.now()));
        for (var fut : futs) {
            String res = fut.get();
            log.info(res);
        }
        execSvc.shutdown();
        log.info("Done in {}", Duration.between(tmStart, Instant.now()));
    }
}
