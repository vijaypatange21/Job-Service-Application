package com.authms.service.impl;

import com.authms.config.JwtProperties;
import com.authms.entity.AppUser;
import com.authms.entity.RefreshToken;
import com.authms.exception.TokenRefreshException;
import com.authms.repository.RefreshTokenRepository;
import com.authms.service.RefreshTokenService;
import com.authms.util.TokenHashing;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public String createRefreshToken(AppUser user) {
        String rawToken = generateOpaqueToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(TokenHashing.sha256(rawToken))
                .user(user)
                .expiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpiration()))
                .build();
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Override
    @Transactional
    public RotatedRefreshToken rotate(String refreshToken) {
        String oldHash = TokenHashing.sha256(refreshToken);
        RefreshToken existingToken = refreshTokenRepository.findByTokenHash(oldHash)
                .orElseThrow(() -> new TokenRefreshException("Invalid refresh token"));

        if (existingToken.isRevoked()) {
            refreshTokenRepository.revokeActiveTokensForUser(existingToken.getUser(), Instant.now());
            throw new TokenRefreshException("Refresh token reuse detected");
        }

        if (existingToken.isExpired()) {
            existingToken.setRevokedAt(Instant.now());
            throw new TokenRefreshException("Refresh token expired");
        }

        String newRawToken = generateOpaqueToken();
        String newHash = TokenHashing.sha256(newRawToken);
        existingToken.setRevokedAt(Instant.now());
        existingToken.setReplacedByTokenHash(newHash);

        RefreshToken rotatedToken = RefreshToken.builder()
                .tokenHash(newHash)
                .user(existingToken.getUser())
                .expiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpiration()))
                .build();
        refreshTokenRepository.save(rotatedToken);

        return new RotatedRefreshToken(newRawToken, rotatedToken);
    }

    @Override
    @Transactional
    public void revoke(String refreshToken) {
        refreshTokenRepository.findByTokenHash(TokenHashing.sha256(refreshToken))
                .ifPresent(token -> token.setRevokedAt(Instant.now()));
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
