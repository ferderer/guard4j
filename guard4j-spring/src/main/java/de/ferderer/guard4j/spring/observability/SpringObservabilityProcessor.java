package de.ferderer.guard4j.spring.observability;

import de.ferderer.guard4j.classification.Level;
import de.ferderer.guard4j.observability.ContextExtractor;
import de.ferderer.guard4j.observability.ObservabilityProcessor;
import de.ferderer.guard4j.observability.ObservableEvent;
import de.ferderer.guard4j.spring.autoconfigure.Guard4jProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Spring Boot implementation of ObservabilityProcessor.
 *
 * <p>Provides comprehensive observability for Guard4j events using Spring Boot's
 * observability infrastructure:
 * <ul>
 *   <li>Micrometer metrics for error counting and timing</li>
 *   <li>Structured logging with MDC context</li>
 *   <li>Context extraction for enhanced correlation</li>
 *   <li>Integration with Spring Boot Actuator</li>
 * </ul>
 *
 * <p>Metrics are automatically registered with the configured {@link MeterRegistry}
 * and follow Spring Boot naming conventions for optimal integration with
 * monitoring systems like Prometheus, InfluxDB, or CloudWatch.
 *
 * <p>Logging uses class-based logger names in the new Emitter pattern, providing
 * familiar SLF4J-style log organization. MDC context includes:
 * <ul>
 *   <li>{@code guard4j.app} - The application name (from {@code spring.application.name})</li>
 *   <li>{@code guard4j.event.type} - The event type</li>
 *   <li>{@code guard4j.event.level} - The event level</li>
 *   <li>{@code guard4j.event.timestamp} - The event timestamp</li>
 *   <li>{@code guard4j.event.metric} - The event metric value</li>
 *   <li>Context fields from ContextExtractor (e.g., traceId, userId, correlationId)</li>
 * </ul>
 *
 * <p>This approach allows standard logger configuration in {@code logback-spring.xml}
 * while providing rich contextual information for filtering and routing logs in
 * multi-application environments.
 *
 * @since 2.1.0 - Added context extraction support
 */
@Component
public class SpringObservabilityProcessor implements ObservabilityProcessor {

    private static final Logger log = LoggerFactory.getLogger(SpringObservabilityProcessor.class);

    private final MeterRegistry meterRegistry;
    private final Guard4jProperties properties;
    private final String effectiveMetricsPrefix;
    private final String applicationName;
    
    // Context extraction support
    private ContextExtractor contextExtractor;

    // Cache for performance
    private final Map<String, Counter> counterCache = new ConcurrentHashMap<>();
    private final Map<String, Timer> timerCache = new ConcurrentHashMap<>();
    private final Map<String, Logger> loggerCache = new ConcurrentHashMap<>();

    public SpringObservabilityProcessor(MeterRegistry meterRegistry, Guard4jProperties properties,
                                      String applicationName) {
        this.meterRegistry = meterRegistry;
        this.properties = properties;
        this.applicationName = applicationName;
        this.effectiveMetricsPrefix = determineMetricsPrefix(properties, applicationName);
    }
    
    @Override
    public void setContextExtractor(ContextExtractor contextExtractor) {
        this.contextExtractor = contextExtractor;
        log.debug("Context extractor {} for observability processor", 
                 contextExtractor != null ? "enabled" : "disabled");
    }

    /**
     * Determine the effective metrics prefix.
     * Uses configured prefix if set, otherwise falls back to application name, then "guard4j".
     */
    private String determineMetricsPrefix(Guard4jProperties properties, String applicationName) {
        String configuredPrefix = properties.getObservabilityOrDefault().metricsPrefix();
        if (configuredPrefix != null && !configuredPrefix.trim().isEmpty()) {
            return configuredPrefix.trim();
        }
        
        // Fallback: use application name, but avoid generic names
        if (applicationName != null && !applicationName.equals("application") && !applicationName.trim().isEmpty()) {
            return applicationName.trim();
        }
        
        // Last resort: use "guard4j" as prefix
        return "guard4j";
    }    @Override
    public void process(ObservableEvent event) {
        // This method is kept for backward compatibility but should not be used in new Emitter pattern
        // Default to INFO level and use event type as logger name for compatibility
        processWithLevel(event, Level.INFO, "guard4j.events." + event.eventType());
    }

    @Override
    public void processWithLevel(ObservableEvent event, Level level, String loggerName) {
        try {
            // Process metrics if enabled
            if (properties.getObservabilityOrDefault().metricsEnabled()) {
                processMetrics(event, level);
            }

            // Process logging if enabled
            if (properties.getObservabilityOrDefault().loggingEnabled()) {
                processLogging(event, level, loggerName);
            }

        } catch (Exception e) {
            // Never let observability processing break the main application flow
            log.warn("Failed to process observability event: {}", event.eventType(), e);
        }
    }

