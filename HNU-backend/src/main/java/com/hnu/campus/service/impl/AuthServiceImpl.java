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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private static final String VERIFY_CODE_PREFIX = "verify_code:";

    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
            throw new BusinessException(400, "验证码错误或已过期");
        }

        User existing = userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));
        if (existing != null) {
            throw new BusinessException(400, "手机号已注册");
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
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", loginDTO.getPhone()));
        if (user == null) {
            throw new BusinessException(400, "手机号或密码错误");
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "手机号或密码错误");
        }
        if (!AuthStatus.APPROVED.getCode().equals(user.getAuthStatus())) {
            throw new BusinessException(403, "账号未通过审核");
        }
        if (Boolean.TRUE.equals(user.getIsMuted())) {
            throw new BusinessException(403, "账号已被禁言");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setUserId(user.getId());
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
}
