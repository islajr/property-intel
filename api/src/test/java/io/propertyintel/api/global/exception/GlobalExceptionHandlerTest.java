package io.propertyintel.api.global.exception;

import io.propertyintel.api.global.exception.exceptions.BadRequestException;
import io.propertyintel.api.global.exception.exceptions.ForbiddenException;
import io.propertyintel.api.global.exception.exceptions.RateLimitException;
import io.propertyintel.api.global.exception.exceptions.ResourceNotFoundException;
import io.propertyintel.api.global.exception.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.validation.method.ParameterValidationResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/test-endpoint");
    }

    @Test
    void testHandleBadRequestException() {
        BadRequestException ex = new BadRequestException("Bad input data");
        ResponseEntity<ErrorResponse> response = handler.handleBadRequestException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BAD_REQUEST", response.getBody().error());
        assertEquals("Bad input data", response.getBody().message());
        assertEquals("/api/test-endpoint", response.getBody().path());
    }

    @Test
    void testHandleUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Invalid credentials");
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorizedException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UNAUTHORIZED", response.getBody().error());
        assertEquals("Invalid credentials", response.getBody().message());
    }

    @Test
    void testHandleForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Access Denied");
        ResponseEntity<ErrorResponse> response = handler.handleForbiddenException(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("FORBIDDEN", response.getBody().error());
        assertEquals("Access Denied", response.getBody().message());
    }

    @Test
    void testHandleNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
        ResponseEntity<ErrorResponse> response = handler.handleNotFoundException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("NOT_FOUND", response.getBody().error());
        assertEquals("Resource not found", response.getBody().message());
    }

    @Test
    void testHandleRateLimitException() {
        RateLimitException ex = new RateLimitException("Too many requests");
        ResponseEntity<ErrorResponse> response = handler.handleRateLimitException(ex, request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TOO_MANY_REQUESTS", response.getBody().error());
        assertEquals("Too many requests", response.getBody().message());
    }

    @Test
    void testHandleValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error = new FieldError("object", "email", "must be a valid email");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("email=must be a valid email"));
    }

    @Test
    void testHandleMethodValidationErrors() {
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
        ParameterValidationResult result = mock(ParameterValidationResult.class);
        MethodParameter parameter = mock(MethodParameter.class);
        
        when(parameter.getParameterName()).thenReturn("limit");
        when(result.getMethodParameter()).thenReturn(parameter);

        MessageSourceResolvable resolvable = mock(MessageSourceResolvable.class);
        when(resolvable.getDefaultMessage()).thenReturn("must be greater than 0");
        when(result.getResolvableErrors()).thenReturn(List.of(resolvable));

        when(ex.getParameterValidationResults()).thenReturn(List.of(result));

        ResponseEntity<ErrorResponse> response = handler.handleMethodValidationErrors(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("limit=must be greater than 0"));
    }

    @Test
    void testHandleAllExceptionsMasksDetailsOn500() {
        Exception rawException = new RuntimeException("Sensitive database connection details leaked here");
        ResponseEntity<ErrorResponse> response = handler.handleAllExceptions(rawException, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().error());
        // Verify it masks the real error message with a generic public warning
        assertEquals("An unexpected internal server error occurred.", response.getBody().message());
    }
}
