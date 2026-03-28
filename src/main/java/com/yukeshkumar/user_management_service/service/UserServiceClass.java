package com.yukeshkumar.user_management_service.service;

import com.yukeshkumar.user_management_service.entity.UserEntity;
import com.yukeshkumar.user_management_service.mapper.UserMapper;
import com.yukeshkumar.user_management_service.model.ResetPasswordRequest;
import com.yukeshkumar.user_management_service.model.UpdateRequest;
import com.yukeshkumar.user_management_service.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceClass {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceClass(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UpdateRequest getUserById(UUID id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("user not exist"));
        return userMapper.convertEntityToDto(userEntity);
    }

    public List<UpdateRequest> getAllUser() {
        List<UserEntity> userEntities = userRepository.findAll();
        List<UpdateRequest> updateRequests = new ArrayList<>();
        for (UserEntity user : userEntities) {
            updateRequests.add(userMapper.convertEntityToDto(user));
        }
        return updateRequests;
    }

    public String resetPassword(UUID id, ResetPasswordRequest request) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("user not found"));
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(userEntity);
        return "password resetted";
    }

    public Page<UpdateRequest> pageUsers(Pageable pageable) {
        Page<UserEntity> userEntities = userRepository.findAll(pageable);
        return userEntities.map(userMapper::convertEntityToDto);
    }

}
