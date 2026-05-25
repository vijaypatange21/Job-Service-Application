package com.authms.service;

import java.time.Instant;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateToken(UserDetails userDetails);

    String extractTokenId(String token);

    String extractUsername(String token);

    Instant extractExpiration(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
