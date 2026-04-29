
package com.tangl.aistocksimulatorbackend.controller;

import com.tangl.aistocksimulatorbackend.common.Result;
import com.tangl.aistocksimulatorbackend.dto.request.LoginRequest;
import com.tangl.aistocksimulatorbackend.dto.request.RegisterRequest;
import com.tangl.aistocksimulatorbackend.dto.response.LoginResponse;
import com.tangl.aistocksimulatorbackend.dto.response.UserResponse;
import com.tangl.aistocksimulatorbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return Result.success("注册成功", response);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    @GetMapping("/me")
    public Result<UserResponse> getCurrentUser() {
        UserResponse response = authService.getCurrentUser();
        return Result.success(response);
    }
}
