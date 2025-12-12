package com.example.demo.service;

import com.example.demo.dto.CsvDeviceImportDto;
import com.example.demo.dto.CsvImportResponse;
import com.example.demo.model.Device;
import com.example.demo.model.Room;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.repository.RoomRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvService {
    
    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    
    public CsvImportResponse importDevicesFromCsv(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        List<String> successMessages = new ArrayList<>();
        int importedCount = 0;
        int failedCount = 0;
        
        try (Reader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            // Парсим CSV файл
            CsvToBean<CsvDeviceImportDto> csvToBean = new CsvToBeanBuilder<CsvDeviceImportDto>(reader)
                    .withType(CsvDeviceImportDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(';')
                    .build();
            
            List<CsvDeviceImportDto> devices = csvToBean.parse();
            
            if (devices.isEmpty()) {
                errors.add("CSV файл пустой или не содержит данных");
                return CsvImportResponse.builder()
                        .success(false)
                        .message("Файл пустой")
                        .importedCount(0)
                        .failedCount(0)
                        .errors(errors)
                        .build();
            }
            
            log.info("Начата обработка {} записей из CSV", devices.size());
            
            // Обрабатываем каждую запись
            for (int i = 0; i < devices.size(); i++) {
                CsvDeviceImportDto dto = devices.get(i);
                try {
                    processDeviceImport(dto, i + 1);
                    importedCount++;
                    successMessages.add(String.format("Устройство '%s' успешно импортировано", dto.getName()));
                } catch (Exception e) {
                    failedCount++;
                    errors.add(String.format("Строка %d: %s - %s", 
                            i + 1, dto.getName(), e.getMessage()));
                    log.error("Ошибка импорта устройства: {}", e.getMessage());
                }
            }
            
            String message = String.format("Импорт завершен. Успешно: %d, Ошибок: %d", 
                    importedCount, failedCount);
            
            return CsvImportResponse.builder()
                    .success(failedCount == 0)
                    .message(message)
                    .importedCount(importedCount)
                    .failedCount(failedCount)
                    .errors(errors)
                    .successMessages(successMessages)
                    .build();
            
        } catch (Exception e) {
            log.error("Ошибка обработки CSV файла: {}", e.getMessage());
            errors.add("Ошибка чтения файла: " + e.getMessage());
            
            return CsvImportResponse.builder()
                    .success(false)
                    .message("Ошибка обработки файла")
                    .importedCount(0)
                    .failedCount(0)
                    .errors(errors)
                    .build();
        }
    }
    
    private void processDeviceImport(CsvDeviceImportDto dto, int lineNumber) {
        // Валидация обязательных полей
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Не указано название устройства");
        }
        
        if (dto.getDeviceId() == null || dto.getDeviceId().trim().isEmpty()) {
            throw new IllegalArgumentException("Не указан ID устройства");
        }
        
        // Проверяем, нет ли уже устройства с таким ID
        if (deviceRepository.findByDeviceId(dto.getDeviceId()) != null) {
            throw new IllegalArgumentException("Устройство с таким ID уже существует");
        }
        
        Device device = new Device();
        device.setName(dto.getName().trim());
        device.setDeviceId(dto.getDeviceId().trim());
        
        // Парсим тип устройства
        Device.DeviceType deviceType = parseDeviceType(dto.getType());
        if (deviceType == null) {
            throw new IllegalArgumentException("Неизвестный тип устройства: " + dto.getType());
        }
        device.setType(deviceType);
        
        // Обрабатываем комнату если указана
        if (dto.getRoomName() != null && !dto.getRoomName().trim().isEmpty()) {
            Room room = processRoom(dto);
            device.setRoom(room);
        }
        
        // Начальное состояние
        if (dto.getInitialStatus() != null) {
            device.setOnline("online".equalsIgnoreCase(dto.getInitialStatus()) || 
                            "включен".equalsIgnoreCase(dto.getInitialStatus()));
        } else {
            device.setOnline(false);
        }
        
        device.setLastValue(dto.getInitialValue());
        
        deviceRepository.save(device);
        log.info("Импортировано устройство: {}", device.getName());
    }
    
    private Device.DeviceType parseDeviceType(String typeString) {
        if (typeString == null) return null;
        
        String normalized = typeString.trim().toUpperCase();
        
        switch (normalized) {
            case "ТЕРМОМЕТР":
            case "TEMPERATURE_SENSOR":
                return Device.DeviceType.TEMPERATURE_SENSOR;
                
            case "ГИГРОМЕТР":
            case "HUMIDITY_SENSOR":
                return Device.DeviceType.HUMIDITY_SENSOR;
                
            case "ДАТЧИК_ОСВЕЩЕННОСТИ":
            case "LIGHT_SENSOR":
                return Device.DeviceType.LIGHT_SENSOR;
                
            case "ДАТЧИК_ДВИЖЕНИЯ":
            case "MOTION_SENSOR":
                return Device.DeviceType.MOTION_SENSOR;
                
            case "ЛАМПОЧКА":
            case "LIGHT":
                return Device.DeviceType.LIGHT;
                
            case "КОНДИЦИОНЕР":
            case "AIR_CONDITIONER":
                return Device.DeviceType.AIR_CONDITIONER;
                
            case "ОБОГРЕВАТЕЛЬ":
            case "HEATER":
                return Device.DeviceType.HEATER;
                
            case "ТЕЛЕВИЗОР":
            case "TV":
                return Device.DeviceType.TV;
                
            case "УВЛАЖНИТЕЛЬ":
            case "HUMIDIFIER":
                return Device.DeviceType.HUMIDIFIER;
                
            case "ОСУШИТЕЛЬ":
            case "DEHUMIDIFIER":
                return Device.DeviceType.DEHUMIDIFIER;
                
            case "КОФЕВАРКА":
            case "COFFEE_MAKER":
                return Device.DeviceType.COFFEE_MAKER;
                
            default:
                return null;
        }
    }
    
    private Room processRoom(CsvDeviceImportDto dto) {
        // Ищем комнату по имени
        List<Room> existingRooms = roomRepository.findAll();
        Room room = existingRooms.stream()
                .filter(r -> r.getName().equalsIgnoreCase(dto.getRoomName().trim()))
                .findFirst()
                .orElse(null);
        
        // Если комната не найдена, создаем новую
        if (room == null) {
            room = new Room();
            room.setName(dto.getRoomName().trim());
            
            // Парсим тип комнаты если указан
            if (dto.getRoomType() != null && !dto.getRoomType().trim().isEmpty()) {
                try {
                    Room.RoomType roomType = Room.RoomType.valueOf(
                            dto.getRoomType().trim().toUpperCase());
                    room.setType(roomType);
                } catch (IllegalArgumentException e) {
                    room.setType(Room.RoomType.LIVING_ROOM); // Значение по умолчанию
                }
            } else {
                room.setType(Room.RoomType.LIVING_ROOM);
            }
            
            room = roomRepository.save(room);
            log.info("Создана новая комната: {}", room.getName());
        }
        
        return room;
    }
}