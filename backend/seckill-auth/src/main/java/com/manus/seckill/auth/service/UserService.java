package com.manus.seckill.auth.service;

import com.manus.seckill.auth.dto.LoginRequest;
import com.manus.seckill.auth.dto.LoginResponse;
import com.manus.seckill.auth.dto.RegisterRequest;
import com.manus.seckill.auth.dto.UserDTO;
import com.manus.seckill.auth.entity.User;

public interface UserService {

    /**
     * User registration
     */
    void register(RegisterRequest request);

    /**
     * User login
     */
    LoginResponse login(LoginRequest request);

    /**
     * Get user info by ID
     */
    UserDTO getUserInfo(Long userId);

    /**
     * Get user by username
     */
    User getUserByUsername(String username);

}
