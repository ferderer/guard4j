package de.ferderer.guard4j.classification;

import java.util.Objects;

/**
 * Framework-agnostic HTTP status representation.
 *
 * <p>This interface provides a bridge between Guard4j's error handling
 * and various framework-specific HTTP status representations.
 *
 * <h3>Usage Examples</h3>
 *
 * <h4>Using predefined constants:</h4>
 * <pre>{@code
 *     throw new AppException(ErrorCodes.VALIDATION_FAILED);
 * }</pre>
 *
 * <h4>Creating custom HTTP statuses:</h4>
 * <pre>{@code
 // Direct creation
 HttpStatus custom = HttpStatus.of(418, "I'm a fridge");

 // Organized as enum constants
 public enum CustomHttpStatus implements HttpStatus {
     IM_A_FRIDGE(418, "I'm a fridge"),
     BUSINESS_ERROR(499, "Business Rule Violation");

     private final int value;
     private final String reason;

     CustomHttpStatus(int value, String reason) {
         this.value = value;
         this.reason = reason;
     }

     public int value() { return value; }
     public String reason() { return reason; }
 }
 }</pre>
 *
 * @since 1.0.0
 */
public interface HttpStatus {

    /**
     * HTTP status code (e.g., 200, 404, 500).
     *
     * @return the HTTP status code
     */
    int value();

    /**
     * HTTP reason phrase (e.g., "OK", "Not Found", "Internal Server Error").
     *
     * @return the HTTP reason phrase
     */
    String reason();

    // Common 2xx status codes
    HttpStatus OK = HttpStatus.of(200, "OK");
    HttpStatus CREATED = HttpStatus.of(201, "Created");
    HttpStatus ACCEPTED = HttpStatus.of(202, "Accepted");
    HttpStatus NO_CONTENT = HttpStatus.of(204, "No Content");

    // Common 4xx client error codes
    HttpStatus BAD_REQUEST = HttpStatus.of(400, "Bad Request");
    HttpStatus UNAUTHORIZED = HttpStatus.of(401, "Unauthorized");
    HttpStatus PAYMENT_REQUIRED = HttpStatus.of(402, "Payment Required");
    HttpStatus FORBIDDEN = HttpStatus.of(403, "Forbidden");
    HttpStatus NOT_FOUND = HttpStatus.of(404, "Not Found");
    HttpStatus METHOD_NOT_ALLOWED = HttpStatus.of(405, "Method Not Allowed");
    HttpStatus NOT_ACCEPTABLE = HttpStatus.of(406, "Not Acceptable");
    HttpStatus CONFLICT = HttpStatus.of(409, "Conflict");
    HttpStatus GONE = HttpStatus.of(410, "Gone");
    HttpStatus PRECONDITION_FAILED = HttpStatus.of(412, "Precondition Failed");
    HttpStatus PAYLOAD_TOO_LARGE = HttpStatus.of(413, "Payload Too Large");
    HttpStatus UNSUPPORTED_MEDIA_TYPE = HttpStatus.of(415, "Unsupported Media Type");
    HttpStatus UNPROCESSABLE_ENTITY = HttpStatus.of(422, "Unprocessable Entity");
    HttpStatus LOCKED = HttpStatus.of(423, "Locked");
    HttpStatus TOO_MANY_REQUESTS = HttpStatus.of(429, "Too Many Requests");

    // Common 5xx server error codes
    HttpStatus INTERNAL_SERVER_ERROR = HttpStatus.of(500, "Internal Server Error");
    HttpStatus NOT_IMPLEMENTED = HttpStatus.of(501, "Not Implemented");
    HttpStatus BAD_GATEWAY = HttpStatus.of(502, "Bad Gateway");
    HttpStatus SERVICE_UNAVAILABLE = HttpStatus.of(503, "Service Unavailable");
    HttpStatus GATEWAY_TIMEOUT = HttpStatus.of(504, "Gateway Timeout");
    HttpStatus HTTP_VERSION_NOT_SUPPORTED = HttpStatus.of(505, "HTTP Version Not Supported");
    HttpStatus INSUFFICIENT_STORAGE = HttpStatus.of(507, "Insufficient Storage");

    /**
     * Create custom HTTP status.
     *
     * @param value the HTTP status code
     * @param reason the HTTP reason phrase
     * @return a new HttpStatus instance
     */
    static HttpStatus of(int value, String reason) {
        return new SimpleHttpStatus(value, reason);
    }

    /**
     * Check if this is an informational status (1xx).
     */
    default boolean is1xxInformational() {
        return value() >= 100 && value() < 200;
    }

    /**
     * Check if this is a successful status (2xx).
     */
    default boolean is2xxSuccessful() {
        return value() >= 200 && value() < 300;
    }

    /**
     * Check if this is a redirection status (3xx).
     */
    default boolean is3xxRedirection() {
        return value() >= 300 && value() < 400;
    }

    /**
     * Check if this is a client error status (4xx).
     */
    default boolean is4xxClientError() {
        return value() >= 400 && value() < 500;
    }

    /**
     * Check if this is a server error status (5xx).
     */
    default boolean is5xxServerError() {
        return value() >= 500 && value() < 600;
    }

    /**
     * Check if this represents an error status (4xx or 5xx).
     */
    default boolean isError() {
        return is4xxClientError() || is5xxServerError();
    }

    record SimpleHttpStatus(int value, String reason) implements HttpStatus {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof SimpleHttpStatus other && value == other.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return value + " " + reason;
        }
    }
}
