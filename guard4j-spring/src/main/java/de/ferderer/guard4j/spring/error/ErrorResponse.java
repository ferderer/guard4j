package de.ferderer.guard4j.spring.error;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error response structure for Guard4j Spring Boot integration.
 *
 * <p>This record provides a consistent JSON structure for all error responses,
 * regardless of whether they originate from {@code AppException} or Spring
 * framework exceptions.
 *
 * <h3>Example JSON Response:</h3>
 * <pre>{@code
 * {
 *   "timestamp": "2025-09-07T15:30:45.123Z",
 *   "status": 400,
 *   "error": "VALIDATION_FAILED",
 *   "message": "Input validation failed",
 *   "path": "/api/users",
 *   "data": {
 *     "field": "email",
 *     "rejectedValue": "invalid-email"
 *   }
 * }
 * }</pre>
 *
 * @param timestamp ISO-8601 formatted timestamp when error occurred
 * @param status HTTP status code
 * @param error Guard4j error code name
 * @param message Human-readable error message
 * @param path Request path where error occurred
 * @param data Additional context data (validation errors, etc.)
 */
public record ErrorResponse(
    String timestamp,
    int status,
    String error,
    String message,
    String path,
    Map<String, Object> data
) {

    /**
     * Create an ErrorResponse with current timestamp.
     *
     * @param status HTTP status code
     * @param error error code name
     * @param message error message
     * @param path request path
     * @param data additional context data
     * @return new ErrorResponse with current timestamp
     */
    public static ErrorResponse of(int status, String error, String message, String path, Map<String, Object> data) {
        return new ErrorResponse(
            Instant.now().toString(),
            status,
            error,
            message,
            path,
            data != null ? data : Map.of()
        );
    }

    /**
     * Create an ErrorResponse without additional data.
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return of(status, error, message, path, Map.of());
    }
}
