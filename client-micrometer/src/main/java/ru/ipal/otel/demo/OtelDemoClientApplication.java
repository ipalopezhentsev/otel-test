package ru.ipal.otel.demo;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class OtelDemoClientApplication {
	@Bean
	public RestClient restClient(
			// without this, micrometer tracing context propagation won't work
			RestClient.Builder restClientBuilder,
			@Value("http://${SERVER_ADDR:localhost:8080}/") String serverAddr) {
		return restClientBuilder.baseUrl(serverAddr).build();
	}

	/**
	 * Enable RestClient to use virtual threads, without this it uses native threads...
	 * https://github.com/spring-projects/spring-framework/issues/34393
	 */
	@Bean
	public RestClientCustomizer virtualThreadsCustomizer() {
		HttpClient httpClient = HttpClient.newBuilder()
				.executor(Executors.newVirtualThreadPerTaskExecutor())
				.build();
		return (client) -> {
			client.requestFactory(new JdkClientHttpRequestFactory(httpClient));
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(OtelDemoClientApplication.class, args);
	}
}
