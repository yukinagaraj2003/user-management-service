package com.yukeshkumar.user_management_service.service;

import com.yukeshkumar.user_management_service.entity.RoleType;
import com.yukeshkumar.user_management_service.entity.UserEntity;
import com.yukeshkumar.user_management_service.exception.UserAlreadyExistsException;
import com.yukeshkumar.user_management_service.mapper.UserMapper;
import com.yukeshkumar.user_management_service.model.*;
import com.yukeshkumar.user_management_service.repository.UserRepository;
import com.yukeshkumar.user_management_service.security.JwtUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtility jwtUtility;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private UUID creatorId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        creatorId = UUID.randomUUID();
    }

    @Test
    void testCreateUser_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password");
        request.setEmail("new@example.com");
        request.setFull_name("New User");
        request.setRole(RoleType.ROLE_USER);

        UserEntity creatorEntity = new UserEntity();
        creatorEntity.setId(creatorId);
        creatorEntity.setRole(RoleType.ROLE_SUPER_ADMIN);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());
        userEntity.setUsername("newuser");

        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setUsername("newuser");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creatorEntity));
        when(userMapper.convertDtoToEntity(request)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.convertEntityToUserResponse(userEntity)).thenReturn(expectedResponse);


        UserResponse result = userServiceImpl.createUser(request, creatorId);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userRepository).findByUsername("newuser");
        verify(userRepository).save(userEntity);
    }

    @Test
    void testCreateUser_UserAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");

        UserEntity existingUser = new UserEntity();
        existingUser.setUsername("existinguser");

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));


        assertThrows(UserAlreadyExistsException.class, () -> userServiceImpl.createUser(request, creatorId));
        verify(userRepository).findByUsername("existinguser");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        UUID userId = UUID.randomUUID();
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                () -> "ROLE_USER"
        );
        CustomUserDetails userDetails = new CustomUserDetails(userId, "testuser", "password", authorities);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(jwtUtility.generateToken(userId, "ROLE_USER")).thenReturn("jwt-token");

        String token = userServiceImpl.login(request);

        assertEquals("jwt-token", token);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
     
        verify(jwtUtility).generateToken(userId, "ROLE_USER");
    }

}
