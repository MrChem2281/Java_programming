package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private DeviceType type;
    private String deviceId; // Уникальный ID IoT устройства
    
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    
    private boolean isOnline;
    private Double lastValue;
    
    public enum DeviceType {
        TEMPERATURE_SENSOR,
        HUMIDITY_SENSOR, 
        LIGHT,
        AIR_CONDITIONER,
        MUSIC_PLAYER,
        TV,
        CURTAIN
    }
}