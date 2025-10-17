package com.example.demo.service;

import java.util.List;

import javax.management.relation.RoleNotFoundException;

import org.springframework.stereotype.Service;

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

    public List<UserDto> getUsers(){
        return userRepository.findAll().stream().map(UserMapper::userToUserDto).toList();
    }
    public User getByUsername(String username){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("user not found with username" + username));
        
        return user;
    }
}
