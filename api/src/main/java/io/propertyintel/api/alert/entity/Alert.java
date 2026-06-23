package io.propertyintel.api.alert.entity;

import io.propertyintel.api.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "auths", name = "alerts")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String neighbourhood;

    private Long maxPriceKobo;
    private Integer minBedrooms;
    private String propertyType;

    @Builder.Default
    private boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private UUID alertUnsubscribeToken;

    @CreationTimestamp
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (alertUnsubscribeToken == null) {
            alertUnsubscribeToken = UUID.randomUUID();
        }
    }
}
