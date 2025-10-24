package com.example.demo.controller;

import com.example.demo.service.SmartHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {
    
    private final SmartHomeService smartHomeService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAutomationStats() {
        return ResponseEntity.ok(smartHomeService.getAutomationStats());
    }
    
    @GetMapping("/efficiency")
    public ResponseEntity<Map<String, Object>> getEfficiency() {
        return ResponseEntity.ok(Map.of(
            "energySaved", "15%",
            "autoActions", "47",
            "comfortLevel", "92%",
            "monthlySavings", "1200 руб."
        ));
    }
}