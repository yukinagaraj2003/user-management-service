package com.yukeshkumar.user_management_service.mapper;

import com.yukeshkumar.user_management_service.entity.RoleType;
import com.yukeshkumar.user_management_service.entity.UserEntity;
import com.yukeshkumar.user_management_service.model.RegisterRequest;
import com.yukeshkumar.user_management_service.model.UpdateRequest;
import com.yukeshkumar.user_management_service.model.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserMapperTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userMapper = new UserMapper(passwordEncoder);
    }

    @Test
    void testConvertDtoToEntity() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("rawpassword");
        request.setEmail("test@example.com");
        request.setFull_name("Test User");
        request.setRole(RoleType.ROLE_USER);

        when(passwordEncoder.encode("rawpassword")).thenReturn("encodedpassword");

        UserEntity result = userMapper.convertDtoToEntity(request);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedpassword", result.getPassword());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFull_name());
        assertEquals(RoleType.ROLE_USER, result.getRole());
    }

    @Test
    void testConvertDtoFromEntity() {
        UserEntity entity = new UserEntity();
        UUID id = UUID.randomUUID();
        entity.setId(id);
        entity.setUsername("testuser");
        entity.setPassword("encodedpassword");
        entity.setEmail("test@example.com");
        entity.setFull_name("Test User");
        entity.setRole(RoleType.ROLE_USER);

        RegisterRequest result = userMapper.convertDtoFromEntity(entity);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedpassword", result.getPassword());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFull_name());
        assertEquals(RoleType.ROLE_USER, result.getRole());
    }

    @Test
    void testConvertEntityToDto() {
        UserEntity entity = new UserEntity();
        UUID id = UUID.randomUUID();
        entity.setId(id);
        entity.setUsername("testuser");
        entity.setPassword("encodedpassword");
        entity.setEmail("test@example.com");
        entity.setFull_name("Test User");
        entity.setRole(RoleType.ROLE_USER);

        UpdateRequest result = userMapper.convertEntityToDto(entity);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedpassword", result.getPassword());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFull_name());
        assertEquals(RoleType.ROLE_USER, result.getRole());
    }

    @Test
    void testConvertEntityToUserResponse() {
        UserEntity entity = new UserEntity();
        UUID id = UUID.randomUUID();
        entity.setId(id);
        entity.setUsername("testuser");
        entity.setEmail("test@example.com");
        entity.setFull_name("Test User");
        entity.setRole(RoleType.ROLE_USER);

        UserResponse result = userMapper.convertEntityToUserResponse(entity);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFull_name());
        assertEquals(RoleType.ROLE_USER, result.getRole());
    }
}
