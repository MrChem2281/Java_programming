package com.example.demo.mapper;

import java.util.Collections;
import java.util.stream.Collectors;

import com.example.demo.dto.UserDto;
import com.example.demo.dto.UserLogedDto;
import com.example.demo.model.Permission;
import com.example.demo.model.User;

public class UserMapper{
    public static UserDto userToUserDto(User user) {
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            user.getRole().getAuthority(),
            user.getRole().getPermissions().stream().map(Permission::getAuthority).collect(Collectors.toSet()));
    }

    public static UserLogedDto userToUserLogedDto(User user) {
        return new UserLogedDto(
            user.getUsername(),
            user.getRole().getAuthority(),
            user.getRole().getPermissions().stream().map(Permission::getAuthority).collect(Collectors.toSet()));
    }
}