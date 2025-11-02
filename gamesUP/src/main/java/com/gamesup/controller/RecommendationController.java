package com.gamesup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class RecommendationController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/recommendations")
    public String showRecommendations() {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:8001/test/recommendations",
                    null,
                    String.class
            );

            return response.getBody();

        } catch (Exception e) {
            return "{\"error\": \"Service indisponible\"}";
        }
    }
}