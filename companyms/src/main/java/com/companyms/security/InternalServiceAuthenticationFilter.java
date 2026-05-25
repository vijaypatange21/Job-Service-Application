package com.companyms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Service-Secret";

    private final String internalServiceSecret;

    public InternalServiceAuthenticationFilter(
            @Value("${gateway.internal.secret:change-this-internal-service-secret}") String internalServiceSecret
    ) {
        this.internalServiceSecret = internalServiceSecret;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!internalServiceSecret.equals(request.getHeader(INTERNAL_SECRET_HEADER))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Gateway internal authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
