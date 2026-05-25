package com.authms.service;

import com.authms.entity.AppUser;
import com.authms.entity.RefreshToken;

public interface RefreshTokenService {

    String createRefreshToken(AppUser user);

    RotatedRefreshToken rotate(String refreshToken);

    void revoke(String refreshToken);

    record RotatedRefreshToken(String rawToken, RefreshToken token) {
    }
}
