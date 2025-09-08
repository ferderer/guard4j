package de.ferderer.guard4j.spring.autoconfigure;

import de.ferderer.guard4j.observability.ContextConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Map;

/**
 * Configuration properties for Guard4j Spring Boot integration.
 *
 * <p>All Guard4j configuration can be customized via application properties
 * using the {@code guard4j} prefix.
 *
 * <h3>Example Configuration:</h3>
 * <pre>{@code
 * guard4j:
 *   enabled: true
 *   include-debug-info: false
 *   include-stack-trace: false
 *
 *   observability:
 *     metrics-enabled: true
 *     metrics-prefix: "myapp"  # defaults to spring.application.name
 *     logging-enabled: true
 *     context:
 *       enabled: true
 *       include-trace-id: true
 *       include-user-id: true
 *       include-correlation-id: true
 *
 *   level-overrides:
 *     TOKEN_EXPIRED: DEBUG
 *     RESOURCE_NOT_FOUND: INFO
 * }</pre>
 */
@ConfigurationProperties(prefix = "guard4j")
public record Guard4jProperties(
    @DefaultValue("true") boolean enabled,
    @DefaultValue("false") boolean includeDebugInfo,
    @DefaultValue("false") boolean includeStackTrace,
    Map<String, String> levelOverrides,
    Observability observability,
    Web web
) {
    
    /**
     * Get observability configuration with default fallback.
     */
    public Observability getObservabilityOrDefault() {
        if (observability != null) {
            // If observability is not null but context is null, create a new one with default context
            if (observability.context() == null) {
                return new Observability(
                    observability.metricsEnabled(),
                    observability.metricsPrefix(),
                    observability.loggingEnabled(),
                    observability.includeMdc(),
                    new ContextConfig()
                );
            }
            return observability;
        }
        return new Observability(true, null, true, true, new ContextConfig());
    }
    
    /**
     * Get web configuration with default fallback.
     */
    public Web getWebOrDefault() {
        return web != null ? web : new Web(true, true, -100);
    }

    /**
     * Observability-related configuration.
     * 
     * @param metricsEnabled enable metrics collection
     * @param metricsPrefix metric name prefix (defaults to application name)
     * @param loggingEnabled enable enhanced logging with MDC
     * @param includeMdc include MDC context in logs
     * @param context context extraction configuration
     */
    public record Observability(
        @DefaultValue("true") boolean metricsEnabled,
        String metricsPrefix,
        @DefaultValue("true") boolean loggingEnabled,
        @DefaultValue("true") boolean includeMdc,
        ContextConfig context
    ) {
    }

    /**
     * Web-specific configuration.
     * 
     * @param enabled enable web exception handling
     * @param handleSpringExceptions handle Spring framework exceptions (not just AppException)
     * @param handlerOrder exception handler order (lower values = higher priority)
     */
    public record Web(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("true") boolean handleSpringExceptions,
        @DefaultValue("-100") int handlerOrder
    ) {
    }
}