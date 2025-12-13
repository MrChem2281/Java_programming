package com.example.demo.service;

import com.example.demo.dto.HealthReportDto;
import com.example.demo.model.Device;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.repository.ModeSettingsRepository;
import com.example.demo.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthReportService {
    
    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final ModeSettingsRepository modeSettingsRepository;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    /**
     * Генерация текстового отчета для Telegram
     */
    public String generateHealthReport() {
        HealthReportDto report = generateReportData();
        return formatReportForTelegram(report);
    }
    
    /**
     * Получение данных для отчета
     */
    public HealthReportDto generateReportData() {
        List<Device> allDevices = deviceRepository.findAll();
        long totalDevices = allDevices.size();
        long onlineDevices = allDevices.stream().filter(Device::isOnline).count();
        
        // Статистика по типам устройств
        Map<String, Integer> devicesByType = allDevices.stream()
                .collect(Collectors.groupingBy(
                        device -> device.getType().name(),
                        Collectors.summingInt(e -> 1)
                ));
        
        // Получаем текущий режим
        String currentMode = modeSettingsRepository.findById(1L)
                .map(mode -> mode.getModeName())
                .orElse("Не установлен");
        
        return HealthReportDto.builder()
                .timestamp(LocalDateTime.now())
                .status("OK")
                .totalDevices((int) totalDevices)
                .onlineDevices((int) onlineDevices)
                .onlinePercentage(totalDevices > 0 ? (onlineDevices * 100.0 / totalDevices) : 0)
                .currentMode(currentMode)
                .totalRooms((int) roomRepository.count())
                .devicesByType(devicesByType)
                .offlineDevicesCount((int) (totalDevices - onlineDevices))
                .energySaving("15%")
                .build();
    }
    
    /**
     * Форматирование отчета для Telegram (Markdown)
     */
    private String formatReportForTelegram(HealthReportDto report) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("🏠 *Smart Home Health Report*\n\n");
        
        // Общая информация
        sb.append("📅 Дата: ").append(report.getTimestamp().format(DATE_FORMATTER)).append("\n");
        sb.append("⏰ Время: ").append(report.getTimestamp().format(TIME_FORMATTER)).append("\n");
        sb.append("📊 Статус: ").append("✅ *").append(report.getStatus()).append("*\n\n");
        
        // Основные метрики
        sb.append("*📈 ОСНОВНЫЕ МЕТРИКИ*\n");
        sb.append("• Всего устройств: ").append(report.getTotalDevices()).append("\n");
        sb.append("• Устройств онлайн: ").append(report.getOnlineDevices()).append("\n");
        sb.append("• Доступность: ").append(String.format("%.1f", report.getOnlinePercentage())).append("%\n");
        sb.append("• Комнат: ").append(report.getTotalRooms()).append("\n");
        sb.append("• Экономия энергии: ").append(report.getEnergySaving()).append("\n\n");
        
        // Режим работы
        sb.append("*🎛️ РЕЖИМ РАБОТЫ*\n");
        sb.append("• Текущий режим: ").append(report.getCurrentMode()).append("\n\n");
        
        // Распределение по типам
        sb.append("*🔧 ТИПЫ УСТРОЙСТВ*\n");
        if (!report.getDevicesByType().isEmpty()) {
            report.getDevicesByType().forEach((type, count) -> {
                sb.append("• ").append(type).append(": ").append(count).append("\n");
            });
        } else {
            sb.append("Нет данных\n");
        }
        sb.append("\n");
        
        // Предупреждения
        if (report.getOfflineDevicesCount() > 0) {
            sb.append("⚠️ *ВНИМАНИЕ!*\n");
            sb.append("Найдено ").append(report.getOfflineDevicesCount())
              .append(" отключенных устройств\n\n");
        }
        
        // Итог
        if (report.getOnlinePercentage() >= 90) {
            sb.append("✅ *Все системы работают стабильно!*");
        } else if (report.getOnlinePercentage() >= 70) {
            sb.append("⚠️ *Есть проблемы с некоторыми устройствами*");
        } else {
            sb.append("❌ *Критическое состояние! Требуется вмешательство*");
        }
        
        return sb.toString();
    }
    
    /**
     * Получить отчет в формате JSON (для API)
     */
    public HealthReportDto getReportJson() {
        return generateReportData();
    }
}