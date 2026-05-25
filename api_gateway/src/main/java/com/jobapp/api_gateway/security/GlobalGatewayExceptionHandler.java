package com.jobapp.api_gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobapp.api_gateway.dto.GatewayErrorResponse;
import java.time.Instant;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalGatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalGatewayExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(exception);
        }

        HttpStatus status = resolveStatus(exception);
        GatewayErrorResponse response = new GatewayErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                resolveMessage(status, exception),
                exchange.getRequest().getPath().value()
        );

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(response);
        } catch (Exception serializationException) {
            bytes = new byte[0];
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private HttpStatus resolveStatus(Throwable exception) {
        if (exception instanceof ResponseStatusException responseStatusException
                && responseStatusException.getStatusCode() instanceof HttpStatus httpStatus) {
            return httpStatus;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(HttpStatus status, Throwable exception) {
        if (status.is5xxServerError()) {
            return "Gateway failed to process the request";
        }
        return exception.getMessage();
    }
}
