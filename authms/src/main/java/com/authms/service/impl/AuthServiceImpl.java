package com.authms.service.impl;

import com.authms.dto.request.LoginRequest;
import com.authms.dto.request.RegisterRequest;
import com.authms.dto.response.AuthResponse;
import com.authms.entity.AppUser;
import com.authms.entity.Role;
import com.authms.exception.DuplicateResourceException;
import com.authms.exception.InvalidCredentialsException;
import com.authms.repository.AppUserRepository;
import com.authms.security.AppUserPrincipal;
import com.authms.service.AuthService;
import com.authms.service.JwtService;
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
        String token = jwtService.generateToken(principal);
        return buildAuthResponse(savedUser, token);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password())
            );
            AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);
            return buildAuthResponse(principal.user(), token);
        } catch (BadCredentialsException exception) {
            throw new InvalidCredentialsException("Invalid email or password");
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

    private AuthResponse buildAuthResponse(AppUser user, String token) {
        return new AuthResponse(
                "Bearer",
                token,
                jwtService.extractExpiration(token),
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
