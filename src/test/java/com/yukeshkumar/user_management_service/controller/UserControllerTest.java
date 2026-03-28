package com.yukeshkumar.user_management_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukeshkumar.user_management_service.entity.RoleType;
import com.yukeshkumar.user_management_service.model.*;
import com.yukeshkumar.user_management_service.security.JwtAuthFilter;
import com.yukeshkumar.user_management_service.security.JwtUtility;
import com.yukeshkumar.user_management_service.service.CustomUserDetailsService;
import com.yukeshkumar.user_management_service.service.UserService;
import com.yukeshkumar.user_management_service.service.UserServiceClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("deprecation")
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("deprecation")
    @MockBean
    private UserService userService;

    @SuppressWarnings("deprecation")
    @MockBean
    private UserServiceClass userServiceClass;

    @SuppressWarnings("deprecation")
    @MockBean
    private JwtUtility jwtUtility;

    @SuppressWarnings("deprecation")
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @SuppressWarnings("deprecation")
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGetUserById() throws Exception {
        UUID userId = UUID.randomUUID();
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setId(userId);
        updateRequest.setUsername("testuser");
        updateRequest.setEmail("test@example.com");
        updateRequest.setFull_name("Test User");
        updateRequest.setRole(RoleType.ROLE_USER);

        when(userServiceClass.getUserById(userId)).thenReturn(updateRequest);

        mockMvc.perform(get("/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testGetAllUser() throws Exception {
        UpdateRequest user1 = new UpdateRequest();
        user1.setId(UUID.randomUUID());
        user1.setUsername("user1");

        UpdateRequest user2 = new UpdateRequest();
        user2.setId(UUID.randomUUID());
        user2.setUsername("user2");

        List<UpdateRequest> users = Arrays.asList(user1, user2);

        when(userServiceClass.getAllUser()).thenReturn(users);

        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testResetPassword() throws Exception {
        UUID userId = UUID.randomUUID();
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPassword("newpassword");

        when(userServiceClass.resetPassword(any(UUID.class), any(ResetPasswordRequest.class))).thenReturn("password resetted");

        mockMvc.perform(patch("/v1/users/{id}/resetpassword", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("password resetted"));
    }

    @Test
    void testPageUsers() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        UpdateRequest user = new UpdateRequest();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");

        Page<UpdateRequest> page = new PageImpl<>(Arrays.asList(user), pageable, 1);

        when(userServiceClass.pageUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/v1/users/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void testLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        String token = "jwt-token";

        when(userService.login(any(LoginRequest.class))).thenReturn(token);

        mockMvc.perform(post("/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        RefreshToken refreshToken = new RefreshToken("old-token");

        UUID userId = UUID.randomUUID();
        CustomUserDetails userDetails = new CustomUserDetails(userId, "testuser", "password", Arrays.asList());

        when(jwtUtility.isTokenExpired("old-token")).thenReturn(false);
        when(jwtUtility.extractUserId("old-token")).thenReturn(userId);
        when(customUserDetailsService.loadUserById(userId)).thenReturn(userDetails);
        when(jwtUtility.generateToken(userDetails)).thenReturn("new-token");

        mockMvc.perform(post("/v1/users/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-token"));
    }

    @Test
    void testCreateUser() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setFull_name("New User");
        registerRequest.setPassword("password");
        registerRequest.setRole(RoleType.ROLE_USER);

        UserResponse userResponse = new UserResponse(UUID.randomUUID(), "New User", "newuser", "new@example.com", RoleType.ROLE_USER);

        when(userService.createUser(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"));
    }
}
