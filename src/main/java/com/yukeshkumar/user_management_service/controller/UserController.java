package com.yukeshkumar.user_management_service.controller;

import com.yukeshkumar.user_management_service.model.*;
import com.yukeshkumar.user_management_service.security.JwtUtility;
import com.yukeshkumar.user_management_service.service.CustomUserDetailsService;
import com.yukeshkumar.user_management_service.service.UserService;
import com.yukeshkumar.user_management_service.service.UserServiceClass;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("v1/users")
public class UserController {
    private UserServiceClass userServiceClass;
    private UserService userService;
    private JwtUtility jwtUtility;
    private CustomUserDetailsService customUserDetailsService;

    public UserController(UserService userService, UserServiceClass userServiceClass, JwtUtility jwtUtility, CustomUserDetailsService customUserDetailsService) {
        this.userService = userService;
        this.userServiceClass = userServiceClass;
        this.jwtUtility = jwtUtility;
        this.customUserDetailsService = customUserDetailsService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UpdateRequest> getUserById(@PathVariable UUID id) {
        UpdateRequest user = userServiceClass.getUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<UpdateRequest>> getAllUser() {
        List<UpdateRequest> users = userServiceClass.getAllUser();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PatchMapping("/{id}/resetpassword")
    public ResponseEntity<String> resetPassword(@PathVariable UUID id, @RequestBody ResetPasswordRequest request) {
        return new ResponseEntity<>(userServiceClass.resetPassword(id, request), HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<UpdateRequest>> pageUsers(Pageable pageable) {

        return new ResponseEntity<>(userServiceClass.pageUsers(pageable), HttpStatus.OK);

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        return new ResponseEntity<>(new AuthResponse(token), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> getToken(@RequestBody RefreshToken refreshToken) {
        try {
            String oldToken = refreshToken.getToken();
            if (jwtUtility.isTokenExpired(oldToken)) {
                return new ResponseEntity<>(new AuthResponse("token expired,login again"), HttpStatus.UNAUTHORIZED);
            }

            UUID userId = jwtUtility.extractUserId(oldToken);
            CustomUserDetails customUserDetails = (CustomUserDetails) customUserDetailsService.loadUserById(userId);
            String newToken = jwtUtility.generateToken(customUserDetails);
            return new ResponseEntity<>(new AuthResponse(newToken), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new AuthResponse("Invalid token"), HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody RegisterRequest request) {
        UserResponse result = userService.createUser(request);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
