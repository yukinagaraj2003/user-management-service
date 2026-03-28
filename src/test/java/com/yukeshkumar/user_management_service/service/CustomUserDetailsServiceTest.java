package com.yukeshkumar.user_management_service.service;

import com.yukeshkumar.user_management_service.entity.RoleType;
import com.yukeshkumar.user_management_service.entity.UserEntity;
import com.yukeshkumar.user_management_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_Success() {
        String username = "testuser";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setUsername(username);
        userEntity.setPassword("password");
        userEntity.setRole(RoleType.ROLE_USER);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("password", result.getPassword());
        assertTrue(result.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        verify(userRepository).findByUsername(username);
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        String username = "nonexistent";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(username));
        verify(userRepository).findByUsername(username);
    }

    @Test
    void testLoadUserById_Success() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setUsername("testuser");
        userEntity.setPassword("password");
        userEntity.setRole(RoleType.ROLE_ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        UserDetails result = customUserDetailsService.loadUserById(userId);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("password", result.getPassword());
        assertTrue(result.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        verify(userRepository).findById(userId);
    }

    @Test
    void testLoadUserById_UserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserById(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void testLoadUserByUsername_RoleWithoutPrefix() {
        String username = "testuser";
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setUsername(username);
        userEntity.setPassword("password");
        userEntity.setRole(RoleType.ROLE_USER); // Assuming RoleType.ROLE_USER already has ROLE_

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        assertNotNull(result);
        assertTrue(result.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }
}
