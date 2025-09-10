package de.ferderer.guard4j.examples.finstream.common.error;

import de.ferderer.guard4j.classification.Category;
import de.ferderer.guard4j.classification.HttpStatus;
import de.ferderer.guard4j.classification.Level;
import de.ferderer.guard4j.error.Error;

/**
 * FinStream-specific error codes demonstrating Guard4j's comprehensive error handling.
 *
 * This enum showcases all major error categories that can occur in a real-time
 * stock price streaming application, providing realistic scenarios for demonstrating
 * Guard4j's framework-agnostic error handling capabilities.
 *
 * <h3>Error Categories Demonstrated:</h3>
 * <ul>
 *   <li><strong>External Service Errors</strong> - Finnhub API integration failures</li>
 *   <li><strong>Validation Errors</strong> - Input validation and format checking</li>
 *   <li><strong>Business Logic Errors</strong> - Stock market business rules</li>
 *   <li><strong>System Errors</strong> - WebSocket and internal failures</li>
 *   <li><strong>Security Errors</strong> - Authentication and authorization</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // External service timeout with context
 * throw new AppException(FinStreamError.FINNHUB_TIMEOUT)
 *     .withData("symbol", "AAPL")
 *     .withData("timeoutMs", 5000);
 *
 * // Validation error with user input
 * throw new AppException(FinStreamError.INVALID_STOCK_SYMBOL)
 *     .withData("symbol", "INVALID123")
 *     .withData("expectedFormat", "1-5 uppercase letters");
 * }</pre>
 *
 * @author Guard4j Examples Team
 * @version 1.0.0
 * @see de.ferderer.guard4j.error.AppException
 */
public enum FinStreamError implements Error {

    // =========================================================================
    // EXTERNAL SERVICE ERRORS - Finnhub API Integration
    // =========================================================================

    /**
     * Finnhub API service is completely unavailable.
     * Typically occurs during Finnhub maintenance or network outages.
     */
    FINNHUB_SERVICE_UNAVAILABLE(
        HttpStatus.SERVICE_UNAVAILABLE,
        "Stock price service unavailable",
        Level.ERROR,
        Category.EXTERNAL
    ),

    /**
     * Finnhub API rate limit exceeded.
     * Free tier: 60 calls/minute, paid tiers have higher limits.
     * This is a common scenario that needs graceful handling.
     */
    FINNHUB_RATE_LIMIT_EXCEEDED(
        HttpStatus.TOO_MANY_REQUESTS,
        "Stock API rate limit exceeded",
        Level.WARN,
        Category.EXTERNAL
    ),

    /**
     * Request to Finnhub API timed out.
     * Network delays or Finnhub server overload.
     */
    FINNHUB_TIMEOUT(
        HttpStatus.GATEWAY_TIMEOUT,
        "Stock price request timeout",
        Level.WARN,
        Category.EXTERNAL
    ),

    /**
     * Finnhub API returned invalid or malformed response.
     * JSON parsing errors, missing required fields, etc.
     */
    FINNHUB_INVALID_RESPONSE(
        HttpStatus.BAD_GATEWAY,
        "Invalid response from stock service",
        Level.ERROR,
        Category.EXTERNAL
    ),

    /**
     * Finnhub API authentication failed.
     * Invalid API key or expired subscription.
     */
    FINNHUB_AUTHENTICATION_FAILED(
        HttpStatus.UNAUTHORIZED,
        "Stock service authentication failed",
        Level.ERROR,
        Category.EXTERNAL
    ),

    // =========================================================================
    // VALIDATION ERRORS - Input Validation
    // =========================================================================

    /**
     * Stock symbol format is invalid.
     * Must be 1-5 uppercase letters (e.g., AAPL, GOOGL).
     */
    INVALID_STOCK_SYMBOL(
        HttpStatus.BAD_REQUEST,
        "Invalid stock symbol format",
        Level.WARN,
        Category.VALIDATION
    ),

