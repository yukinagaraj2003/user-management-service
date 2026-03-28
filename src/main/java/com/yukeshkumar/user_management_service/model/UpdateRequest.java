package com.yukeshkumar.user_management_service.model;

import com.yukeshkumar.user_management_service.entity.RoleType;

import java.util.UUID;

public class UpdateRequest {
    private UUID id;
    private String full_name;
    private String username;
    private String email;
    private String password;
    private RoleType role;

    public UpdateRequest() {

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RoleType getRole() {
        return role;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }
}
