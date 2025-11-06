package com.hive.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {
    public record ApiError(
            Instant timestamp,
            int status,
            String error,
            String path,
            String message,
            List<String> details) {
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
    })
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream().map(FieldError::getField)
                .collect(Collectors.toList());
        return new ResponseEntity<>(buildError(
                HttpStatus.BAD_REQUEST,
                request,
                "Validation failed",
                fieldErrors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            DisabledException.class,
            LockedException.class,
            AccountExpiredException.class,
            CredentialsExpiredException.class,
            AuthenticationServiceException.class,
            UserNotFoundException.class,
            AuthenticationFailureException.class
    })
    public ResponseEntity<ApiError> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {
        return new ResponseEntity<>(buildError(
                HttpStatus.UNAUTHORIZED,
                request,
                "Authentication failed",
                List.of(ex.getMessage())), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AssetNotFoundException.class)
    public ResponseEntity<ApiError> handleAssetNotFoundException(
            AssetNotFoundException ex,
            HttpServletRequest request) {
        return new ResponseEntity<>(buildError(
                HttpStatus.NOT_FOUND,
                request,
                "Asset not found!",
                List.of(ex.getMessage())), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        return new ResponseEntity<>(buildError(
                HttpStatus.FORBIDDEN,
                request,
                "Access denied",
                List.of(ex.getMessage())), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return new ResponseEntity<>(buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                request,
                "An unexpected error occurred",
                List.of(ex.getClass().getSimpleName() + ": " + ex.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            NoHandlerFoundException ex,
            HttpServletRequest request) {
        return new ResponseEntity<>(buildError(
                HttpStatus.NOT_FOUND,
                request,
                "Endpoint not found",
                List.of(String.format("%s %s", ex.getHttpMethod(), ex.getRequestURL()))), HttpStatus.NOT_FOUND);
    }

    private static ApiError buildError(HttpStatus status,
                                       HttpServletRequest request,
                                       String message,
                                       List<String> details) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                request.getRequestURI(),
                message,
                details);
    }
}