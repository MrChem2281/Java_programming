package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthReportDto {
    private LocalDateTime timestamp;
    private String status;
    private int totalDevices;
    private int onlineDevices;
    private double onlinePercentage;
    private String currentMode;
    private int totalRooms;
    private Map<String, Integer> devicesByType;
    private int offlineDevicesCount;
    private String energySaving;
}