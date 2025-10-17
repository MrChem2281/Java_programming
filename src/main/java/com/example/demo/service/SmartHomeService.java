package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SmartHomeService {
    
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceDataRepository deviceDataRepository;
    private final ModeSettingsRepository modeSettingsRepository;
    private final AutomationService automationService;
    
    public RoomStatus getRoomStatus(Long roomId) {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
        
        RoomStatus status = new RoomStatus();
        status.setRoomId(roomId);
        status.setRoomName(room.getName());
        
        // Получаем данные устройств
        List<Device> devices = deviceRepository.findByRoomId(roomId);
        
        for (Device device : devices) {
            // Данные сенсоров
            if (device.getType().name().contains("SENSOR")) {
                DeviceData latestData = deviceDataRepository.findTopByDeviceIdOrderByTimestampDesc(device.getId());
                if (latestData != null) {
                    status.getSensorData().put(device.getType().name(), latestData.getValue());
                }
            }
            
            // Статус устройств
            Map<String, Object> deviceStatus = new HashMap<>();
            deviceStatus.put("online", device.isOnline());
            deviceStatus.put("lastValue", device.getLastValue());
            deviceStatus.put("type", device.getType());
            
            status.getDeviceStatus().put(device.getName(), deviceStatus);
        }
        
        // Определяем занятость комнаты (по движению или другим сенсорам)
        status.setOccupied(detectRoomOccupancy(status));
        status.setCurrentMode(getCurrentMode().getModeName());
        
        return status;
    }
    
    public void processDeviceData(String deviceId, Double value, String dataType) {
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device == null) {
            throw new RuntimeException("Device not found: " + deviceId);
        }
        
        // Сохраняем данные
        DeviceData deviceData = new DeviceData();
        deviceData.setDevice(device);
        deviceData.setValue(value);
        deviceData.setDataType(dataType);
        deviceData.setTimestamp(LocalDateTime.now());
        deviceDataRepository.save(deviceData);
        
        // Обновляем устройство
        device.setLastValue(value);
        device.setOnline(true);
        deviceRepository.save(device);
        
        // Автоматизация
        automationService.processAutomation(device, value);
    }
    
    public ModeSettings updateModeSettings(ModeSettings settings) {
        return modeSettingsRepository.save(settings);
    }
    
    public ModeSettings getCurrentMode() {
        // Сначала пытаемся найти существующий режим
        List<ModeSettings> allModes = modeSettingsRepository.findAll();
        if (!allModes.isEmpty()) {
            return allModes.get(0); // Возвращаем первый найденный
        }
        
        // Если нет режимов, создаем дефолтный
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
        return modeSettingsRepository.save(defaultMode);
    }
    
    private boolean detectRoomOccupancy(RoomStatus status) {
        // Простая логика определения занятости
        // В реальной системе здесь будут данные с датчиков движения
        Double lightLevel = status.getSensorData().get("LIGHT_SENSOR");
        return lightLevel != null && lightLevel < 50; // Если темно, возможно кто-то есть
    }
}