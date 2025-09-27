package ru.ipal.otel.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;

@SpringBootApplication
@RestController
public class OtelDemoServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(OtelDemoServerApplication.class, args);
	}

	@Autowired
	private ResponseService respService;

	public OtelDemoServerApplication() {
	}

	//https://spring.io/blog/2022/10/12/observability-with-spring-boot-3
	// To have the @Observed support we need to register this aspect
	@Bean
	ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
		return new ObservedAspect(observationRegistry);
	}

	@GetMapping("/greet")
	public String greet(@RequestParam String name, @RequestParam int delayMillis) throws InterruptedException {
		// var tmStart = System.nanoTime();
		if (delayMillis != 0) {
			Thread.sleep(delayMillis);
		}
		String calcResponse = respService.calcResponse(name);
		// var tmEnd = System.nanoTime();
		// var myLatencySec = (tmEnd - tmStart) * 1.0E-9;
		return calcResponse;
	}
}
