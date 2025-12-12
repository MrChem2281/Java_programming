package com.example.demo.controller;

import com.example.demo.dto.CsvImportResponse;
import com.example.demo.service.CsvService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/csv")
@RequiredArgsConstructor
@Tag(name = "CSV Импорт", description = "Импорт и экспорт данных в CSV формате")
public class CsvController {
    
    private final CsvService csvService;
    
    @Operation(
        summary = "Импорт устройств из CSV",
        description = "Загрузка CSV файла с устройствами для импорта в систему"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Импорт успешно завершен",
                     content = @Content(mediaType = "application/json",
                     schema = @Schema(implementation = CsvImportResponse.class))),
        @ApiResponse(responseCode = "400", description = "Ошибка в формате файла или данных"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация")
    })
    @PostMapping(value = "/import/devices", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importDevices(
            @Parameter(description = "CSV файл с устройствами", 
                       required = true,
                       content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Проверяем тип файла
            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Поддерживаются только CSV файлы"));
            }
            
            log.info("Начало импорта CSV файла: {}", file.getOriginalFilename());
            
            CsvImportResponse response = csvService.importDevicesFromCsv(file);
            
            if (response.isSuccess()) {
                log.info("Импорт успешно завершен: {} устройств", response.getImportedCount());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Импорт завершен с ошибками: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Ошибка при импорте CSV: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ошибка обработки файла: " + e.getMessage()));
        }
    }
    
    @Operation(
        summary = "Скачать шаблон CSV",
        description = "Получить шаблон CSV файла для импорта устройств"
    )
    @GetMapping("/export/devices")
    public ResponseEntity<?> exportDevicesTemplate() {
        try {
            // Генерируем шаблон CSV для импорта устройств
            String csvTemplate = generateDevicesCsvTemplate();
            
            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .header("Content-Disposition", "attachment; filename=devices_template.csv")
                    .body(csvTemplate);
            
        } catch (Exception e) {
            log.error("Ошибка генерации шаблона CSV: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ошибка генерации шаблона"));
        }
    }
    
    private String generateDevicesCsvTemplate() {
        StringBuilder csv = new StringBuilder();
        
        // Заголовки с русскими названиями
        csv.append("Название устройства;Тип устройства;ID устройства;Комната;Тип комнаты;Начальное состояние;Начальное значение\n");
        
        // Примеры устройств
        csv.append("Термометр в гостиной;ТЕРМОМЕТР;sensor_temp_001;Гостиная;LIVING_ROOM;online;22.5\n");
        csv.append("Основной свет;ЛАМПОЧКА;light_main_001;Гостиная;LIVING_ROOM;off;0\n");
        csv.append("Кондиционер;КОНДИЦИОНЕР;ac_bedroom_001;Спальня;BEDROOM;off;0\n");
        csv.append("Датчик движения;ДАТЧИК_ДВИЖЕНИЯ;sensor_motion_001;Коридор;LIVING_ROOM;online;0\n");
        csv.append("Телевизор;ТЕЛЕВИЗОР;tv_living_001;Гостиная;LIVING_ROOM;off;0\n");
        
        // Пояснения
        csv.append("\n");
        csv.append("ПРИМЕЧАНИЯ:\n");
        csv.append("1. Типы устройств: ТЕРМОМЕТР, ГИГРОМЕТР, ДАТЧИК_ОСВЕЩЕННОСТИ, ДАТЧИК_ДВИЖЕНИЯ, ЛАМПОЧКА, КОНДИЦИОНЕР, ОБОГРЕВАТЕЛЬ, ТЕЛЕВИЗОР, УВЛАЖНИТЕЛЬ, ОСУШИТЕЛЬ, КОФЕВАРКА\n");
        csv.append("2. Типы комнат: LIVING_ROOM, BEDROOM, KITCHEN, BATHROOM, STUDY\n");
        csv.append("3. Начальное состояние: online/offline или включен/выключен\n");
        csv.append("4. Разделитель - точка с запятой (;)\n");
        csv.append("5. Кодировка - UTF-8\n");
        
        return csv.toString();
    }
}