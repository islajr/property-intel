package io.propertyintel.api.global.exception;

import io.propertyintel.api.global.exception.exceptions.BadRequestException;
import io.propertyintel.api.global.exception.exceptions.ForbiddenException;
import io.propertyintel.api.global.exception.exceptions.ResourceNotFoundException;
import io.propertyintel.api.global.exception.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    // 400 - Bad Request
    @ExceptionHandler(value = { BadRequestException.class })
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.name(), ex.getMessage(), request.getRequestURI(), LocalDateTime.now()));
    }

    // 502 - Method Not Allowed
    @ExceptionHandler(value = {MethodNotAllowedException.class})
    public ResponseEntity<ErrorResponse> handleMethodNotAllowedException(MethodNotAllowedException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.name(), ex.getMessage(), request.getRequestURI(), LocalDateTime.now()));
    }

    // 404 - Not Found
    @ExceptionHandler(value = { ResourceNotFoundException.class })
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.name(), ex.getMessage(), request.getRequestURI(), LocalDateTime.now()));
    }

    // 400 - Bad Request
    @ExceptionHandler(value = { NoResourceFoundException.class })
    public ResponseEntity<ErrorResponse> handleBadRequestException(NoResourceFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.name(), ex.getMessage(), request.getRequestURI(), LocalDateTime.now()));
    }

    // 400 - Validation Errors
    @ExceptionHandler(value = { MethodArgumentNotValidException.class })
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }


    // 400 - Missing Parameters
    @ExceptionHandler(value = { MissingServletRequestParameterException.class })
    public ResponseEntity<ErrorResponse> handleMissingParams(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.name(), ex.getMessage(), request.getRequestURI(), LocalDateTime.now()));
    }

    // 401 - Unauthorized
    @ExceptionHandler(value = { UnauthorizedException.class })
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.name(), ex.getMessage(), request.getRequestURI(), LocalDateTime.now()));
    }

    // 403 - Forbidden
    @ExceptionHandler(value = { ForbiddenException.class })
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.name(), ex.getMessage(), request.getRequestURI(), LocalDateTime.now()));
    }


    // 500 - Fallback Handler
    @ExceptionHandler(value = { RuntimeException.class })
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.name(), ex.getMessage(), request.getRequestURI(), LocalDateTime.now()));
    }


}
