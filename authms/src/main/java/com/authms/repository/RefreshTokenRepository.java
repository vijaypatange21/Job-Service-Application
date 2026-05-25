package com.authms.repository;

import com.authms.entity.AppUser;
import com.authms.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshToken token
            set token.revokedAt = :revokedAt
            where token.user = :user and token.revokedAt is null
            """)
    void revokeActiveTokensForUser(@Param("user") AppUser user, @Param("revokedAt") Instant revokedAt);

    @Modifying
    @Query("delete from RefreshToken token where token.expiresAt < :cutoff")
    void deleteExpiredTokens(@Param("cutoff") Instant cutoff);
}
