package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.ModeSettings;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.repository.ModeSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
            return;
        }
        
        // Основная логика автоматизации
        switch (triggeredDevice.getType()) {
            case TEMPERATURE_SENSOR:
                controlTemperature(value, currentMode);
                break;
            case HUMIDITY_SENSOR:
                controlHumidity(value);
                break;
            case LIGHT_SENSOR:
                controlLighting(value, currentMode);
                break;
            case MOTION_SENSOR:
                controlMotion(value);
                break;
        }
        
        // Дополнительная логика по времени суток
        applyTimeBasedAutomation(currentMode);
    }
    
    private void controlTemperature(Double currentTemp, ModeSettings settings) {
        Double targetTemp = settings.getTargetTemperature();
        Double threshold = settings.getTemperatureThreshold();
        
        List<Device> acDevices = getDevicesByType(Device.DeviceType.AIR_CONDITIONER);
        List<Device> heaterDevices = getDevicesByType(Device.DeviceType.HEATER);
        
        if (currentTemp > targetTemp + threshold) {
            // Жарко - включаем кондиционер, выключаем обогреватель
            sendCommandToDevices(acDevices, "COOL", 1.0);
            sendCommandToDevices(heaterDevices, "OFF", 0.0);
        } else if (currentTemp < targetTemp - threshold) {
            // Холодно - включаем обогреватель, выключаем кондиционер
            sendCommandToDevices(heaterDevices, "HEAT", 1.0);
            sendCommandToDevices(acDevices, "OFF", 0.0);
        } else {
            // Комфортная температура - выключаем всё
            sendCommandToDevices(acDevices, "OFF", 0.0);
            sendCommandToDevices(heaterDevices, "OFF", 0.0);
        }
    }
    
    private void controlHumidity(Double humidity) {
        if (humidity > 70) {
            // Высокая влажность - включаем осушитель
            List<Device> dehumidifiers = getDevicesByName("осушитель");
            sendCommandToDevices(dehumidifiers, "ON", 1.0);
        } else if (humidity < 30) {
            // Низкая влажность - включаем увлажнитель
            List<Device> humidifiers = getDevicesByName("увлажнитель");
            sendCommandToDevices(humidifiers, "ON", 1.0);
        }
    }
    
    private void controlLighting(Double lightLevel, ModeSettings settings) {
        if (!settings.isAutoLightControl()) return;
        
        List<Device> lights = getDevicesByType(Device.DeviceType.LIGHT);
        
        // Если слишком темно и вечернее время - включаем свет
        LocalTime now = LocalTime.now();
        boolean isEvening = now.isAfter(LocalTime.of(18, 0)) || now.isBefore(LocalTime.of(6, 0));
        
        if (lightLevel < 50 && isEvening) {
            sendCommandToDevices(lights, "ON", 0.7); // 70% яркости
        } else if (lightLevel > 200 || !isEvening) {
            sendCommandToDevices(lights, "OFF", 0.0);
        }
    }
    
    private void controlMotion(Double motionValue) {
        // motionValue = 1 - движение detected, 0 - нет движения
        if (motionValue == 1.0) {
            // Обнаружено движение - включаем свет в коридоре
            List<Device> corridorLights = getDevicesByRoom("Коридор");
            sendCommandToDevices(corridorLights, "ON", 0.5);
        }
    }
    
    private void applyTimeBasedAutomation(ModeSettings settings) {
        LocalTime now = LocalTime.now();
        
        // Ночной режим (23:00 - 07:00)
        if (now.isAfter(LocalTime.of(23, 0)) || now.isBefore(LocalTime.of(7, 0))) {
            applyNightMode();
        }
        
        // Утренний режим (07:00 - 09:00)
        else if (now.isAfter(LocalTime.of(7, 0)) && now.isBefore(LocalTime.of(9, 0))) {
            applyMorningMode();
        }
        
        // Режим дня когда никого нет (09:00 - 17:00)
        else if (now.isAfter(LocalTime.of(9, 0)) && now.isBefore(LocalTime.of(17, 0))) {
            applyAwayMode();
        }
    }
    
    private void applyNightMode() {
        // Выключаем весь свет кроме ночника
        List<Device> mainLights = getDevicesByType(Device.DeviceType.LIGHT);
        Device nightLight = getDeviceByName("Ночник");
        
        sendCommandToDevices(mainLights, "OFF", 0.0);
        if (nightLight != null) {
            sendCommand(nightLight.getDeviceId(), "ON", 0.2); // 20% яркости
        }
        
        // Устанавливаем комфортную температуру для сна
        List<Device> acDevices = getDevicesByType(Device.DeviceType.AIR_CONDITIONER);
        sendCommandToDevices(acDevices, "AUTO", 0.5);
    }
    
    private void applyMorningMode() {
        // Плавно включаем свет, готовим кофе
        List<Device> bedroomLights = getDevicesByRoom("Спальня");
        sendCommandToDevices(bedroomLights, "ON", 0.3);
        
        Device coffeeMaker = getDeviceByName("Кофеварка");
        if (coffeeMaker != null) {
            sendCommand(coffeeMaker.getDeviceId(), "ON", 1.0);
        }
    }
    
    private void applyAwayMode() {
        // Экономим энергию когда никого нет дома
        List<Device> lights = getDevicesByType(Device.DeviceType.LIGHT);
        sendCommandToDevices(lights, "OFF", 0.0);
        
        // Устанавливаем экономную температуру
        List<Device> acDevices = getDevicesByType(Device.DeviceType.AIR_CONDITIONER);
        sendCommandToDevices(acDevices, "ECO", 0.3);
    }
    
    // Вспомогательные методы
    private List<Device> getDevicesByType(Device.DeviceType type) {
        return deviceRepository.findAll().stream()
                .filter(d -> d.getType() == type)
                .collect(Collectors.toList());
    }
    
    private List<Device> getDevicesByName(String name) {
        return deviceRepository.findAll().stream()
                .filter(d -> d.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    private List<Device> getDevicesByRoom(String roomName) {
        return deviceRepository.findAll().stream()
                .filter(d -> d.getRoom() != null && roomName.equals(d.getRoom().getName()))
                .collect(Collectors.toList());
    }
    
    private Device getDeviceByName(String name) {
        return deviceRepository.findAll().stream()
                .filter(d -> name.equals(d.getName()))
                .findFirst()
                .orElse(null);
    }
    
    private void sendCommandToDevices(List<Device> devices, String command, Double value) {
        devices.forEach(device -> sendCommand(device.getDeviceId(), command, value));
    }
    
    private void sendCommand(String deviceId, String command, Double value) {
        System.out.println("🚀 [" + LocalDateTime.now() + "] Command to " + deviceId + ": " + command + " value: " + value);
        
        Device device = deviceRepository.findByDeviceId(deviceId);
        if (device != null) {
            device.setLastValue(value);
            deviceRepository.save(device);
        }
    }
}