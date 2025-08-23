package ru.ipal.otel.demo;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

@SpringBootApplication
@RestController
public class OtelDemoServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(OtelDemoServerApplication.class, args);
	}

	private final DoubleHistogram myLatencyMetric;

	public OtelDemoServerApplication() {
		final Meter meter = GlobalOpenTelemetry.getMeter("application");
		myLatencyMetric = meter.histogramBuilder("my_latency_seconds")
		.setDescription("Custom /greet latency histogram inside java method")
		.setExplicitBucketBoundariesAdvice(
			List.<Double>of(1E-7, 1E-6, 1E-05, 1E-4, 1E-3, 0.005, 0.01, 0.025, 0.05)
			)
		.build();
	}


	@GetMapping("/greet")
	public String greet(@RequestParam String name, @RequestParam int delayMillis) throws InterruptedException {
		var tmStart = System.nanoTime();
		if (delayMillis != 0) {
			Thread.sleep(delayMillis);
		}
		String calcResponse = calcResponse(name);
		var tmEnd = System.nanoTime();
		var myLatencySec = (tmEnd - tmStart) * 1.0E-9;
		myLatencyMetric.record(myLatencySec);
		return calcResponse;
	}

	@WithSpan
	public String calcResponse(@SpanAttribute String name) {
		return "Hello " + name;
	}
}
