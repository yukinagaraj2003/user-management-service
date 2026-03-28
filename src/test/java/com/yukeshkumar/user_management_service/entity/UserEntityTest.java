package com.yukeshkumar.user_management_service.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    void testGenerateIdAndCreateAt_PrePersist() {
        UserEntity userEntity = new UserEntity();


        assertNull(userEntity.getId());
        assertNull(userEntity.getCreatedAt());


        userEntity.generateIdAndCreateAt();

        assertNotNull(userEntity.getId());
        assertNotNull(userEntity.getCreatedAt());
        assertTrue(userEntity.getCreatedAt().isBefore(Instant.now().plusMillis(100))); // Allow small time difference
    }

    @Test
    void testUpdatedTimeStamp_PreUpdate() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUpdatedAt(Instant.now().minusSeconds(60)); // Set old timestamp

        Instant oldUpdatedAt = userEntity.getUpdatedAt();

        // Simulate @PreUpdate
        userEntity.updatedTimeStamp();

        assertNotNull(userEntity.getUpdatedAt());
        assertTrue(userEntity.getUpdatedAt().isAfter(oldUpdatedAt));
        assertTrue(userEntity.getUpdatedAt().isBefore(Instant.now().plusMillis(100))); // Allow small time difference
    }

    @Test
    void testGettersAndSetters() {
        UserEntity userEntity = new UserEntity();
        UUID id = UUID.randomUUID();
        String username = "testuser";
        String password = "password";
        String email = "test@example.com";
        String fullName = "Test User";
        RoleType role = RoleType.ROLE_USER;
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();
        String updatedBy = "admin";

        userEntity.setId(id);
        userEntity.setUsername(username);
        userEntity.setPassword(password);
        userEntity.setEmail(email);
        userEntity.setFull_name(fullName);
        userEntity.setRole(role);
        userEntity.setCreatedAt(createdAt);
        userEntity.setUpdatedAt(updatedAt);
        userEntity.setUpdatedBy(updatedBy);

        assertEquals(id, userEntity.getId());
        assertEquals(username, userEntity.getUsername());
        assertEquals(password, userEntity.getPassword());
        assertEquals(email, userEntity.getEmail());
        assertEquals(fullName, userEntity.getFull_name());
        assertEquals(role, userEntity.getRole());
        assertEquals(createdAt, userEntity.getCreatedAt());
        assertEquals(updatedAt, userEntity.getUpdatedAt());
        assertEquals(updatedBy, userEntity.getUpdatedBy());
    }
}
