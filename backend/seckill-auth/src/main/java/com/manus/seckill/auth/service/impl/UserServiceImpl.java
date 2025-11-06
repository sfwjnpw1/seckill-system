package com.manus.seckill.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.manus.seckill.auth.dto.LoginRequest;
import com.manus.seckill.auth.dto.LoginResponse;
import com.manus.seckill.auth.dto.RegisterRequest;
import com.manus.seckill.auth.dto.UserDTO;
import com.manus.seckill.auth.entity.User;
import com.manus.seckill.auth.mapper.UserMapper;
import com.manus.seckill.auth.service.UserService;
import com.manus.seckill.auth.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void register(RegisterRequest request) {
        // Check if user already exists
        User existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("User already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setScore(0);
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);
        log.info("User registered successfully: {}", request.getUsername());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // Find user by username
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // Build response
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setPhone(user.getPhone());
        userDTO.setScore(user.getScore());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(userDTO);

        log.info("User logged in successfully: {}", request.getUsername());
        return response;
    }

    @Override
    public UserDTO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setPhone(user.getPhone());
        userDTO.setScore(user.getScore());

        return userDTO;
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

}
