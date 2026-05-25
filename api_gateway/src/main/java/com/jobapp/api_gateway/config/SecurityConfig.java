package com.jobapp.api_gateway.config;

import com.jobapp.api_gateway.security.BearerTokenServerAuthenticationConverter;
import com.jobapp.api_gateway.security.JsonServerAccessDeniedHandler;
import com.jobapp.api_gateway.security.JsonServerAuthenticationEntryPoint;
import com.jobapp.api_gateway.security.JwtReactiveAuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final JwtReactiveAuthenticationManager authenticationManager;
    private final BearerTokenServerAuthenticationConverter authenticationConverter;
    private final JsonServerAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonServerAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtReactiveAuthenticationManager authenticationManager,
            BearerTokenServerAuthenticationConverter authenticationConverter,
            JsonServerAuthenticationEntryPoint authenticationEntryPoint,
            JsonServerAccessDeniedHandler accessDeniedHandler
    ) {
        this.authenticationManager = authenticationManager;
        this.authenticationConverter = authenticationConverter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        AuthenticationWebFilter jwtAuthenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
        jwtAuthenticationWebFilter.setServerAuthenticationConverter(authenticationConverter);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/auth/**", "/eureka/**", "/actuator/**").permitAll()
                        .pathMatchers("/jobs/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers("/companies/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers("/reviews/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers("/admin/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
