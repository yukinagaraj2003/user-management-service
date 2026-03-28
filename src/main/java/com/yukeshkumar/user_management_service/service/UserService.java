package com.yukeshkumar.user_management_service.service;

import com.yukeshkumar.user_management_service.model.LoginRequest;
import com.yukeshkumar.user_management_service.model.RegisterRequest;
import com.yukeshkumar.user_management_service.model.UserResponse;

public interface UserService {
    UserResponse createUser(RegisterRequest request);

    String login(LoginRequest request);

}