    /**
     * Required stock symbol parameter is missing.
     */
    MISSING_STOCK_SYMBOL(
        HttpStatus.BAD_REQUEST,
        "Stock symbol is required",
        Level.WARN,
        Category.VALIDATION
    ),

    /**
     * Search query is too short or invalid.
     * Minimum 1 character required for stock symbol search.
     */
    INVALID_SEARCH_QUERY(
        HttpStatus.BAD_REQUEST,
        "Search query must be at least 1 character",
        Level.WARN,
        Category.VALIDATION
    ),

    // =========================================================================
    // BUSINESS LOGIC ERRORS - Stock Market Rules
    // =========================================================================

    /**
     * Requested stock symbol was not found.
     * Symbol doesn't exist or is delisted.
     */
    STOCK_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "Stock symbol not found",
        Level.INFO,
        Category.BUSINESS
    ),

    /**
     * Stock market is currently closed.
     * Trading hours: 9:30 AM - 4:00 PM ET, Monday-Friday.
     */
    MARKET_CLOSED(
        HttpStatus.UNPROCESSABLE_ENTITY,
        "Market is currently closed",
        Level.INFO,
        Category.BUSINESS
    ),

    /**
     * Stock trading is suspended.
     * Circuit breakers, news pending, or regulatory halt.
     */
    TRADING_SUSPENDED(
        HttpStatus.UNPROCESSABLE_ENTITY,
        "Trading suspended for this stock",
        Level.WARN,
        Category.BUSINESS
    ),

    // =========================================================================
    // SYSTEM ERRORS - WebSocket and Internal
    // =========================================================================

    /**
     * WebSocket connection failed to establish.
     * Network issues, server overload, or client incompatibility.
     */
    WEBSOCKET_CONNECTION_FAILED(
        HttpStatus.SERVICE_UNAVAILABLE,
        "Real-time connection failed",
        Level.ERROR,
        Category.SYSTEM
    ),

    /**
     * Failed to broadcast price update via WebSocket.
     * Client disconnection, message too large, or serialization error.
     */
    PRICE_BROADCAST_FAILED(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Failed to broadcast price update",
        Level.ERROR,
        Category.SYSTEM
    ),

    /**
     * Stock price cache operation failed.
     * Redis connection issues or memory constraints.
     */
    CACHE_OPERATION_FAILED(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Cache operation failed",
        Level.WARN,
        Category.SYSTEM
    ),

    /**
     * Internal configuration error.
     * Missing environment variables, invalid settings.
     */
    CONFIGURATION_ERROR(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Application configuration error",
        Level.ERROR,
        Category.SYSTEM
    ),

    // =========================================================================
    // SECURITY ERRORS - Authentication and Authorization
    // =========================================================================

    /**
     * Authentication is required for this operation.
     * User must provide valid JWT token.
     */
    AUTHENTICATION_REQUIRED(
        HttpStatus.UNAUTHORIZED,
        "Authentication required",
        Level.WARN,
        Category.SECURITY
    ),

    /**
     * Provided authentication token is invalid.
     * Expired, malformed, or revoked JWT token.
     */
    INVALID_TOKEN(
        HttpStatus.UNAUTHORIZED,
        "Invalid authentication token",
        Level.WARN,
        Category.SECURITY
    ),

    /**
     * User doesn't have permission for this operation.
     * Insufficient role or scope in JWT token.
     */
    ACCESS_DENIED(
        HttpStatus.FORBIDDEN,
        "Access denied",
        Level.WARN,
        Category.SECURITY
    ),

    /**
     * Too many authentication attempts.
     * Rate limiting for security purposes.
     */
    TOO_MANY_AUTH_ATTEMPTS(
        HttpStatus.TOO_MANY_REQUESTS,
        "Too many authentication attempts",
        Level.WARN,
        Category.SECURITY
    );

    // =========================================================================
    // ENUM IMPLEMENTATION
    // =========================================================================

    private final HttpStatus httpStatus;
    private final String message;
    private final Level level;
    private final Category category;

    FinStreamError(HttpStatus httpStatus, String message, Level level, Category category) {
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
