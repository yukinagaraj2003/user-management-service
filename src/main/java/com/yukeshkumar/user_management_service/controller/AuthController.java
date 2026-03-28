package com.yukeshkumar.user_management_service.controller;

import com.yukeshkumar.user_management_service.model.*;
import com.yukeshkumar.user_management_service.security.JwtUtility;
import com.yukeshkumar.user_management_service.service.CustomUserDetailsService;
import com.yukeshkumar.user_management_service.service.UserService;
import com.yukeshkumar.user_management_service.service.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {


    private final JwtUtility jwtUtility;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserServiceImpl userServiceImpl;

    public AuthController(
            JwtUtility jwtUtility,
            CustomUserDetailsService customUserDetailsService, UserServiceImpl userServiceImpl) {

        this.jwtUtility = jwtUtility;
        this.customUserDetailsService = customUserDetailsService;
        this.userServiceImpl = userServiceImpl;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        String token = userServiceImpl.login(request);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshToken refreshToken) {

        try {

            String oldToken = refreshToken.getToken();

            if (jwtUtility.isTokenExpired(oldToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse("Token expired, login again"));
            }

            UUID userId = jwtUtility.extractUserId(oldToken);

            CustomUserDetails customUserDetails =
                    (CustomUserDetails) customUserDetailsService.loadUserById(userId);

            String newToken = jwtUtility.generateToken(customUserDetails);

            return ResponseEntity.ok(new AuthResponse(newToken));

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthResponse("Invalid token"));
        }
    }
}