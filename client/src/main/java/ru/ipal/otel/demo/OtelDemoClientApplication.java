package ru.ipal.otel.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class OtelDemoClientApplication {
	@Bean
	public RestClient restClient(@Value("http://${SERVER_ADDR:localhost:8080}/") String serverAddr) {
		return RestClient.create(serverAddr);
	}

	public static void main(String[] args) {
		SpringApplication.run(OtelDemoClientApplication.class, args);
	}
}
