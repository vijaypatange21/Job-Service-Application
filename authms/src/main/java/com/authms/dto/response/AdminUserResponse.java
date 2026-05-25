package com.authms.dto.response;

import java.time.Instant;
import java.util.Set;

public record AdminUserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Set<String> roles,
        boolean enabled,
        Instant createdAt
) {
}
