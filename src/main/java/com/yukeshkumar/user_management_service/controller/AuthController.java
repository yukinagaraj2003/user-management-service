package com.yukeshkumar.user_management_service.controller;

import com.yukeshkumar.user_management_service.model.*;
import com.yukeshkumar.user_management_service.security.JwtUtility;
import com.yukeshkumar.user_management_service.service.CustomUserDetailsService;
import com.yukeshkumar.user_management_service.service.UserService;
import com.yukeshkumar.user_management_service.service.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173")
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
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshToken request) {

        String oldToken = request.getToken();

        if (!jwtUtility.isValid(oldToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Invalid token"));
        }

        if (jwtUtility.isExpired(oldToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Token expired, please login again"));
        }

        UUID userId = jwtUtility.getUserId(oldToken);
        String role = jwtUtility.getRole(oldToken);

        String newToken = jwtUtility.generateToken(userId, role);

        return ResponseEntity.ok(new AuthResponse(newToken));
    }
}