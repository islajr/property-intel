package io.propertyintel.api.global.idempotency.config;

import io.propertyintel.api.global.exception.exceptions.DuplicateIdempotencyKeyException;
import io.propertyintel.api.global.idempotency.annotation.Idempotent;
import io.propertyintel.api.global.idempotency.entity.IdempotencyRecord;
import io.propertyintel.api.global.idempotency.entity.IdempotencyState;
import io.propertyintel.api.global.idempotency.service.IdempotencyService;
import io.propertyintel.api.global.idempotency.util.RequestHasher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyInterceptor implements HandlerInterceptor {
    private final IdempotencyService service;
    private final RequestHasher requestHasher;

    private static final String IDEMPOTENCY_KEY_ATTR = "IDEMPOTENCY_KEY_ATTR";
    private static final String IDEMPOTENCY_ENDPOINT_ATTR = "IDEMPOTENCY_ENDPOINT_ATTR";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Only intercept requests mapped to Controller methods
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // Only enforce idempotency if the @Idempotent annotation is present
        Idempotent idempotent = handlerMethod.getMethodAnnotation(Idempotent.class);
        if (idempotent == null) {
            return true;
        }

        String key = request.getHeader("Idempotency-Key");
        String endpoint = request.getRequestURI();

        if (key == null || key.isBlank()) {
            log.warn("Idempotency key missing for annotated endpoint: {}", endpoint);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                        "error": "%s",
                        "message": "Idempotency-Key header is required for this endpoint",
                        "path": "%s",
                        "timestamp": "%s"
                    }
                    """.formatted(HttpStatus.BAD_REQUEST.name(), endpoint, Instant.now()));
            response.getWriter().flush();
            return false;
        }

        // Retrieve request body from cache
        String requestBody = "";
        if (request instanceof CachingBodyFilter.CachedBodyHttpServletRequest wrappedRequest) {
            requestBody = new String(wrappedRequest.getCachedBody(), StandardCharsets.UTF_8);
        }

        String hashedBody = requestHasher.hashRequest(requestBody);

        try {
            service.createProcessingRecord(key, endpoint, hashedBody);
            request.setAttribute(IDEMPOTENCY_KEY_ATTR, key);
            request.setAttribute(IDEMPOTENCY_ENDPOINT_ATTR, endpoint);
            log.debug("Request marked as processing for key: {}", key);
            return true;

        } catch (DuplicateIdempotencyKeyException ex) {
            // Detect key re-use
            IdempotencyRecord existingRecord = service.find(key, endpoint)
                    .orElseThrow(() -> new IllegalStateException("Record not found despite duplicate key exception"));

            // Check if request body matches
            if (!existingRecord.getRequestHash().equals(hashedBody)) {
                log.warn("Idempotency key re-use detected for key: {}", key);
                response.setStatus(HttpStatus.CONFLICT.value());
                response.setContentType("application/json");
                response.getWriter().write("""
                        {
                            "error": "%s",
                            "message": "Idempotency key re-use detected with a different request payload",
                            "path": "%s",
                            "timestamp": "%s"
                        }
                        """.formatted(HttpStatus.CONFLICT.name(), endpoint, Instant.now()));
                response.getWriter().flush();
                return false;
            }

            // Check for completed requests
            if (existingRecord.getState() == IdempotencyState.COMPLETE) {
                log.debug("Returning cached response for key: {}", key);
                response.setStatus(existingRecord.getStatusCode());
                if (existingRecord.getContentType() != null) {
                    response.setContentType(existingRecord.getContentType());
                } else {
                    response.setContentType("application/json");
                }
                if (existingRecord.getResponseBody() != null) {
                    response.getWriter().write(existingRecord.getResponseBody());
                }
                response.getWriter().flush();
                return false;
            }

            // Requests under processing
            if (existingRecord.getState() == IdempotencyState.PROCESSING) {
                log.debug("Duplicate request is already under processing for key: {}", key);
                response.setStatus(HttpStatus.CONFLICT.value());
                response.setContentType("application/json");
                response.getWriter().write("""
                        {
                            "error": "%s",
                            "message": "A request with the same idempotency key is already in progress",
                            "path": "%s",
                            "timestamp": "%s"
                        }
                        """.formatted(HttpStatus.CONFLICT.name(), endpoint, Instant.now()));
                response.getWriter().flush();
                return false;
            }

            // Previous attempt failed - allow retry
            if (existingRecord.getState() == IdempotencyState.FAILED) {
                log.debug("Previous attempt failed for key: {}. Retrying request.", key);

                service.delete(existingRecord);
                service.createProcessingRecord(key, endpoint, hashedBody);
                request.setAttribute(IDEMPOTENCY_KEY_ATTR, key);
                request.setAttribute(IDEMPOTENCY_ENDPOINT_ATTR, endpoint);
                return true;
            }

            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String key = (String) request.getAttribute(IDEMPOTENCY_KEY_ATTR);
        String endpoint = (String) request.getAttribute(IDEMPOTENCY_ENDPOINT_ATTR);

        if (key != null && endpoint != null) {
            service.find(key, endpoint).ifPresent(record -> {
                if (ex != null || response.getStatus() >= 500) {
                    log.debug("Request failed (status: {} or exception). Deleting processing record for key: {}", response.getStatus(), key);
                    service.delete(record);
                } else {
                    String responseBody = "";
                    if (response instanceof ContentCachingResponseWrapper wrappedResponse) {
                        responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
                    }
                    log.debug("Request completed successfully (status: {}). Caching response for key: {}", response.getStatus(), key);
                    service.markCompleted(record, responseBody, response.getStatus(), response.getContentType());
                }
            });
        }
    }
}
