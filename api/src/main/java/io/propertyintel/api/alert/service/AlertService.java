package io.propertyintel.api.alert.service;

import io.propertyintel.api.alert.dto.AlertRequest;
import io.propertyintel.api.alert.dto.AlertResponse;
import io.propertyintel.api.alert.entity.Alert;
import io.propertyintel.api.alert.repository.AlertRepository;
import io.propertyintel.api.auth.entity.User;
import io.propertyintel.api.global.exception.exceptions.ForbiddenException;
import io.propertyintel.api.global.exception.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    @Transactional
    public AlertResponse createAlert(AlertRequest request, User user) {
        Alert alert = Alert.builder()
                .user(user)
                .neighbourhood(request.getNeighbourhood())
                .maxPriceKobo(request.getMaxPriceKobo())
                .minBedrooms(request.getMinBedrooms())
                .propertyType(request.getPropertyType())
                .isActive(true)
                .build();

        Alert saved = alertRepository.save(alert);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAlerts(User user) {
        return alertRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAlert(UUID alertId, User user) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));

        if (!alert.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not own this alert");
        }

        alertRepository.delete(alert);
    }

    private AlertResponse mapToResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .userId(alert.getUser().getId())
                .neighbourhood(alert.getNeighbourhood())
                .maxPriceKobo(alert.getMaxPriceKobo())
                .minBedrooms(alert.getMinBedrooms())
                .propertyType(alert.getPropertyType())
                .isActive(alert.isActive())
                .alertUnsubscribeToken(alert.getAlertUnsubscribeToken())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
