package com.yukeshkumar.user_management_service.service;

import com.yukeshkumar.user_management_service.entity.RoleType;
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

import java.util.UUID;

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

    public UserResponse createUser(RegisterRequest request, UUID userId) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        UserEntity creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        RoleType creatorRole = creator.getRole();
        RoleType requestedRole = request.getRole();

        if (requestedRole == RoleType.ROLE_ADMIN &&
                creatorRole != RoleType.ROLE_SUPER_ADMIN) {
            throw new RuntimeException("Only SUPER_ADMIN can create ADMIN");
        }

        if (requestedRole == RoleType.ROLE_MANAGER &&
                creatorRole == RoleType.ROLE_USER) {
            throw new RuntimeException("USER cannot create MANAGER");
        }

        UserEntity userEntity = userMapper.convertDtoToEntity(request);
        userEntity.setCreatedBy(userId);

        return userMapper.convertEntityToUserResponse(
                userRepository.save(userEntity)
        );
    }

    public String login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String role = userDetails.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        return jwtUtility.generateToken(
                userDetails.getId(),
                role
        );
    }
}
