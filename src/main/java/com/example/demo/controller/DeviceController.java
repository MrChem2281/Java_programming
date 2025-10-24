package com.example.demo.controller;

import com.example.demo.dto.DeviceDataRequest;
import com.example.demo.service.SmartHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DeviceController {
    
    private final SmartHomeService smartHomeService;
    
    @PostMapping("/devices")
    public ResponseEntity<String> receiveDeviceData(@RequestBody DeviceDataRequest request) {
        try {
            smartHomeService.processDeviceData(
                request.getDeviceId(), 
                request.getValue(), 
                request.getDataType()
            );
            return ResponseEntity.ok("Data received successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/devices/command")
    public ResponseEntity<String> sendDeviceCommand(@RequestBody Map<String, Object> command) {
        String deviceId = (String) command.get("deviceId");
        String action = (String) command.get("action");
        Double value = (Double) command.get("value");
        
        System.out.println("Command to " + deviceId + ": " + action + " value: " + value);
        
        return ResponseEntity.ok("Command sent successfully");
    }
}