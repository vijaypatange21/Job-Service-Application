package com.authms.dto.response;

import java.util.Set;

public record TokenValidationResponse(
        boolean valid,
        String email,
        Set<String> roles
) {
}
