package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModeSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String modeName; // "auto", "manual", "eco", "comfort"
    
    // Настройки температуры
    private Double targetTemperature;
    private Double temperatureThreshold;
    
    // Настройки освещения
    private Integer targetLightLevel;
    private boolean autoLightControl;
    
    // Настройки развлечений
    private boolean autoEntertainment;
    private String preferredMusicGenre;
    
    // Расписание
    private String schedule; // JSON с расписанием
    
    // Конструктор для быстрого создания
    public ModeSettings(String modeName, Double targetTemperature, boolean autoLightControl) {
        this.modeName = modeName;
        this.targetTemperature = targetTemperature;
        this.autoLightControl = autoLightControl;
        this.temperatureThreshold = 1.0;
        this.autoEntertainment = false;
    }
}