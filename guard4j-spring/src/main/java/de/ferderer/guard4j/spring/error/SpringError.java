package de.ferderer.guard4j.spring.error;

import de.ferderer.guard4j.classification.Category;
import de.ferderer.guard4j.classification.HttpStatus;
import de.ferderer.guard4j.classification.Level;
import de.ferderer.guard4j.error.Error;

/**
 * Spring-specific error codes that map common Spring exceptions to structured error responses.
 *
 * <p>This enum provides comprehensive coverage of Spring Framework exceptions with appropriate
 * HTTP status codes, severity levels, and categorization for observability purposes.
 */
public enum SpringError implements Error {

    // === Authentication & Authorization ===
    AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied to requested resource", Level.WARN, Category.SECURITY),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid username or password", Level.WARN, Category.SECURITY),
    AUTH_INSUFFICIENT_AUTHENTICATION(HttpStatus.UNAUTHORIZED, "Insufficient authentication for requested resource", Level.WARN, Category.SECURITY),
    AUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "Authentication failed", Level.WARN, Category.SECURITY),
    AUTH_ACCOUNT_EXPIRED(HttpStatus.UNAUTHORIZED, "User account has expired", Level.WARN, Category.SECURITY),
    AUTH_ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "User account is locked", Level.WARN, Category.SECURITY),
    AUTH_CREDENTIALS_EXPIRED(HttpStatus.UNAUTHORIZED, "User credentials have expired", Level.WARN, Category.SECURITY),
    AUTH_ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "User account is disabled", Level.WARN, Category.SECURITY),
    AUTH_SECURITY_VIOLATION(HttpStatus.FORBIDDEN, "Security violation detected", Level.ERROR, Category.SECURITY),

    // === Input Validation & Data ===
    VALIDATION_INVALID_INPUT(HttpStatus.BAD_REQUEST, "Input validation failed", Level.WARN, Category.VALIDATION),
    VALIDATION_MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "Required field is missing", Level.WARN, Category.VALIDATION),
    VALIDATION_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "Field format is invalid", Level.WARN, Category.VALIDATION),
    VALIDATION_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed for this endpoint", Level.WARN, Category.VALIDATION),
    VALIDATION_INVALID_JSON(HttpStatus.BAD_REQUEST, "Invalid JSON format in request body", Level.WARN, Category.VALIDATION),
    VALIDATION_MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "Required request parameter is missing", Level.WARN, Category.VALIDATION),
    VALIDATION_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "Parameter type mismatch", Level.WARN, Category.VALIDATION),
    VALIDATION_BINDING_ERROR(HttpStatus.BAD_REQUEST, "Request binding failed", Level.WARN, Category.VALIDATION),
    VALIDATION_CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "Constraint validation failed", Level.WARN, Category.VALIDATION),
    VALIDATION_INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "Invalid argument provided", Level.WARN, Category.VALIDATION),

    // === Data & Resource Management ===
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "Requested resource not found", Level.INFO, Category.BUSINESS),
    DATA_DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "Resource already exists", Level.WARN, Category.BUSINESS),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "Data integrity constraint violated", Level.WARN, Category.BUSINESS),
    DATA_OPTIMISTIC_LOCK_FAILURE(HttpStatus.CONFLICT, "Resource was modified by another process", Level.WARN, Category.BUSINESS),
    DATA_PESSIMISTIC_LOCK_FAILURE(HttpStatus.CONFLICT, "Could not acquire database lock", Level.WARN, Category.BUSINESS),
    DATA_LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "Failed to acquire required lock", Level.WARN, Category.BUSINESS),
    DATA_INCONSISTENT_STATE(HttpStatus.CONFLICT, "Data in inconsistent state", Level.WARN, Category.BUSINESS),

    // === Business Logic ===
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_ENTITY, "Business rule validation failed", Level.WARN, Category.BUSINESS),
    BUSINESS_OPERATION_NOT_ALLOWED(HttpStatus.FORBIDDEN, "Operation not allowed in current state", Level.WARN, Category.BUSINESS),
    BUSINESS_INVALID_STATE(HttpStatus.CONFLICT, "Invalid state for requested operation", Level.WARN, Category.BUSINESS),

    // === External Services ===
    EXTERNAL_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "External service is unavailable", Level.WARN, Category.EXTERNAL),
    EXTERNAL_SERVICE_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "External service request timeout", Level.WARN, Category.EXTERNAL),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "External API returned an error", Level.ERROR, Category.EXTERNAL),

    // === System & Infrastructure ===
    SYSTEM_DATABASE_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "Database connection failed", Level.ERROR, Category.SYSTEM),
    SYSTEM_DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed", Level.ERROR, Category.SYSTEM),
    SYSTEM_TRANSACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Database transaction failed", Level.ERROR, Category.SYSTEM),
    SYSTEM_DATABASE_TEMPORARY_FAILURE(HttpStatus.SERVICE_UNAVAILABLE, "Temporary database failure", Level.WARN, Category.SYSTEM),
    SYSTEM_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Input/output operation failed", Level.ERROR, Category.SYSTEM),
    SYSTEM_CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "System configuration error", Level.ERROR, Category.SYSTEM),

    // === Rate Limiting ===
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded", Level.WARN, Category.SYSTEM),
    QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Quota limit exceeded", Level.WARN, Category.BUSINESS),

    // === Generic/Fallback ===
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error occurred", Level.ERROR, Category.SYSTEM),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error occurred", Level.ERROR, Category.SYSTEM),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "Feature not implemented", Level.WARN, Category.SYSTEM),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable", Level.WARN, Category.SYSTEM);

    private final HttpStatus httpStatus;
    private final String message;
    private final Level level;
    private final Category category;

    SpringError(HttpStatus httpStatus, String message, Level level, Category category) {
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

    /**
     * Map a Spring exception to the corresponding SpringError.
     * Uses pattern matching for clean exception mapping.
     * Returns UNKNOWN_ERROR for unmapped exceptions.
     */
    public static SpringError fromException(Throwable ex) {
        if (ex == null) {
            return UNKNOWN_ERROR;
        }

        return switch (ex.getClass().getName()) {
            // Spring Security (when available)
            case "org.springframework.security.access.AccessDeniedException" -> AUTH_ACCESS_DENIED;
            case "org.springframework.security.authentication.BadCredentialsException" -> AUTH_INVALID_CREDENTIALS;
            case "org.springframework.security.authentication.InsufficientAuthenticationException" -> AUTH_INSUFFICIENT_AUTHENTICATION;

            // Spring Web
            case "org.springframework.web.servlet.NoHandlerFoundException" -> DATA_NOT_FOUND;
            case "org.springframework.web.HttpRequestMethodNotSupportedException" -> VALIDATION_METHOD_NOT_ALLOWED;
            case "org.springframework.web.HttpMessageNotReadableException" -> VALIDATION_INVALID_JSON;
            case "org.springframework.web.bind.MissingServletRequestParameterException" -> VALIDATION_MISSING_PARAMETER;
            case "org.springframework.web.method.annotation.MethodArgumentTypeMismatchException" -> VALIDATION_TYPE_MISMATCH;
            case "org.springframework.web.bind.MethodArgumentNotValidException" -> VALIDATION_INVALID_INPUT;
            case "org.springframework.validation.BindException" -> VALIDATION_BINDING_ERROR;

            // Validation
            case "jakarta.validation.ConstraintViolationException" -> VALIDATION_CONSTRAINT_VIOLATION;

            // Spring Data/DAO
            case "org.springframework.dao.DataAccessResourceFailureException" -> SYSTEM_DATABASE_CONNECTION_FAILED;
            case "org.springframework.dao.DuplicateKeyException" -> DATA_DUPLICATE_RESOURCE;
            case "org.springframework.dao.DataIntegrityViolationException" -> DATA_INTEGRITY_VIOLATION;
            case "org.springframework.dao.EmptyResultDataAccessException" -> DATA_NOT_FOUND;
            case "org.springframework.dao.OptimisticLockingFailureException" -> DATA_OPTIMISTIC_LOCK_FAILURE;
            case "org.springframework.dao.CannotAcquireLockException" -> DATA_LOCK_ACQUISITION_FAILED;
            case "org.springframework.dao.PessimisticLockingFailureException" -> DATA_PESSIMISTIC_LOCK_FAILURE;
            case "org.springframework.transaction.CannotCreateTransactionException" -> SYSTEM_TRANSACTION_FAILED;
            case "org.springframework.dao.TransientDataAccessException" -> SYSTEM_DATABASE_TEMPORARY_FAILURE;

            // JPA/Hibernate (when available)
            case "jakarta.persistence.EntityNotFoundException" -> DATA_NOT_FOUND;
            case "jakarta.persistence.OptimisticLockException" -> DATA_OPTIMISTIC_LOCK_FAILURE;
            case "org.springframework.orm.jpa.JpaObjectRetrievalFailureException" -> DATA_NOT_FOUND;
            case "jakarta.persistence.PersistenceException" -> SYSTEM_DATABASE_ERROR;

            // Common Java
            case "java.lang.IllegalArgumentException" -> VALIDATION_INVALID_ARGUMENT;
            case "java.lang.IllegalStateException" -> BUSINESS_INVALID_STATE;
            case "java.lang.UnsupportedOperationException" -> NOT_IMPLEMENTED;
            case "java.lang.SecurityException" -> AUTH_SECURITY_VIOLATION;

            // Network/IO
            case "java.net.SocketTimeoutException" -> EXTERNAL_SERVICE_TIMEOUT;
            case "java.net.ConnectException" -> EXTERNAL_SERVICE_UNAVAILABLE;
            case "java.io.IOException" -> SYSTEM_IO_ERROR;

            default -> UNKNOWN_ERROR;
        };
    }

    /**
     * Map exception with fallback to INTERNAL_SERVER_ERROR for critical system errors.
     * This provides a stronger fallback than UNKNOWN_ERROR for cases where
     * a generic server error response is preferred.
     */
    public static SpringError fromExceptionWithFallback(Throwable exception) {
        SpringError error = fromException(exception);
        return (error == UNKNOWN_ERROR) ? INTERNAL_SERVER_ERROR : error;
    }

}
