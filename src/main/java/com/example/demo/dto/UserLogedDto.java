package com.example.demo.dto;

import java.util.Set;

public record UserLogedDto(String username, String role, Set<String> permissions) {

}
