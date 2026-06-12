package io.propertyintel.api.auth.repository;

import io.propertyintel.api.auth.entity.EmailVerificationToken;
import io.propertyintel.api.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenHash(String token);

    // Invalidates all previous tokens before issuing a new one
    @Modifying
    @Query(value = "UPDATE EmailVerificationToken t " +
            "SET t.isUsed = true, t.usedAt = :instant " +
            "WHERE t.user = :user AND t.isUsed = false")
    void invalidateTokensForUser(@Param("user")User user, @Param("instant") Instant instant);

    // Rate-limit for resend: checks how many tokens issued in the last N minutes
    @Query(value = "SELECT COUNT(t) FROM EmailVerificationToken t WHERE t.user = :user AND t.createdAt > :since")
    long countRecentTokens(@Param("user") User user, @Param("since") Instant since);

}
