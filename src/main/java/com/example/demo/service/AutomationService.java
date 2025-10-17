package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.ModeSettings;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.repository.ModeSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutomationService {
    
    private final DeviceRepository deviceRepository;
    private final ModeSettingsRepository modeSettingsRepository;
    
    public void processAutomation(Device triggeredDevice, Double value) {
        ModeSettings currentMode = modeSettingsRepository.findById(1L).orElse(null);
        if (currentMode == null || !"auto".equals(currentMode.getModeName())) {
            return; // Автоматизация только в auto режиме
        }
        
        switch (triggeredDevice.getType()) {
            case TEMPERATURE_SENSOR:
                controlTemperature(value, currentMode);
                break;
            case HUMIDITY_SENSOR:
                controlHumidity(value);
                break;
            case LIGHT:
                // Логика для освещения
                break;
        }
    }
    
    private void controlTemperature(Double currentTemp, ModeSettings settings) {
        Double targetTemp = settings.getTargetTemperature();
        Double threshold = settings.getTemperatureThreshold() != null ? 
            settings.getTemperatureThreshold() : 1.0;
        
        List<Device> acDevices = deviceRepository.findAll().stream()
            .filter(d -> d.getType() == Device.DeviceType.AIR_CONDITIONER)
            .collect(Collectors.toList());
        
        for (Device ac : acDevices) {
            if (currentTemp > targetTemp + threshold) {
                // Включить охлаждение
                sendDeviceCommand(ac.getDeviceId(), "COOL", 1.0);
            } else if (currentTemp < targetTemp - threshold) {
                // Включить обогрев
                sendDeviceCommand(ac.getDeviceId(), "HEAT", 1.0);
            } else {
                // Выключить
                sendDeviceCommand(ac.getDeviceId(), "OFF", 0.0);
            }
        }
    }
    
    private void controlHumidity(Double humidity) {
        // Логика контроля влажности
        if (humidity > 70) {
            // Включить осушитель
            List<Device> dehumidifiers = deviceRepository.findAll().stream()
                .filter(d -> d.getName().toLowerCase().contains("dehumidifier"))
                .collect(Collectors.toList());
            
            for (Device device : dehumidifiers) {
                sendDeviceCommand(device.getDeviceId(), "ON", 1.0);
            }
        }
    }
    
    private void sendDeviceCommand(String deviceId, String command, Double value) {
        // В реальной системе здесь будет отправка команды на IoT устройство
        System.out.println("Sending command to " + deviceId + ": " + command + " value: " + value);
        
        // Обновляем статус устройства в базе
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device != null) {
            device.setLastValue(value);
            deviceRepository.save(device);
        }
    }
}