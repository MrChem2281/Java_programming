package com.example.demo;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class DemoApplication implements ApplicationRunner {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    

    @Override
    public void run(ApplicationArguments args) throws Exception{
        log.info("Starting application initialization...");
        
        // Создаем базовые разрешения
        if (permissionRepository.count() == 0){
            Permission readPermission = new Permission("device", "read");
            Permission writePermission = new Permission("device", "write");
            Permission userReadPermission = new Permission("user", "read");
            
            permissionRepository.save(readPermission);
            permissionRepository.save(writePermission);
            permissionRepository.save(userReadPermission);
            
            log.info("Created default permissions: device:read, device:write, user:read");
        } else {
            log.info("Permissions already exist, skipping creation");
        }
        
        // Создаем роль пользователя
        if (roleRepository.count() == 0){
            Role userRole = new Role();
            userRole.setName("User");
            
            // Получаем созданные разрешения
            Permission deviceRead = permissionRepository.findByResourceAndOperation("device", "read");
            Permission userRead = permissionRepository.findByResourceAndOperation("user", "read");
            
            userRole.setPermissions(new HashSet<>(Set.of(deviceRead, userRead)));
            roleRepository.save(userRole);
            
            log.info("Created User role with permissions");
        } else {
            log.info("Roles already exist, skipping creation");
        }
        
        // Создаем тестового пользователя
        if(userRepository.count() == 0){
            Role userRole = roleRepository.findByName("User");
            if (userRole != null) {
                User testUser = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user"))
                    .role(userRole)
                    .build();
                userRepository.save(testUser);
                log.info("Created test user: username='user', password='user'");
            } else {
                log.error("User role not found, cannot create test user");
            }
        } else {
            log.info("Users already exist, skipping creation");
        }
        
        log.info("Application initialization completed successfully");
    } 

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}