package com.yukeshkumar.user_management_service.model;

import com.yukeshkumar.user_management_service.entity.RoleType;

import java.util.UUID;

public class UserResponse {
    private UUID id;
    private String full_name;
    private String username;
    private String email;
    private RoleType role;

    public UserResponse() {
    }

    public UserResponse(UUID id, String full_name, String username, String email, RoleType role) {
        this.id = id;
        this.full_name = full_name;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }
}
