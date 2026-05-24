package io.propertyintel.api.global.idempotency.service;

import io.propertyintel.api.global.exception.exceptions.DuplicateIdempotencyKeyException;
import io.propertyintel.api.global.idempotency.entity.IdempotencyRecord;
import io.propertyintel.api.global.idempotency.entity.IdempotencyState;
import io.propertyintel.api.global.idempotency.repository.IdempotencyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final IdempotencyRepository repository;

    @Transactional
    public void createProcessingRecord(String key, String endpoint, String requestHash) {
        IdempotencyRecord record = IdempotencyRecord.builder()
                .idempotencyKey(key)
                .endpoint(endpoint)
                .requestHash(requestHash)
                .state(IdempotencyState.PROCESSING)
                .build();

        try {
            repository.saveAndFlush(record);
        } catch (Exception e) {
            log.debug("Duplicate request detected or database error: {}", e.getMessage());
            throw new DuplicateIdempotencyKeyException("Request already in process");
        }
    }

    public Optional<IdempotencyRecord> find(String key, String endpoint) {
        return repository.findByIdempotencyKeyAndEndpoint(key, endpoint);
    }

    @Transactional
    public void delete(IdempotencyRecord record) {
        repository.delete(record);
    }

    @Transactional
    public void markCompleted(
            IdempotencyRecord record,
            String responseBody,
            int statusCode,
            String contentType
    ) {
        record.setState(IdempotencyState.COMPLETE);
        record.setResponseBody(responseBody);
        record.setStatusCode(statusCode);
        record.setContentType(contentType);

        repository.save(record);
    }

}
