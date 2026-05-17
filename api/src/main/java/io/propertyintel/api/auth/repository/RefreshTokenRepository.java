package io.propertyintel.api.auth.repository;

import io.propertyintel.api.auth.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String token);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE RefreshToken t SET " +
            "t.tokenHash = :#{#token.tokenHash}, " +
            "t.isRevoked = :#{#token.isRevoked}, " +
            "t.createdAt = :#{#token.createdAt}, " +
            "t.expiresAt = :#{#token.expiresAt} " +
            "WHERE t.user = :#{#token.user}")
    void updateToken(@Param("token") RefreshToken token);
}
