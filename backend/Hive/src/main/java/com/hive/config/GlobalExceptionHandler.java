package com.hive.config;

import jakarta.servlet.ServletException;
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
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static ApiError buildError(HttpStatus status,
                                       HttpServletRequest request,
                                       String message) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                request.getRequestURI(),
                message
        );
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
                "Validation failed! " +
                        String.join("\n", fieldErrors)), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            DisabledException.class,
            LockedException.class,
            AccountExpiredException.class,
            CredentialsExpiredException.class,
            AuthenticationServiceException.class,
            AuthenticationFailureException.class
    })
    public ResponseEntity<ApiError> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {
        return new ResponseEntity<>(buildError(
                HttpStatus.UNAUTHORIZED,
                request,
                "Authentication failed! " + ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({
            UserNotFoundException.class,
            NotFoundException.class
    })
    public ResponseEntity<ApiError> handleAssetNotFoundException(
            ServletException ex,
            HttpServletRequest request) {
        return new ResponseEntity<>(buildError(
                HttpStatus.NOT_FOUND,
                request,
                ex.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        return new ResponseEntity<>(buildError(
                HttpStatus.FORBIDDEN,
                request,
                "Access denied! " + List.of(ex.getMessage())), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return new ResponseEntity<>(buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                request,
                "An unexpected error occurred!"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(
            ServletException ex,
            HttpServletRequest request) {
        return new ResponseEntity<>(buildError(
                HttpStatus.NOT_FOUND,
                request,
                "Endpoint not found: " + ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    public record ApiError(
            Instant timestamp,
            int status,
            String error,
            String path,
            String message
    ) {
    }
}