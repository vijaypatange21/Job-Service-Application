package com.authms.service;

import com.authms.dto.request.LoginRequest;
import com.authms.dto.request.RegisterRequest;
import com.authms.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
