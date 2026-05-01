
package com.tangl.aistocksimulatorbackend.service;

import com.tangl.aistocksimulatorbackend.dto.request.LoginRequest;
import com.tangl.aistocksimulatorbackend.dto.request.RegisterRequest;
import com.tangl.aistocksimulatorbackend.dto.response.LoginResponse;
import com.tangl.aistocksimulatorbackend.dto.response.UserResponse;
import com.tangl.aistocksimulatorbackend.entity.User;
import com.tangl.aistocksimulatorbackend.enums.UserStatus;
import com.tangl.aistocksimulatorbackend.mapper.UserMapper;
import com.tangl.aistocksimulatorbackend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建新用户
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .virtualBalance(new BigDecimal("1000000.00"))
                .totalAssets(new BigDecimal("1000000.00"))
                .status(UserStatus.ACTIVE.getCode())
                .build();

        userMapper.insert(user);
        log.info("用户注册成功: {}", user.getUsername());

        return convertToResponse(user);
    }

    public LoginResponse login(LoginRequest request) {
        // 认证
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsernameOrEmail(),
                request.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成 Token
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.getUsernameOrEmail());

        // 获取用户信息
        User user = userMapper.findByUsernameOrEmail(request.getUsernameOrEmail());
        
        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                .build();
    }

    public UserResponse getCurrentUser() {
        // 从 SecurityContext 获取用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToResponse(user);
    }

    public UserResponse getCurrentUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToResponse(user);
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .virtualBalance(user.getVirtualBalance())
                .totalAssets(user.getTotalAssets())
                .status(user.getStatus())
                .build();
    }
}
