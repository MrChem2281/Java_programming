package com.example.demo.model;

import lombok.Data;
import java.util.Map;
import java.util.HashMap;

@Data
public class RoomStatus {
    private Long roomId;
    private String roomName;
    private Map<String, Object> deviceStatus = new HashMap<>();
    private Map<String, Double> sensorData = new HashMap<>();
    private String currentMode; 
    private boolean isOccupied;
}