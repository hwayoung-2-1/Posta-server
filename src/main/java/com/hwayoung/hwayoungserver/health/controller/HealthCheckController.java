package com.hwayoung.hwayoungserver.health.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<HealthCheckResponse> health() {
        return ResponseEntity.ok(new HealthCheckResponse("UP"));
    }

    public record HealthCheckResponse(String status) {
    }
}
