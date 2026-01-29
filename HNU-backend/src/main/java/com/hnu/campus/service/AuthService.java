package com.hnu.campus.service;

import com.hnu.campus.dto.auth.LoginDTO;
import com.hnu.campus.dto.auth.LoginResponseDTO;
import com.hnu.campus.dto.auth.RegisterDTO;

public interface AuthService {
    Long register(RegisterDTO registerDTO);

    LoginResponseDTO login(LoginDTO loginDTO);

    LoginResponseDTO refresh(String refreshToken);

    void logout(String refreshToken);

    void sendVerifyCode(String phone);
}
