package com.authms.dto.response;

import java.time.Instant;
import java.util.Set;

public record AuthResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt,
        UserResponse user
) {

    public record UserResponse(
            Long id,
            String email,
            String firstName,
            String lastName,
            Set<String> roles
    ) {
    }
}
