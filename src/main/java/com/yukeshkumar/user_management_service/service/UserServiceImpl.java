package com.yukeshkumar.user_management_service.service;

import com.yukeshkumar.user_management_service.entity.UserEntity;
import com.yukeshkumar.user_management_service.exception.UserAlreadyExistsException;
import com.yukeshkumar.user_management_service.mapper.UserMapper;
import com.yukeshkumar.user_management_service.model.LoginRequest;
import com.yukeshkumar.user_management_service.model.RegisterRequest;
import com.yukeshkumar.user_management_service.repository.UserRepository;
import com.yukeshkumar.user_management_service.model.CustomUserDetails;
import com.yukeshkumar.user_management_service.security.JwtUtility;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.yukeshkumar.user_management_service.model.UserResponse;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final JwtUtility jwtUtility;
    private final AuthenticationManager authenticationManager;

    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, JwtUtility jwtUtility, AuthenticationManager authenticationManager, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.jwtUtility = jwtUtility;
        this.authenticationManager = authenticationManager;

        this.userMapper = userMapper;
    }

    public UserResponse createUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }
        UserEntity userEntity = userMapper.convertDtoToEntity(request);
        UserEntity savedEntity = userRepository.save(userEntity);
        return userMapper.convertEntityToUserResponse(savedEntity);

    }

    public String login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return jwtUtility.generateToken(userDetails);
    }
}
