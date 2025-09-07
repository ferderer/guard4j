package de.ferderer.guard4j.error;

import de.ferderer.guard4j.observability.ObservableEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Main exception class for Guard4j error handling.
 *
 * <p>AppException wraps an {@link Error} and provides a fluent API
 * for adding contextual data. This exception is designed to be caught
 * by framework-specific exception handlers that convert it to appropriate
 * HTTP responses.
 *
 * <p>AppException also implements {@link ObservableEvent}, allowing it to be
 * logged directly as an observability event when needed.
 *
 * <h3>Basic Usage</h3>
 * <pre>{@code
 * // Simple business error
 * throw new AppException(ErrorCodes.RESOURCE_NOT_FOUND);
 *
 * // With additional context
 * throw new AppException(ErrorCodes.INSUFFICIENT_FUNDS)
 *     .withData("accountId", "ACC-123")
 *     .withData("requestedAmount", 1000.00)
 *     .withData("availableBalance", 250.00);
 * }</pre>
 *
 * <h3>Framework Integration</h3>
 * <pre>{@code
 * // In a service class
 * public void processPayment(String accountId, BigDecimal amount) {
 *     Account account = accountRepository.findById(accountId)
 *         .orElseThrow(() -> new AppException(ErrorCodes.RESOURCE_NOT_FOUND)
 *             .withData("accountId", accountId));
 *
 *     if (account.getBalance().compareTo(amount) < 0) {
 *         throw new AppException(ErrorCodes.INSUFFICIENT_FUNDS)
 *             .withData("accountId", accountId)
 *             .withData("requestedAmount", amount)
 *             .withData("availableBalance", account.getBalance());
 *     }
 *
 *     // Process payment...
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public class AppException extends RuntimeException {

    private final Error errorCode;
    private final Map<String, Object> data = new HashMap<>();

    /**
     * Create an AppException with the specified error code.
     *
     * @param errorCode the error code that describes this exception
     * @throws NullPointerException if errorCode is null
     */
    public AppException(Error errorCode) {
        super(errorCode.message());
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
    }

    /**
     * Create an AppException with the specified error code and cause.
     *
     * <p>This constructor is useful when wrapping lower-level exceptions
     * (e.g., database exceptions, external service exceptions) into
     * business-meaningful error codes.
     *
     * @param errorCode the error code that describes this exception
     * @param cause the underlying cause of this exception
     * @throws NullPointerException if errorCode is null
     */
    public AppException(Error errorCode, Throwable cause) {
        super(errorCode.message(), cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
    }

    /**
     * Get the error code associated with this exception.
     *
     * @return the error code
     */
    public Error errorCode() {
        return errorCode;
    }

    /**
     * Get the contextual data associated with this exception.
     *
     * @return the contextual data map
     */
    public Map<String, Object> data() {
        return data;
    }

    /**
     * Add contextual data to this exception.
     *
     * <p>Returns a new AppException instance with the additional data.
     * This method follows the fluent API pattern for easy chaining.
     *
     * @param key the data key
     * @param value the data value (null values are allowed)
     * @return a new AppException with the additional data
     * @throws NullPointerException if key is null
     */
    public AppException withData(String key, Object value) {
        Objects.requireNonNull(key, "data key cannot be null");
        data.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return "AppException{" +
                "errorCode=" + errorCode.name() +
                ", message='" + getMessage() + '\'' +
                ", data=" + data +
                '}';
    }
}
