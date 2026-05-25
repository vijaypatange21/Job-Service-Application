package com.jobapp.api_gateway.dto;

import java.time.Instant;

public record GatewayErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
