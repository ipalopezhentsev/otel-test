package ru.ipal.otel.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

@SpringBootApplication
@RestController
public class OtelDemoServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(OtelDemoServerApplication.class, args);
	}

	@GetMapping("/greet")
	public String greet(@RequestParam String name, @RequestParam int delayMillis) throws InterruptedException {
		if (delayMillis != 0) {
			Thread.sleep(delayMillis);
		}
		return calcResponse(name);
	}

	@WithSpan
	public String calcResponse(@SpanAttribute String name) {
		return "Hello " + name;
	}
}
