package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CsvImportResponse {
    private boolean success;
    private String message;
    private int importedCount;
    private int failedCount;
    private List<String> errors;
    private List<String> successMessages;
}