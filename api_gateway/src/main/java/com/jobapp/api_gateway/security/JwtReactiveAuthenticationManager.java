package com.jobapp.api_gateway.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenService jwtTokenService;

    public JwtReactiveAuthenticationManager(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.fromSupplier(() -> {
            String token = String.valueOf(authentication.getCredentials());
            JwtTokenService.JwtPrincipal principal = jwtTokenService.validate(token);
            return new UsernamePasswordAuthenticationToken(
                    principal.username(),
                    token,
                    principal.authorities()
            );
        });
    }
}
