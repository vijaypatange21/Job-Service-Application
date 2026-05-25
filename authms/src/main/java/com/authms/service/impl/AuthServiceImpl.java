package com.authms.service.impl;

import com.authms.config.JwtProperties;
import com.authms.dto.request.LoginRequest;
import com.authms.dto.request.LogoutRequest;
import com.authms.dto.request.RefreshTokenRequest;
import com.authms.dto.request.RegisterRequest;
import com.authms.dto.response.AuthResponse;
import com.authms.entity.AppUser;
import com.authms.entity.RefreshToken;
import com.authms.entity.Role;
import com.authms.exception.DuplicateResourceException;
import com.authms.exception.InvalidCredentialsException;
import com.authms.repository.AppUserRepository;
import com.authms.security.AppUserPrincipal;
import com.authms.service.AuthService;
import com.authms.service.JwtService;
import com.authms.service.RefreshTokenService;
import com.authms.service.SecurityAuditService;
import com.authms.service.TokenBlacklistService;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final SecurityAuditService securityAuditService;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }

        AppUser user = AppUser.builder()
                .email(email)
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .password(passwordEncoder.encode(request.password()))
                .roles(resolveRoles(request.roles()))
                .build();

        AppUser savedUser = appUserRepository.save(user);
        AppUserPrincipal principal = new AppUserPrincipal(savedUser);
        String accessToken = jwtService.generateToken(principal);
        String refreshToken = refreshTokenService.createRefreshToken(savedUser);
        securityAuditService.loginSucceeded(savedUser);
        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password())
            );
            AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
            String accessToken = jwtService.generateToken(principal);
            String refreshToken = refreshTokenService.createRefreshToken(principal.user());
            securityAuditService.loginSucceeded(principal.user());
            return buildAuthResponse(principal.user(), accessToken, refreshToken);
        } catch (BadCredentialsException exception) {
            securityAuditService.loginFailed(request.email());
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(request.refreshToken());
        RefreshToken refreshToken = rotated.token();
        AppUser user = refreshToken.getUser();
        AppUserPrincipal principal = new AppUserPrincipal(user);
        String accessToken = jwtService.generateToken(principal);
        securityAuditService.tokenRefreshed(user);
        return buildAuthResponse(user, accessToken, rotated.rawToken(), refreshToken.getExpiresAt());
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request, String accessToken) {
        refreshTokenService.revoke(request.refreshToken());
        if (accessToken != null && !accessToken.isBlank()) {
            tokenBlacklistService.blacklist(accessToken, jwtService.extractExpiration(accessToken));
            securityAuditService.logoutSucceeded(jwtService.extractUsername(accessToken));
        }
    }

    private Set<Role> resolveRoles(Set<Role> requestedRoles) {
        if (requestedRoles == null || requestedRoles.isEmpty()) {
            return Set.of(Role.USER);
        }
        if (requestedRoles.contains(Role.ADMIN)) {
            throw new IllegalArgumentException("ADMIN role cannot be assigned through public registration");
        }
        return Set.copyOf(requestedRoles);
    }

    private AuthResponse buildAuthResponse(AppUser user, String accessToken, String refreshToken) {
        return buildAuthResponse(
                user,
                accessToken,
                refreshToken,
                Instant.now().plusMillis(jwtProperties.refreshTokenExpiration())
        );
    }

    private AuthResponse buildAuthResponse(
            AppUser user,
            String accessToken,
            String refreshToken,
            Instant refreshTokenExpiresAt
    ) {
        return new AuthResponse(
                "Bearer",
                accessToken,
                refreshToken,
                jwtService.extractExpiration(accessToken),
                refreshTokenExpiresAt,
                new AuthResponse.UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRoles()
                                .stream()
                                .map(Role::name)
                                .collect(Collectors.toSet())
                )
        );
    }
}
