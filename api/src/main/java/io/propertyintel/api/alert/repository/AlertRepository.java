package io.propertyintel.api.alert.repository;

import io.propertyintel.api.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByUserId(UUID userId);
    Optional<Alert> findByAlertUnsubscribeToken(UUID token);
}
