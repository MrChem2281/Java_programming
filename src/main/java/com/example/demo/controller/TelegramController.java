package com.example.demo.controller;

import com.example.demo.dto.HealthReportDto;
import com.example.demo.service.HealthReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
public class TelegramController {
    
    private final HealthReportService healthReportService;
    
    @GetMapping("/report")
    public ResponseEntity<HealthReportDto> getHealthReport() {
        return ResponseEntity.ok(healthReportService.getReportJson());
    }
    
    @PostMapping("/send-test")
    public ResponseEntity<?> sendTestReport() {
        try {
            String report = healthReportService.generateHealthReport();
            // Здесь можно добавить отправку через бота если нужно
            return ResponseEntity.ok(Map.of(
                "message", "Report generated successfully",
                "report", report.substring(0, Math.min(100, report.length())) + "..."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}