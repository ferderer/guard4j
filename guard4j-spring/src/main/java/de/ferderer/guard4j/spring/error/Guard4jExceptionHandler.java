package de.ferderer.guard4j.spring.error;

import de.ferderer.guard4j.classification.HttpStatus;
import de.ferderer.guard4j.error.AppException;
import de.ferderer.guard4j.spring.autoconfigure.Guard4jProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for Guard4j Spring Boot integration.
 *
 * <p>This {@code @ControllerAdvice} provides centralized exception handling
 * for both Guard4j {@code AppException} and common Spring framework exceptions.
 * It converts exceptions into structured {@code ErrorResponse} objects with
 * appropriate HTTP status codes.
 *
 * <p>The handler is ordered to run before Spring Boot's default error handling
 * but after any application-specific exception handlers.
 */
@ControllerAdvice
@Order(-100) // Run before Spring Boot's default but after app-specific handlers
public class Guard4jExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(Guard4jExceptionHandler.class);

    private final Guard4jProperties properties;

    public Guard4jExceptionHandler(Guard4jProperties properties) {
        this.properties = properties;
    }

    /**
     * Handle Guard4j AppException with highest priority.
     *
     * <p>These exceptions contain rich error information and contextual data
     * that should be preserved in the response.
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(
            AppException ex, HttpServletRequest request) {

        log.debug("Handling AppException: {} at {}", ex.errorCode().name(), request.getRequestURI());

        ErrorResponse response = ErrorResponse.of(
            ex.errorCode().httpStatus().value(),
            ex.errorCode().name(),
            ex.getMessage(),
            request.getRequestURI(),
            ex.data()
        );

        return ResponseEntity
            .status(toSpringHttpStatus(ex.errorCode().httpStatus()))
            .body(response);
    }

    /**
     * Handle Spring framework and other exceptions if enabled.
     *
     * <p>This handler attempts to map common Spring exceptions to appropriate
     * Guard4j error codes. If no mapping is found, it falls back to a generic
     * internal server error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        // Only handle Spring exceptions if enabled
        if (!properties.web().handleSpringExceptions()) {
            // Re-throw as AppException to maintain Guard4j error handling consistency
            throw new AppException(SpringError.INTERNAL_SERVER_ERROR, ex);
        }

        // Try to map the exception to a SpringError
        SpringError springError = SpringError.fromExceptionWithFallback(ex);

        log.debug("Mapped {} to {} at {}",
            ex.getClass().getSimpleName(),
            springError.name(),
            request.getRequestURI());

        // Include original exception info in debug mode
        Map<String, Object> data = Map.of();
        if (properties.includeDebugInfo()) {
            data = Map.of(
                "exceptionType", ex.getClass().getSimpleName(),
                "originalMessage", ex.getMessage() != null ? ex.getMessage() : ""
            );
        }

        ErrorResponse response = ErrorResponse.of(
            springError.httpStatus().value(),
            springError.name(),
            springError.message(),
            request.getRequestURI(),
            data
        );

        return ResponseEntity
            .status(toSpringHttpStatus(springError.httpStatus()))
            .body(response);
    }

    /**
     * Convert Guard4j HttpStatus to Spring HttpStatus.
     *
     * @param guard4jStatus the Guard4j HTTP status
     * @return equivalent Spring HttpStatus
     * @throws IllegalArgumentException if status code is not recognized by Spring
     */
    private org.springframework.http.HttpStatus toSpringHttpStatus(HttpStatus guard4jStatus) {
        if (guard4jStatus == null) {
            return org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return org.springframework.http.HttpStatus.valueOf(guard4jStatus.value());
    }
}
