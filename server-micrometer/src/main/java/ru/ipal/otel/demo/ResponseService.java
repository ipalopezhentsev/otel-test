package ru.ipal.otel.demo;

import org.springframework.stereotype.Service;

import io.micrometer.observation.annotation.Observed;

@Service
public class ResponseService {
    @Observed(name = "qqq")
    public String calcResponse(String name) {
        return "Hello " + name;
    }
}
