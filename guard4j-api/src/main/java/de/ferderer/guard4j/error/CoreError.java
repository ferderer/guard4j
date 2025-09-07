package de.ferderer.guard4j.error;

import de.ferderer.guard4j.classification.Category;
import de.ferderer.guard4j.classification.HttpStatus;
import de.ferderer.guard4j.classification.Level;

/**
 * Standard error codes provided by Guard4j.
 *
 * <p>These error codes cover common scenarios across most applications.
 * Applications can use these directly or create their own domain-specific
 * error codes by implementing the {@link Error} interface.
 *
 * <h3>Categories Covered</h3>
 * <ul>
 *   <li><strong>Validation Errors</strong> - Input validation failures</li>
 *   <li><strong>Authentication & Authorization</strong> - Security-related errors</li>
 *   <li><strong>Resource Management</strong> - Not found, conflicts, etc.</li>
 *   <li><strong>System Errors</strong> - Internal failures</li>
 *   <li><strong>External Service Errors</strong> - Third-party service issues</li>
 *   <li><strong>Rate Limiting</strong> - Throttling and quota errors</li>
 * </ul>
 *
 * @since 1.0.0
 */
public enum CoreError implements Error {

    // Validation Errors (400 Bad Request)
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed", Level.WARN, Category.VALIDATION),
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "Required field is missing", Level.WARN, Category.VALIDATION),
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, "Invalid format", Level.WARN, Category.VALIDATION),
    INVALID_VALUE(HttpStatus.BAD_REQUEST, "Invalid value", Level.WARN, Category.VALIDATION),

    // Authentication & Authorization (401/403)
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "Authentication required", Level.WARN, Category.SECURITY),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials", Level.WARN, Category.SECURITY),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Authentication token has expired", Level.INFO, Category.SECURITY),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied", Level.WARN, Category.SECURITY),
    INSUFFICIENT_PERMISSIONS(HttpStatus.FORBIDDEN, "Insufficient permissions", Level.WARN, Category.SECURITY),

    // Resource Management (404/409/410)
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found", Level.INFO, Category.BUSINESS),
    RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Resource already exists", Level.WARN, Category.BUSINESS),
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "Resource conflict", Level.WARN, Category.BUSINESS),
    RESOURCE_GONE(HttpStatus.GONE, "Resource no longer available", Level.INFO, Category.BUSINESS),

    // Business Logic Errors (422)
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violation", Level.WARN, Category.BUSINESS),
    OPERATION_NOT_ALLOWED(HttpStatus.UNPROCESSABLE_ENTITY, "Operation not allowed", Level.WARN, Category.BUSINESS),
    INSUFFICIENT_FUNDS(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient funds", Level.INFO, Category.BUSINESS),

    // Rate Limiting (429)
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded", Level.WARN, Category.SYSTEM),
    QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Quota exceeded", Level.WARN, Category.BUSINESS),

    // System Errors (500)
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", Level.ERROR, Category.SYSTEM),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Database error", Level.ERROR, Category.SYSTEM),
    CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Configuration error", Level.ERROR, Category.SYSTEM),

    // External Service Errors (502/503/504)
    EXTERNAL_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "External service error", Level.ERROR, Category.EXTERNAL),
    EXTERNAL_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "External service unavailable", Level.WARN, Category.EXTERNAL),
    EXTERNAL_SERVICE_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "External service timeout", Level.WARN, Category.EXTERNAL);

    private final HttpStatus httpStatus;
    private final String message;
    private final Level level;
    private final Category category;

    CoreError(HttpStatus httpStatus, String message, Level level, Category category) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.level = level;
        this.category = category;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public Level level() {
        return level;
    }

    @Override
    public Category category() {
        return category;
    }
}
