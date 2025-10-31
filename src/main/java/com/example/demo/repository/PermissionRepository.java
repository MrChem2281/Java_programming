package com.example.demo.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long>{
    Permission findByResourceAndOperation(String string, String string2);
}
