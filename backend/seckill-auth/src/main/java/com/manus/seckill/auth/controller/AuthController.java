package com.manus.seckill.auth.controller;

import com.manus.seckill.auth.common.Result;
import com.manus.seckill.auth.dto.LoginRequest;
import com.manus.seckill.auth.dto.LoginResponse;
import com.manus.seckill.auth.dto.RegisterRequest;
import com.manus.seckill.auth.dto.UserDTO;
import com.manus.seckill.auth.service.UserService;
import com.manus.seckill.auth.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        try {
            userService.register(request);
            return Result.success("User registered successfully");
        } catch (Exception e) {
            log.error("Registration failed", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Login failed", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<UserDTO> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            // Extract token from "Bearer <token>" format
            String actualToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            
            // Validate token
            if (!jwtUtil.isTokenValid(actualToken)) {
                return Result.error(401, "Invalid or expired token");
            }

            Long userId = jwtUtil.getUserIdFromToken(actualToken);
            UserDTO userInfo = userService.getUserInfo(userId);
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("Failed to get user info", e);
            return Result.error(e.getMessage());
        }
    }

}
