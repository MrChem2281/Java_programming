package com.example.demo.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class CsvDeviceImportDto {
    
    @CsvBindByName(column = "Название устройства", required = true)
    private String name;
    
    @CsvBindByName(column = "Тип устройства", required = true)
    private String type;
    
    @CsvBindByName(column = "ID устройства", required = true)
    private String deviceId;
    
    @CsvBindByName(column = "Комната")
    private String roomName;
    
    @CsvBindByName(column = "Тип комнаты")
    private String roomType;
    
    @CsvBindByName(column = "Начальное состояние")
    private String initialStatus;
    
    @CsvBindByName(column = "Начальное значение")
    private Double initialValue;
}