package com.authms.service;

import java.time.Instant;

public interface TokenBlacklistService {

    void blacklist(String accessToken, Instant expiresAt);

    boolean isBlacklisted(String accessToken);
}
