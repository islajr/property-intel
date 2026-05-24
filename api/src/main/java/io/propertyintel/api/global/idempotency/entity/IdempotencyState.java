package io.propertyintel.api.global.idempotency.entity;

public enum IdempotencyState {
    PROCESSING,
    COMPLETE,
    FAILED
}
