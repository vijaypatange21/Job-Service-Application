package com.jobapp.api_gateway.filter;

import java.util.stream.Collectors;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class JwtClaimsRelayGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtClaimsRelayGatewayFilterFactory.Config> {

    private static final String AUTHENTICATED_USER_HEADER = "X-Authenticated-User";
    private static final String AUTHENTICATED_ROLES_HEADER = "X-Authenticated-Roles";

    public JwtClaimsRelayGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication != null && authentication.isAuthenticated())
                .map(authentication -> withAuthenticationHeaders(exchange, authentication))
                .defaultIfEmpty(withoutSpoofedHeaders(exchange))
                .flatMap(chain::filter);
    }

    private ServerWebExchange withAuthenticationHeaders(ServerWebExchange exchange, Authentication authentication) {
        String roles = authentication.getAuthorities()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .headers(headers -> {
                    headers.remove(AUTHENTICATED_USER_HEADER);
                    headers.remove(AUTHENTICATED_ROLES_HEADER);
                    headers.set(AUTHENTICATED_USER_HEADER, authentication.getName());
                    headers.set(AUTHENTICATED_ROLES_HEADER, roles);
                })
                .build();

        return exchange.mutate().request(request).build();
    }

    private ServerWebExchange withoutSpoofedHeaders(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .headers(headers -> {
                    headers.remove(AUTHENTICATED_USER_HEADER);
                    headers.remove(AUTHENTICATED_ROLES_HEADER);
                })
                .build();

        return exchange.mutate().request(request).build();
    }

    public static class Config {
    }
}
