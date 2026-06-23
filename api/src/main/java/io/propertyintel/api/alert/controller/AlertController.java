package io.propertyintel.api.alert.controller;

import io.propertyintel.api.alert.dto.AlertRequest;
import io.propertyintel.api.alert.dto.AlertResponse;
import io.propertyintel.api.alert.service.AlertService;
import io.propertyintel.api.auth.entity.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping({"/api/v1/alerts", "/alerts"})
    public ResponseEntity<AlertResponse> createAlert(
            @Valid @RequestBody AlertRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(alertService.createAlert(request, principal.getUser()));
    }

    @GetMapping({"/api/v1/alerts", "/alerts"})
    public ResponseEntity<List<AlertResponse>> getAlerts(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(alertService.getAlerts(principal.getUser()));
    }

    @DeleteMapping({"/api/v1/alerts/{id}", "/alerts/{id}"})
    public ResponseEntity<Void> deleteAlert(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        alertService.deleteAlert(id, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
