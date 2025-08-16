package ru.ipal.otel.demo;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CallTestController {
	@Autowired
	private RestClient restClient;

	@PostMapping("/test")
	public void test(
			@RequestParam int numRqs,
			@RequestParam int delayMillis) throws InterruptedException, ExecutionException {
		var tmStart = Instant.now();
		final var execSvc = Executors.newVirtualThreadPerTaskExecutor();
		final var futs = new ArrayList<Future<String>>(numRqs);
		final String name = "test";
		for (int i = 0; i < numRqs; i++) {
			final var fut = execSvc.submit(() -> {
				var res = restClient.get()
						.uri(bld -> bld.path("greet")
								.queryParam("name", name)
								.queryParam("delayMillis", delayMillis)
								.build())
						.retrieve()
						.body(String.class);
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
