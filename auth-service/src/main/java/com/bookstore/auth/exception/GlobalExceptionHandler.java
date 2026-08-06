package com.bookstore.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.CONFLICT, ex.getMessage(), request, null);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            AuthenticationException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationErrors(
            AuthenticationException ex, HttpServletRequest request) {
        ErrorResponse error = buildError(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password.",
                request,
                null
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({
            ExpiredTokenException.class,
            InvalidTokenException.class
    })
    public ResponseEntity<ErrorResponse> handleTokenErrors(
            RuntimeException ex, HttpServletRequest request) {
        ErrorResponse error = buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        ErrorResponse error = buildError(
                HttpStatus.BAD_REQUEST,
                "Validation failed for one or more fields",
                request,
                validationErrors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception, HttpServletRequest request) {
        ErrorResponse error = buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                request,
                null
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ErrorResponse buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<String> validationErrors) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();
    }
}
