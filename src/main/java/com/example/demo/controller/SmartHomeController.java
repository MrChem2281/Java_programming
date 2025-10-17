package com.example.demo.controller;

import com.example.demo.model.ModeSettings;
import com.example.demo.model.RoomStatus;
import com.example.demo.service.SmartHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SmartHomeController {
    
    private final SmartHomeService smartHomeService;
    
    @PutMapping("/mode/settings")
    public ResponseEntity<ModeSettings> updateModeSettings(@RequestBody ModeSettings settings) {
        ModeSettings updatedSettings = smartHomeService.updateModeSettings(settings);
        return ResponseEntity.ok(updatedSettings);
    }
    
    @GetMapping("/roomstatus")
    public ResponseEntity<RoomStatus> getRoomStatus(@RequestParam Long roomId) {
        RoomStatus status = smartHomeService.getRoomStatus(roomId);
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/mode/current")
    public ResponseEntity<ModeSettings> getCurrentMode() {
        ModeSettings currentMode = smartHomeService.getCurrentMode();
        return ResponseEntity.ok(currentMode);
    }
}