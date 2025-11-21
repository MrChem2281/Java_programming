package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartHomeService {
    
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceDataRepository deviceDataRepository;
    private final ModeSettingsRepository modeSettingsRepository;
    private final AutomationService automationService;
    
    public RoomStatus getRoomStatus(Long roomId) {
        log.debug("Getting room status for roomId: {}", roomId);
        
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> {
                log.error("Room not found with id: {}", roomId);
                return new RuntimeException("Room not found");
            });
        
        RoomStatus status = new RoomStatus();
        status.setRoomId(roomId);
        status.setRoomName(room.getName());
        
        List<Device> devices = deviceRepository.findByRoomId(roomId);
        log.debug("Found {} devices for room: {}", devices.size(), room.getName());
        
        for (Device device : devices) {
            if (device.getType().name().contains("SENSOR")) {
                DeviceData latestData = deviceDataRepository.findTopByDeviceIdOrderByTimestampDesc(device.getId());
                if (latestData != null) {
                    status.getSensorData().put(device.getType().name(), latestData.getValue());
                    log.trace("Sensor data for {}: {}", device.getName(), latestData.getValue());
                }
            }
            
            Map<String, Object> deviceStatus = new HashMap<>();
            deviceStatus.put("online", device.isOnline());
            deviceStatus.put("lastValue", device.getLastValue());
            deviceStatus.put("type", device.getType());
            
            status.getDeviceStatus().put(device.getName(), deviceStatus);
        }
        
        status.setOccupied(detectRoomOccupancy(status));
        status.setCurrentMode(getCurrentMode().getModeName());
        
        log.info("Room status retrieved for {}: {} devices, occupied: {}", 
                 room.getName(), devices.size(), status.isOccupied());
        return status;
    }
    
    public void processDeviceData(String deviceId, Double value, String dataType) {
        log.info("Processing device data - Device: {}, Value: {}, Type: {}", deviceId, value, dataType);
        
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            log.error("Device not found: {}", deviceId);
            throw new RuntimeException("Device not found: " + deviceId);
        }
        
        DeviceData deviceData = new DeviceData();
        deviceData.setDevice(device);
        deviceData.setValue(value);
        deviceData.setDataType(dataType);
        deviceData.setTimestamp(LocalDateTime.now());
        deviceDataRepository.save(deviceData);
        
        device.setLastValue(value);
        device.setOnline(true);
        deviceRepository.save(device);
        
        log.debug("Device data saved and automation triggered for: {}", device.getName());
        automationService.processAutomation(device, value);
    }
    
    public ModeSettings updateModeSettings(ModeSettings settings) {
        log.info("Updating mode settings to: {}", settings.getModeName());
        ModeSettings updated = modeSettingsRepository.save(settings);
        log.debug("Mode settings updated: {}", updated);
        return updated;
    }
    
    public ModeSettings getCurrentMode() {
        List<ModeSettings> allModes = modeSettingsRepository.findAll();
        if (!allModes.isEmpty()) {
            log.debug("Current mode: {}", allModes.get(0).getModeName());
            return allModes.get(0);
        }
        
        log.info("No modes found, creating default mode");
        return createDefaultMode();
    }
    
    private ModeSettings createDefaultMode() {
        ModeSettings defaultMode = new ModeSettings();
        defaultMode.setModeName("auto");
        defaultMode.setTargetTemperature(22.0);
        defaultMode.setTemperatureThreshold(1.0);
        defaultMode.setAutoLightControl(true);
        defaultMode.setAutoEntertainment(false);
        defaultMode.setTargetLightLevel(300);
        
        log.info("Created default mode: auto");
        return modeSettingsRepository.save(defaultMode);
    }
    
    private boolean detectRoomOccupancy(RoomStatus status) {
        Double lightLevel = status.getSensorData().get("LIGHT_SENSOR");
        boolean occupied = lightLevel != null && lightLevel < 50;
        log.trace("Room occupancy detection - lightLevel: {}, occupied: {}", lightLevel, occupied);
        return occupied;
    }

    public Map<String, Object> getAutomationStats() {
        log.debug("Generating automation statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        long totalDevices = deviceRepository.count();
        long onlineDevices = deviceRepository.findAll().stream()
                .filter(Device::isOnline)
                .count();
        
        stats.put("totalDevices", totalDevices);
        stats.put("onlineDevices", onlineDevices);
        stats.put("onlinePercentage", (onlineDevices * 100) / totalDevices);
        
        List<Device> activeDevices = deviceRepository.findAll().stream()
                .filter(d -> d.getLastValue() != null && d.getLastValue() > 0)
                .collect(Collectors.toList());
        
        stats.put("activeDevices", activeDevices.size());
        stats.put("energySaving", calculateEnergySaving());
        
        log.info("Automation stats - Total: {}, Online: {}, Active: {}", 
                 totalDevices, onlineDevices, activeDevices.size());
        return stats;
    }

    private String calculateEnergySaving() {
        return "15%";
    }
}