package de.ferderer.guard4j.error;

import de.ferderer.guard4j.classification.Category;
import de.ferderer.guard4j.classification.HttpStatus;
import de.ferderer.guard4j.classification.Level;
import java.io.Serializable;

/**
 * Core interface for all error codes in the Guard4j system.
 *
 * @since 1.0.0
 */
public interface Error extends Serializable {

    /**
     * Unique identifier for this error code.
     * For enums, this returns Enum.name().
     * For custom implementations, must be unique within the application.
     *
     * @return the unique error code identifier
     */
    String name();

    /**
     * HTTP status code to return for this error.
     *
     * @return the HTTP status for this error
     */
    HttpStatus httpStatus();

    /**
     * Internationalization message reference.
     * Contains message key and default fallback text.
     *
     * @return the message for this error
     */
    String message();

    /**
     * Severity level for logging and monitoring.
     *
     * @return the severity level
     */
    Level level();

    /**
     * Category for error classification and retry logic.
     *
     * @return the error category
     */
    Category category();

    // Derived methods with default implementations

    /**
     * Whether this error type should be retryable by clients.
     * Based on category: SYSTEM and EXTERNAL are retryable.
     *
     * @return true if the error is retryable
     */
    default boolean isRetryable() {
        return category().isRetryable();
    }

    /**
     * Client-friendly error code (kebab-case).
     * Converts enum names like AUTH_ACCESS_DENIED to auth-access-denied.
     *
     * @return the client-friendly error code
     */
    default String code() {
        return name().toLowerCase().replace('_', '-');
    }
}
