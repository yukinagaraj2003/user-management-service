package com.yukeshkumar.user_management_service.service;

import com.yukeshkumar.user_management_service.entity.RoleType;
import com.yukeshkumar.user_management_service.entity.UserEntity;
import com.yukeshkumar.user_management_service.mapper.UserMapper;
import com.yukeshkumar.user_management_service.model.ResetPasswordRequest;
import com.yukeshkumar.user_management_service.model.UpdateRequest;
import com.yukeshkumar.user_management_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceClassTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceClass userServiceClass;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserById_Success() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setUsername("testuser");

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setId(userId);
        updateRequest.setUsername("testuser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.convertEntityToDto(userEntity)).thenReturn(updateRequest);

        UpdateRequest result = userServiceClass.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(userId);
        verify(userMapper).convertEntityToDto(userEntity);
    }

    @Test
    void testGetUserById_UserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userServiceClass.getUserById(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void testGetAllUser() {
        UserEntity user1 = new UserEntity();
        user1.setId(UUID.randomUUID());
        user1.setUsername("user1");

        UserEntity user2 = new UserEntity();
        user2.setId(UUID.randomUUID());
        user2.setUsername("user2");

        List<UserEntity> userEntities = Arrays.asList(user1, user2);

        UpdateRequest updateRequest1 = new UpdateRequest();
        updateRequest1.setUsername("user1");

        UpdateRequest updateRequest2 = new UpdateRequest();
        updateRequest2.setUsername("user2");

        when(userRepository.findAll()).thenReturn(userEntities);
        when(userMapper.convertEntityToDto(user1)).thenReturn(updateRequest1);
        when(userMapper.convertEntityToDto(user2)).thenReturn(updateRequest2);

        List<UpdateRequest> result = userServiceClass.getAllUser();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
        verify(userMapper, times(2)).convertEntityToDto(any(UserEntity.class));
    }

    @Test
    void testResetPassword_Success() {
        UUID userId = UUID.randomUUID();
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPassword("newpassword");

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setPassword("oldpassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedpassword");

        String result = userServiceClass.resetPassword(userId, request);

        assertEquals("password resetted", result);
        verify(userRepository).findById(userId);
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(userEntity);
        assertEquals("encodedpassword", userEntity.getPassword());
    }

    @Test
    void testResetPassword_UserNotFound() {
        UUID userId = UUID.randomUUID();
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPassword("newpassword");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userServiceClass.resetPassword(userId, request));
        verify(userRepository).findById(userId);
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testPageUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setUsername("testuser");

        Page<UserEntity> userEntityPage = new PageImpl<>(Arrays.asList(userEntity), pageable, 1);

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setUsername("testuser");

        when(userRepository.findAll(pageable)).thenReturn(userEntityPage);
        when(userMapper.convertEntityToDto(userEntity)).thenReturn(updateRequest);

        Page<UpdateRequest> result = userServiceClass.pageUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(pageable);
    }
}
