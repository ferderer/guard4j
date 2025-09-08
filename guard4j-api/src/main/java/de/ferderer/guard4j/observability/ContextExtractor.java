package de.ferderer.guard4j.observability;

import java.util.Map;
import java.util.Optional;

/**
 * Interface for extracting context information from the current request/execution context.
 * 
 * <p>Context extractors are responsible for gathering observability context from various
 * sources like HTTP headers, MDC (Mapped Diagnostic Context), Spring Security, and
 * distributed tracing systems. This context enriches observability events with
 * information needed for correlation and debugging.
 * 
 * <p>Implementations should be lightweight and handle missing context gracefully.
 * All methods should return empty Optional/Map when context is not available
 * rather than throwing exceptions.
 * 
 * @since 2.1.0
 */
public interface ContextExtractor {
    
    /**
     * Extract all available context from the current execution context.
     * 
     * <p>This method combines results from all specific extraction methods
     * plus any custom fields configured in the ContextConfig. Implementation
     * should gather context from all available sources in the current execution
     * environment.
     * 
     * @return map of context key-value pairs, never null but may be empty
     */
    Map<String, String> extractContext();
    
    /**
     * Extract the current trace ID from distributed tracing systems.
     * 
     * <p>Common sources include:
     * <ul>
     *   <li>Micrometer Tracing MDC keys</li>
     *   <li>Spring Cloud Sleuth MDC keys</li>
     *   <li>OpenTelemetry span context</li>
     *   <li>HTTP headers like X-Trace-Id</li>
     * </ul>
     * 
     * @return the trace ID if available
     */
    Optional<String> extractTraceId();
    
    /**
     * Extract the current user ID from security context.
     * 
     * <p>Common sources include:
     * <ul>
     *   <li>Spring Security Authentication principal</li>
     *   <li>JWT token claims</li>
     *   <li>HTTP headers like X-User-Id</li>
     *   <li>Session attributes</li>
     * </ul>
     * 
     * @return the user ID if available
     */
    Optional<String> extractUserId();
    
    /**
     * Extract the correlation ID for request tracking.
     * 
     * <p>Common sources include:
     * <ul>
     *   <li>HTTP headers like X-Correlation-ID, X-Request-ID</li>
     *   <li>MDC keys set by request filters</li>
     *   <li>Generated request identifiers</li>
     * </ul>
     * 
     * @return the correlation ID if available
     */
    Optional<String> extractCorrelationId();
}
