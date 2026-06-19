package io.propertyintel.api.global.idempotency.config;

import io.propertyintel.api.global.exception.exceptions.DuplicateIdempotencyKeyException;
import io.propertyintel.api.global.idempotency.annotation.Idempotent;
import io.propertyintel.api.global.idempotency.entity.IdempotencyRecord;
import io.propertyintel.api.global.idempotency.entity.IdempotencyState;
import io.propertyintel.api.global.idempotency.service.IdempotencyService;
import io.propertyintel.api.global.idempotency.util.RequestHasher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyInterceptorTest {

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private RequestHasher requestHasher;

    @InjectMocks
    private IdempotencyInterceptor interceptor;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HandlerMethod handlerMethod;
    private StringWriter responseOut;

    @BeforeEach
    void setUp() throws Exception {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handlerMethod = mock(HandlerMethod.class);
        
        responseOut = new StringWriter();
        PrintWriter writer = new PrintWriter(responseOut);
        lenient().when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testPreHandleBypassesNonHandlerMethod() throws Exception {
        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        verifyNoInteractions(idempotencyService);
    }

    @Test
    void testPreHandleBypassesNonIdempotentMethod() throws Exception {
        when(handlerMethod.getMethodAnnotation(Idempotent.class)).thenReturn(null);
        boolean result = interceptor.preHandle(request, response, handlerMethod);
        assertTrue(result);
        verifyNoInteractions(idempotencyService);
    }

    @Test
    void testPreHandleMissingIdempotencyKey() throws Exception {
        Idempotent annotation = mock(Idempotent.class);
        when(handlerMethod.getMethodAnnotation(Idempotent.class)).thenReturn(annotation);
        when(request.getHeader("Idempotency-Key")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/resource");

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertFalse(result);
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(response).setContentType("application/json");
        assertTrue(responseOut.toString().contains("Idempotency-Key header is required"));
    }

    @Test
    void testPreHandleNewRequestSuccess() throws Exception {
        Idempotent annotation = mock(Idempotent.class);
        when(handlerMethod.getMethodAnnotation(Idempotent.class)).thenReturn(annotation);
        
        CachingBodyFilter.CachedBodyHttpServletRequest wrappedRequest = mock(CachingBodyFilter.CachedBodyHttpServletRequest.class);
        when(wrappedRequest.getHeader("Idempotency-Key")).thenReturn("unique-key-123");
        when(wrappedRequest.getRequestURI()).thenReturn("/api/v1/resource");
        when(wrappedRequest.getCachedBody()).thenReturn("payload".getBytes(StandardCharsets.UTF_8));
        when(requestHasher.hashRequest("payload")).thenReturn("hash-123");

        boolean result = interceptor.preHandle(wrappedRequest, response, handlerMethod);

        assertTrue(result);
        verify(idempotencyService).createProcessingRecord("unique-key-123", "/api/v1/resource", "hash-123");
        verify(wrappedRequest).setAttribute("IDEMPOTENCY_KEY_ATTR", "unique-key-123");
        verify(wrappedRequest).setAttribute("IDEMPOTENCY_ENDPOINT_ATTR", "/api/v1/resource");
    }

    @Test
    void testPreHandleDuplicateProcessingRecord() throws Exception {
        Idempotent annotation = mock(Idempotent.class);
        when(handlerMethod.getMethodAnnotation(Idempotent.class)).thenReturn(annotation);
        
        CachingBodyFilter.CachedBodyHttpServletRequest wrappedRequest = mock(CachingBodyFilter.CachedBodyHttpServletRequest.class);
        when(wrappedRequest.getHeader("Idempotency-Key")).thenReturn("duplicate-key");
        when(wrappedRequest.getRequestURI()).thenReturn("/api/v1/resource");
        when(wrappedRequest.getCachedBody()).thenReturn("payload".getBytes(StandardCharsets.UTF_8));
        when(requestHasher.hashRequest("payload")).thenReturn("hash-123");

        doThrow(new DuplicateIdempotencyKeyException("Processing"))
                .when(idempotencyService).createProcessingRecord("duplicate-key", "/api/v1/resource", "hash-123");

        IdempotencyRecord processingRecord = IdempotencyRecord.builder()
                .idempotencyKey("duplicate-key")
                .endpoint("/api/v1/resource")
                .requestHash("hash-123")
                .state(IdempotencyState.PROCESSING)
                .build();
        when(idempotencyService.find("duplicate-key", "/api/v1/resource")).thenReturn(Optional.of(processingRecord));

        boolean result = interceptor.preHandle(wrappedRequest, response, handlerMethod);

        assertFalse(result);
        verify(response).setStatus(HttpStatus.CONFLICT.value());
        assertTrue(responseOut.toString().contains("already in progress"));
    }

    @Test
    void testPreHandleDuplicateCompletedRecord() throws Exception {
        Idempotent annotation = mock(Idempotent.class);
        when(handlerMethod.getMethodAnnotation(Idempotent.class)).thenReturn(annotation);
        
        CachingBodyFilter.CachedBodyHttpServletRequest wrappedRequest = mock(CachingBodyFilter.CachedBodyHttpServletRequest.class);
        when(wrappedRequest.getHeader("Idempotency-Key")).thenReturn("duplicate-key");
        when(wrappedRequest.getRequestURI()).thenReturn("/api/v1/resource");
        when(wrappedRequest.getCachedBody()).thenReturn("payload".getBytes(StandardCharsets.UTF_8));
        when(requestHasher.hashRequest("payload")).thenReturn("hash-123");

        doThrow(new DuplicateIdempotencyKeyException("Complete"))
                .when(idempotencyService).createProcessingRecord("duplicate-key", "/api/v1/resource", "hash-123");

        IdempotencyRecord completedRecord = IdempotencyRecord.builder()
                .idempotencyKey("duplicate-key")
                .endpoint("/api/v1/resource")
                .requestHash("hash-123")
                .state(IdempotencyState.COMPLETE)
                .statusCode(201)
                .contentType("application/json")
                .responseBody("{\"result\":\"cached\"}")
                .build();
        when(idempotencyService.find("duplicate-key", "/api/v1/resource")).thenReturn(Optional.of(completedRecord));

        boolean result = interceptor.preHandle(wrappedRequest, response, handlerMethod);

        assertFalse(result);
        verify(response).setStatus(201);
        verify(response).setContentType("application/json");
        assertEquals("{\"result\":\"cached\"}", responseOut.toString());
    }
}
