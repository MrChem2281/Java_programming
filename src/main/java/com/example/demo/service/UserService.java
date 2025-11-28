package com.example.demo.service;

import java.util.List;

import javax.management.relation.RoleNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.UserDto;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> getUsers(){
        return userRepository.findAll().stream().map(UserMapper::userToUserDto).toList();
    }
    
    public User getByUsername(String username){
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("user not found with username" + username));
        return user;
    }
    
    public void changePassword(ChangePasswordRequest request) {
        // Получаем текущего пользователя из SecurityContext
        String currentUsername = getCurrentUsername();
        User user = getByUsername(currentUsername);
        
        // Проверяем текущий пароль
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new RuntimeException("Текущий пароль неверен");
        }
        
        // Проверяем совпадение нового пароля и подтверждения
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new RuntimeException("Новый пароль и подтверждение не совпадают");
        }
        
        // Обновляем пароль
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
    
    private String getCurrentUsername() {
        // В реальном приложении получаем из SecurityContext
        // Здесь упрощенная реализация - нужно интегрировать с JWT
        return org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication().getName();
    }
}