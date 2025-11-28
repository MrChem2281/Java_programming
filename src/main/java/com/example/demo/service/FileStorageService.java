package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {
    
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    
    public String storeFile(MultipartFile file) {
        try {
            // Создаем директорию если не существует
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Проверяем тип файла (опционально)
            String contentType = file.getContentType();
            if (contentType == null) {
                throw new RuntimeException("Не удалось определить тип файла");
            }
            
            // Генерируем уникальное имя файла
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            
            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(fileName);
            
            // Сохраняем файл
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String fileUrl = "/uploads/" + fileName;
            log.info("Файл сохранен: {}", filePath);
            
            return fileUrl;
            
        } catch (IOException e) {
            log.error("Ошибка при сохранении файла: {}", e.getMessage());
            throw new RuntimeException("Не удалось сохранить файл: " + e.getMessage());
        }
    }
}