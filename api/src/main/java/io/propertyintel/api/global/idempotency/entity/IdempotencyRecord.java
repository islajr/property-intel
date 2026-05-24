package io.propertyintel.api.global.idempotency.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(schema = "public", name = "idempotency_records", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "endpoint",
                "idempotency_key"
        })
})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Column(name = "endpoint", nullable = false)
    private String endpoint;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private IdempotencyState state;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "status_code")
    private Integer statusCode;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

}
