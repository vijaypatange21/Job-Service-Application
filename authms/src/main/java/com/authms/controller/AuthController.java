package com.authms.controller;

import com.authms.dto.request.LoginRequest;
import com.authms.dto.request.RegisterRequest;
import com.authms.dto.response.AuthResponse;
import com.authms.dto.response.TokenValidationResponse;
import com.authms.security.AppUserPrincipal;
import com.authms.service.AuthService;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        Set<String> roles = principal.getAuthorities()
                .stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(new TokenValidationResponse(true, principal.getUsername(), roles));
    }
}
