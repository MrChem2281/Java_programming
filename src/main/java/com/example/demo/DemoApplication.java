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

@SpringBootApplication
@RequiredArgsConstructor
public class DemoApplication implements ApplicationRunner{

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

	private final PermissionRepository permissionRepository;
	private final RoleRepository roleRepository;
	

	@Override
	public void run(ApplicationArguments args) throws Exception{
		if (permissionRepository.count() == 0){
			permissionRepository.save(new Permission("blank","read"));
		}
		if (roleRepository.count() == 0){
			Role userRole = new Role();
			userRole.setName("User");
			userRole.setPermissions(new HashSet<>(Set.of(permissionRepository.findByResourceAndOperation("blank", "read"))));
			roleRepository.save(userRole);
		}
		if(userRepository.count() == 0){
			userRepository.save(User.builder().username("user")
			.password(passwordEncoder.encode("user"))
			.role(roleRepository.findByName("User")).build());
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
