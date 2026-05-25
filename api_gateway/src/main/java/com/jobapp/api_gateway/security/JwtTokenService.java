package com.jobapp.api_gateway.security;

import com.jobapp.api_gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public JwtPrincipal validate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .requireIssuer(jwtProperties.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new BadCredentialsException("JWT subject is missing");
            }

            List<String> roles = claims.get("roles", List.class);
            Collection<GrantedAuthority> authorities = roles == null
                    ? List.of()
                    : roles.stream()
                            .map(String::valueOf)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toSet());

            return new JwtPrincipal(subject, authorities);
        } catch (Exception exception) {
            throw new BadCredentialsException("Invalid or expired JWT token", exception);
        }
    }

    private SecretKey signingKey() {
        String secret = jwtProperties.secret();
        byte[] keyBytes = looksBase64(secret)
                ? Decoders.BASE64.decode(secret)
                : secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean looksBase64(String value) {
        return value.matches("^[A-Za-z0-9+/]+={0,2}$") && value.length() % 4 == 0;
    }

    public record JwtPrincipal(String username, Collection<GrantedAuthority> authorities) {
    }
}
