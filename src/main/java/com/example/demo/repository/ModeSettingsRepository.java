package com.example.demo.repository;

import com.example.demo.model.ModeSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModeSettingsRepository extends JpaRepository<ModeSettings, Long> {
    ModeSettings findByModeName(String modeName);
}