package de.ferderer.guard4j.classification;

/**
 * Error categories for classification and retry logic.
 *
 * <p>Categorizes errors by their nature to enable intelligent handling:
 * <ul>
 *   <li><strong>BUSINESS</strong> - Expected business logic violations (not retryable)</li>
 *   <li><strong>VALIDATION</strong> - Input validation failures (not retryable)</li>
 *   <li><strong>SECURITY</strong> - Authentication/authorization failures (not retryable)</li>
 *   <li><strong>SYSTEM</strong> - Internal system errors (retryable)</li>
 *   <li><strong>EXTERNAL</strong> - External service failures (retryable)</li>
 * </ul>
 *
 * @since 1.0.0
 */
public enum Category {

    /**
     * Business logic violations.
     *
     * <p>Examples: insufficient funds, order already shipped, product out of stock.
     * These represent expected business rules and should not be retried.
     */
    BUSINESS(false),

    /**
     * Input validation failures.
     *
     * <p>Examples: missing required fields, invalid email format, malformed JSON.
     * These indicate client errors and should not be retried.
     */
    VALIDATION(false),

    /**
     * Security-related errors.
     *
     * <p>Examples: invalid credentials, insufficient permissions, expired tokens.
     * These should not be retried to avoid security issues.
     */
    SECURITY(false),

    /**
     * Internal system errors.
     *
     * <p>Examples: database connection failures, configuration errors, internal bugs.
     * These may be transient and could be retried.
     */
    SYSTEM(true),

    /**
     * External service failures.
     *
     * <p>Examples: payment gateway timeouts, third-party API errors, network issues.
     * These are often transient and should be retried.
     */
    EXTERNAL(true);

    private final boolean retryable;

    Category(boolean retryable) {
        this.retryable = retryable;
    }

    /**
     * Whether errors in this category should be retryable by clients.
     *
     * @return true if errors in this category are retryable
     */
    public boolean isRetryable() {
        return retryable;
    }
}
