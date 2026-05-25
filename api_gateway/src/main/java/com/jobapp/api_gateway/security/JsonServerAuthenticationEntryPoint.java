package com.jobapp.api_gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobapp.api_gateway.dto.GatewayErrorResponse;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JsonServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JsonServerAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException exception) {
        GatewayErrorResponse response = new GatewayErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "A valid Bearer token is required",
                exchange.getRequest().getPath().value()
        );

        return write(exchange, HttpStatus.UNAUTHORIZED, response);
    }

    private Mono<Void> write(ServerWebExchange exchange, HttpStatus status, GatewayErrorResponse response) {
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(response);
        } catch (Exception exception) {
            bytes = new byte[0];
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }
}
