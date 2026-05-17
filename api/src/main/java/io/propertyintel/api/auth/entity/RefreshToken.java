package io.propertyintel.api.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(schema = "auths", name = "refresh_tokens")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    public RefreshToken(String tokenHash, User user, Boolean isRevoked, Instant createdAt, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.isRevoked = isRevoked;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private Boolean isRevoked;

    @CreationTimestamp
    private Instant createdAt;
    private Instant expiresAt;

}
