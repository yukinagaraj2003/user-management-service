package com.yukeshkumar.user_management_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukeshkumar.user_management_service.model.AuthResponse;
import com.yukeshkumar.user_management_service.model.LoginRequest;
import com.yukeshkumar.user_management_service.model.RefreshToken;
import com.yukeshkumar.user_management_service.model.CustomUserDetails;
import com.yukeshkumar.user_management_service.security.JwtAuthFilter;
import com.yukeshkumar.user_management_service.security.JwtUtility;
import com.yukeshkumar.user_management_service.service.CustomUserDetailsService;
import com.yukeshkumar.user_management_service.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("deprecation")
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("deprecation")
    @MockBean
    private JwtUtility jwtUtility;

    @SuppressWarnings("deprecation")
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @SuppressWarnings("deprecation")
    @MockBean
    private UserServiceImpl userServiceImpl;

    @SuppressWarnings("deprecation")
    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        String token = "jwt-token";

        when(userServiceImpl.login(any(LoginRequest.class))).thenReturn(token);

        mockMvc.perform(post("/api/v1/auth/login")
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

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-token"));
    }

    @Test
    void testRefreshToken_TokenExpired() throws Exception {
        RefreshToken refreshToken = new RefreshToken("expired-token");

        when(jwtUtility.isTokenExpired("expired-token")).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").value("Token expired, login again"));
    }

    @Test
    void testRefreshToken_InvalidToken() throws Exception {
        RefreshToken refreshToken = new RefreshToken("invalid-token");

        when(jwtUtility.isTokenExpired("invalid-token")).thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.token").value("Invalid token"));
    }
}
