package com.example.demo.service;

import com.example.demo.model.ModeSettings;
import com.example.demo.repository.ModeSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ModeService {
    
    private final ModeSettingsRepository modeSettingsRepository;
    private final AutomationService automationService;
    
    public void applyMode(String modeName) {
        ModeSettings settings = modeSettingsRepository.findByModeName(modeName);
        if (settings == null) {
            settings = createMode(modeName);
        }
        
        // Применяем настройки режима
        switch (modeName) {
            case "eco":
                applyEcoMode(settings);
                break;
            case "comfort":
                applyComfortMode(settings);
                break;
            case "party":
                applyPartyMode(settings);
                break;
            default:
                applyAutoMode(settings);
        }
    }
    
    private void applyEcoMode(ModeSettings settings) {
        // Экономим энергию
        settings.setTargetTemperature(20.0);
        settings.setAutoLightControl(false);
        settings.setAutoEntertainment(false);
        modeSettingsRepository.save(settings);
    }
    
    private void applyComfortMode(ModeSettings settings) {
        // Максимальный комфорт
        settings.setTargetTemperature(24.0);
        settings.setAutoLightControl(true);
        settings.setAutoEntertainment(true);
        modeSettingsRepository.save(settings);
    }
    
    private void applyPartyMode(ModeSettings settings) {
        // Вечеринка - яркий свет, музыка
        settings.setTargetTemperature(22.0);
        settings.setAutoLightControl(true);
        settings.setAutoEntertainment(true);
        modeSettingsRepository.save(settings);
    }
    
    private void applyAutoMode(ModeSettings settings) {
        // Стандартная автоматизация
        settings.setTargetTemperature(22.0);
        settings.setAutoLightControl(true);
        settings.setAutoEntertainment(false);
        modeSettingsRepository.save(settings);
    }
    
    private ModeSettings createMode(String modeName) {
        ModeSettings mode = new ModeSettings();
        mode.setModeName(modeName);
        return modeSettingsRepository.save(mode);
    }
}