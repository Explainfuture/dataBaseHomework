package com.hnu.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hnu.campus.dto.auth.LoginDTO;
import com.hnu.campus.dto.auth.LoginResponseDTO;
import com.hnu.campus.dto.auth.RegisterDTO;
import com.hnu.campus.entity.User;
import com.hnu.campus.enums.AuthStatus;
import com.hnu.campus.enums.UserRole;
import com.hnu.campus.exception.BusinessException;
import com.hnu.campus.mapper.UserMapper;
import com.hnu.campus.security.JwtUtil;
import com.hnu.campus.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private static final String VERIFY_CODE_PREFIX = "verify_code:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String ROLE_CACHE_PREFIX = "user_role:";
    private static final String TOKEN_VERSION_PREFIX = "user_token_version:";
    private static final String REFRESH_SET_PREFIX = "refresh_set:";

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-expire-seconds:2592000}")
    private long refreshExpireSeconds;

    @Value("${jwt.role-cache-seconds:1800}")
    private long roleCacheSeconds;

    public AuthServiceImpl(UserMapper userMapper,
                           StringRedisTemplate redisTemplate,
                           BCryptPasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Long register(RegisterDTO registerDTO) {
        String phone = registerDTO.getPhone();
        String key = VERIFY_CODE_PREFIX + phone;
        String cachedCode = redisTemplate.opsForValue().get(key);
        if (cachedCode == null || !cachedCode.equals(registerDTO.getVerifyCode())) {
            throw new BusinessException(400, "Invalid verify code");
        }

        User existing = userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));
        if (existing != null) {
            throw new BusinessException(400, "Phone already registered");
        }

        User user = User.builder()
                .phone(phone)
                .nickname(registerDTO.getNickname())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .studentId(registerDTO.getStudentId())
                .campusCardUrl(registerDTO.getCampusCardUrl())
                .authStatus(AuthStatus.PENDING.getCode())
                .role(UserRole.STUDENT.getCode())
                .isMuted(false)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userMapper.insert(user);
        redisTemplate.delete(key);
        return user.getId();
    }

    @Override
    public LoginResponseDTO login(LoginDTO loginDTO) {
        String phone = loginDTO.getPhone();
        String key = VERIFY_CODE_PREFIX + phone;
        String cachedCode = redisTemplate.opsForValue().get(key);
        if (cachedCode == null || !cachedCode.equals(loginDTO.getVerifyCode())) {
            throw new BusinessException(400, "Invalid verify code");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));
        if (user == null) {
            throw new BusinessException(400, "Phone or password is incorrect");
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "Phone or password is incorrect");
        }
        if (!AuthStatus.APPROVED.getCode().equals(user.getAuthStatus())) {
            throw new BusinessException(403, "Account not approved");
        }

        Long tokenVersion = getOrInitTokenVersion(user.getId());
        String accessToken = jwtUtil.generateToken(user.getId(), user.getRole(), tokenVersion);
        String refreshToken = generateRefreshToken();
        storeRefreshToken(refreshToken, user.getId(), tokenVersion);
        cacheUserRole(user.getId(), user.getRole());

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtUtil.getAccessExpireSeconds());
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        redisTemplate.delete(key);
        return response;
    }

    @Override
    public LoginResponseDTO refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(401, "Missing refresh token");
        }
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        String tokenValue = redisTemplate.opsForValue().get(key);
        if (tokenValue == null) {
            throw new BusinessException(401, "Login expired, please re-login");
        }
        Long userId;
        Long tokenVersionFromRefresh;
        String[] parts = tokenValue.split(":");
        if (parts.length != 2) {
            throw new BusinessException(401, "Login expired, please re-login");
        }
        try {
            userId = Long.valueOf(parts[0]);
            tokenVersionFromRefresh = Long.valueOf(parts[1]);
        } catch (NumberFormatException ex) {
            throw new BusinessException(401, "Login expired, please re-login");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "User not found");
        }
        if (!AuthStatus.APPROVED.getCode().equals(user.getAuthStatus())) {
            throw new BusinessException(403, "Account not approved");
        }

        Long tokenVersion = getOrInitTokenVersion(userId);
        if (!tokenVersion.equals(tokenVersionFromRefresh)) {
            throw new BusinessException(401, "Login expired, please re-login");
        }

        String accessToken = jwtUtil.generateToken(userId, user.getRole(), tokenVersion);
        String newRefreshToken = generateRefreshToken();
        deleteRefreshToken(userId, refreshToken);
        storeRefreshToken(newRefreshToken, userId, tokenVersion);
        cacheUserRole(userId, user.getRole());

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(accessToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(jwtUtil.getAccessExpireSeconds());
        response.setUserId(userId);
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        return response;
    }

    @Override
    public void sendVerifyCode(String phone) {
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        String key = VERIFY_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
        log.info("Send verify code to {}: {}", phone, code);
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        String tokenValue = redisTemplate.opsForValue().get(key);
        if (tokenValue == null) {
            return;
        }
        String[] parts = tokenValue.split(":");
        if (parts.length != 2) {
            return;
        }
        Long userId;
        try {
            userId = Long.valueOf(parts[0]);
        } catch (NumberFormatException ex) {
            return;
        }
        deleteRefreshToken(userId, refreshToken);
    }

    private void storeRefreshToken(String refreshToken, Long userId, Long tokenVersion) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        String value = userId + ":" + tokenVersion;
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(refreshExpireSeconds));
        String setKey = REFRESH_SET_PREFIX + userId;
        redisTemplate.opsForSet().add(setKey, refreshToken);
        redisTemplate.expire(setKey, Duration.ofSeconds(refreshExpireSeconds));
    }

    private void deleteRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.delete(key);
        String setKey = REFRESH_SET_PREFIX + userId;
        redisTemplate.opsForSet().remove(setKey, refreshToken);
    }

    private void cacheUserRole(Long userId, String role) {
        if (role == null) {
            return;
        }
        String key = ROLE_CACHE_PREFIX + userId;
        redisTemplate.opsForValue().set(key, role, Duration.ofSeconds(roleCacheSeconds));
    }

    private Long getOrInitTokenVersion(Long userId) {
        String key = TOKEN_VERSION_PREFIX + userId;
        String current = redisTemplate.opsForValue().get(key);
        if (current == null) {
            redisTemplate.opsForValue().set(key, "1");
            return 1L;
        }
        try {
            return Long.valueOf(current);
        } catch (NumberFormatException ex) {
            redisTemplate.opsForValue().set(key, "1");
            return 1L;
        }
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