    /**
     * Process metrics for the given event with the specified level.
     */
    private void processMetrics(ObservableEvent event, Level level) {
        String eventType = event.eventType();
        String levelName = level.name().toLowerCase();

        // Increment counter using event metric value
        String counterName = buildMetricName("events");
        getOrCreateCounter(counterName, eventType, levelName).increment(event.metric());

        // Record timer for error events (useful for tracking error frequency patterns)
        if (isErrorEvent(level)) {
            String timerName = buildMetricName("errors");
            getOrCreateTimer(timerName, eventType, levelName)
                .record(() -> { /* No operation - just recording the occurrence */ });
        }
    }

    /**
     * Process structured logging for the given event with the specified level and logger.
     */
    private void processLogging(ObservableEvent event, Level level, String loggerName) {
        if (!properties.observability().includeMdc()) {
            // Simple logging without MDC
            logEvent(event, level, loggerName);
            return;
        }

        // Enhanced logging with MDC context
        try {
            // Add application context to MDC
            if (applicationName != null && !applicationName.trim().isEmpty() && !"application".equals(applicationName)) {
                MDC.put("guard4j.app", applicationName);
            }

            MDC.put("guard4j.event.type", event.eventType());
            MDC.put("guard4j.event.level", level.name());
            MDC.put("guard4j.event.timestamp", event.timestamp().toString());
            MDC.put("guard4j.event.metric", String.valueOf(event.metric()));
            
            // Add context from ContextExtractor if available
            if (contextExtractor != null) {
                try {
                    Map<String, String> context = contextExtractor.extractContext();
                    for (Map.Entry<String, String> entry : context.entrySet()) {
                        MDC.put("guard4j.context." + entry.getKey(), entry.getValue());
                    }
                } catch (Exception e) {
                    log.debug("Failed to extract context: {}", e.getMessage());
                }
            }

            logEvent(event, level, loggerName);

        } finally {
            // Always clean up MDC
            MDC.remove("guard4j.app");
            MDC.remove("guard4j.event.type");
            MDC.remove("guard4j.event.level");
            MDC.remove("guard4j.event.timestamp");
            MDC.remove("guard4j.event.metric");
            
            // Clean up context fields
            if (contextExtractor != null) {
                try {
                    Map<String, String> context = contextExtractor.extractContext();
                    for (String key : context.keySet()) {
                        MDC.remove("guard4j.context." + key);
                    }
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    /**
     * Log the event at the appropriate level using the specified logger.
     */
    private void logEvent(ObservableEvent event, Level level, String loggerName) {
        Logger logger = getClassLogger(loggerName);
        String message = "Guard4j event: {} at level {}";

        switch (level) {
            case TRACE -> logger.trace(message, event.eventType(), level);
            case DEBUG -> logger.debug(message, event.eventType(), level);
            case INFO -> logger.info(message, event.eventType(), level);
            case WARN -> logger.warn(message, event.eventType(), level);
            case ERROR, FATAL -> logger.error(message, event.eventType(), level);
        }
    }

    /**
     * Get or create a logger for the specified class name.
     */
    private Logger getClassLogger(String className) {
        return loggerCache.computeIfAbsent(className, LoggerFactory::getLogger);
    }

    /**
     * Check if this is an error-level event.
     */
    private boolean isErrorEvent(Level level) {
        return level == Level.ERROR ||
               level == Level.WARN ||
               level == Level.FATAL;
    }

    /**
     * Build metric name with effective prefix.
     */
    private String buildMetricName(String suffix) {
        return effectiveMetricsPrefix + "." + suffix;
    }

    /**
     * Get or create a counter with tags.
     */
    private Counter getOrCreateCounter(String name, String eventType, String level) {
        String key = name + ":" + eventType + ":" + level;
        return counterCache.computeIfAbsent(key, k ->
            Counter.builder(name)
                .description("Guard4j event counter")
                .tag("event_type", eventType)
                .tag("level", level)
                .register(meterRegistry)
        );
    }

    /**
     * Get or create a timer with tags.
     */
    private Timer getOrCreateTimer(String name, String eventType, String level) {
        String key = name + ":" + eventType + ":" + level;
        return timerCache.computeIfAbsent(key, k ->
            Timer.builder(name)
                .description("Guard4j error timer")
                .tag("event_type", eventType)
                .tag("level", level)
                .register(meterRegistry)
        );
    }
}
