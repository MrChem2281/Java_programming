package com.example.demo.dto;

import lombok.Data;

@Data
public class DeviceDataRequest {
    private String deviceId;
    private Double value;
    private String dataType;
}