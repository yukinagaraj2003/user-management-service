
package com.yukeshkumar.user_management_service.mapper;

import com.yukeshkumar.user_management_service.entity.UserEntity;
import com.yukeshkumar.user_management_service.model.RegisterRequest;
import com.yukeshkumar.user_management_service.model.UpdateRequest;
import com.yukeshkumar.user_management_service.model.UserResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity convertDtoToEntity(RegisterRequest request) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(request.getUsername());
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userEntity.setEmail(request.getEmail());
        userEntity.setRole(request.getRole());
        userEntity.setFull_name(request.getFull_name());
        return userEntity;
    }

    public RegisterRequest convertDtoFromEntity(UserEntity entity) {
        RegisterRequest request = new RegisterRequest();
        request.setId(entity.getId());
        request.setFull_name(entity.getFull_name());
        request.setEmail(entity.getEmail());
        request.setUsername(entity.getUsername());
        request.setPassword(entity.getPassword());
        request.setRole(entity.getRole());
        return request;
    }

    public UpdateRequest convertEntityToDto(UserEntity userEntity) {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setId(userEntity.getId());
        updateRequest.setFull_name(userEntity.getFull_name());
        updateRequest.setEmail(userEntity.getEmail());
        updateRequest.setUsername(userEntity.getUsername());
        updateRequest.setPassword(userEntity.getPassword());
        updateRequest.setRole(userEntity.getRole());
        return updateRequest;

    }

    public UserResponse convertEntityToUserResponse(UserEntity userEntity) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(userEntity.getId());
        userResponse.setFull_name(userEntity.getFull_name());
        userResponse.setEmail(userEntity.getEmail());
        userResponse.setUsername(userEntity.getUsername());
        userResponse.setRole(userEntity.getRole());
        return userResponse;
    }
}
