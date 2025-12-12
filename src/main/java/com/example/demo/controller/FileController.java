package com.example.demo.controller;

import com.example.demo.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Файлы", description = "Загрузка и скачивание файлов")
public class FileController {
    
    private final FileStorageService fileStorageService;
    
    @Operation(
        summary = "Загрузить файл",
        description = "Загрузка любого файла на сервер"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Файл успешно загружен"),
        @ApiResponse(responseCode = "400", description = "Ошибка при загрузке файла"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @Parameter(description = "Файл для загрузки", 
                       required = true,
                       content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) {
        
        try {
            String fileUrl = fileStorageService.storeFile(file);
            return ResponseEntity.ok(Map.of(
                "message", "Файл успешно загружен",
                "fileUrl", fileUrl,
                "fileName", file.getOriginalFilename(),
                "fileSize", file.getSize()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @Operation(
        summary = "Скачать файл",
        description = "Скачать файл по имени"
    )
    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Имя файла", required = true)
            @PathVariable String filename) {
        
        try {
            Path filePath = Paths.get(fileStorageService.getUploadDir()).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            String contentType = "application/octet-stream";
            if (filename.toLowerCase().endsWith(".csv")) {
                contentType = "text/csv";
            } else if (filename.toLowerCase().endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (filename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);
            
        } catch (MalformedURLException e) {
            log.error("Ошибка при скачивании файла: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}