package com.authms.service.impl;

import com.authms.service.TokenBlacklistService;
import com.authms.util.TokenHashing;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void blacklist(String accessToken, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isPositive()) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + TokenHashing.sha256(accessToken), "revoked", ttl);
        }
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + TokenHashing.sha256(accessToken)));
    }
}
