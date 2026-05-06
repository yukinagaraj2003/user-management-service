package com.yukeshkumar.user_management_service.entity;

import com.yukeshkumar.user_management_service.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SystemAdmin {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SystemAdmin(UserRepository userRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void createAdmin() {

        if (userRepository.findByUsername("admin").isEmpty()) {

            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setFull_name("ADMIN");
            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRole(RoleType.ROLE_ADMIN);

            userRepository.save(admin);

            System.out.println("✅ ADMIN CREATED");
        }
    }
}