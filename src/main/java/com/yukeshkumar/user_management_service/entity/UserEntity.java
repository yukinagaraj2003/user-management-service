package com.yukeshkumar.user_management_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "users")
public class UserEntity {
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(
            name = "id",
            columnDefinition = "CHAR(36)",
            nullable = false,
            updatable = false
    )
    private UUID id;
    @Column(name = "fullName")
    private String full_name;
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private RoleType role;
    @Column(name = "create_at", updatable = false)
    @JsonIgnore
    private Instant createdAt;

    @Column(name = "updated_at")
    @JsonIgnore
    private Instant updatedAt;
    @Column(name = "updated_by")
    @JsonIgnore
    private String updatedBy;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }


    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @PrePersist
    public void generateIdAndCreateAt() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();

    }


    @PreUpdate
    public void updatedTimeStamp() {
        this.updatedAt = Instant.now();
    }
}

