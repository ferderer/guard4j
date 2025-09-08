package de.ferderer.guard4j.spring.observability;

import de.ferderer.guard4j.classification.Level;
import de.ferderer.guard4j.observability.ContextConfig;
import de.ferderer.guard4j.observability.ObservableEvent;
import de.ferderer.guard4j.spring.autoconfigure.Guard4jProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SpringObservabilityProcessor with the new Emitter pattern.
 */
class SpringObservabilityProcessorTest {

    private MeterRegistry meterRegistry;
    private Guard4jProperties properties;
    private SpringObservabilityProcessor processor;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        
        // Create test configuration with explicit values
        Guard4jProperties.Observability observability = new Guard4jProperties.Observability(
            true,    // metricsEnabled
            "test-app", // metricsPrefix
            true,    // loggingEnabled  
            true,    // includeMdc
            new ContextConfig() // default context config
        );
        properties = new Guard4jProperties(
            true,       // enabled
            false,      // includeDebugInfo
            false,      // includeStackTrace
            Map.of(),   // levelOverrides
            observability,
            new Guard4jProperties.Web(true, true, -100) // default web config
        );
        
        processor = new SpringObservabilityProcessor(meterRegistry, properties, "test-app");
    }

    @Test
    void shouldProcessEventWithMetricsAndLogging() {
        // Given
        ObservableEvent event = createTestEvent("test.event", 1);

        // When
        processor.processWithLevel(event, Level.ERROR, "com.example.TestService");

        // Then
        Counter counter = meterRegistry.find("test-app.events").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        Timer timer = meterRegistry.find("test-app.errors").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    void shouldSkipMetricsWhenDisabled() {
        // Given
        Guard4jProperties.Observability disabledObservability = new Guard4jProperties.Observability(
            false,  // metricsEnabled = false
            properties.observability().metricsPrefix(),
            properties.observability().loggingEnabled(),
            properties.observability().includeMdc(),
            properties.observability().context()
        );
        Guard4jProperties disabledProperties = new Guard4jProperties(
            properties.enabled(),
            properties.includeDebugInfo(),
            properties.includeStackTrace(),
            properties.levelOverrides(),
            disabledObservability,
            properties.web()
        );
        processor = new SpringObservabilityProcessor(meterRegistry, disabledProperties, "test-app");
        
        ObservableEvent event = createTestEvent("test.event", 1);

        // When
        processor.processWithLevel(event, Level.ERROR, "com.example.TestService");

        // Then
        Counter counter = meterRegistry.find("test-app.events").counter();
        assertThat(counter).isNull();
    }

    @Test
    void shouldUseEventMetricValue() {
        // Given
        ObservableEvent event = createTestEvent("test.event", 5);

        // When
        processor.processWithLevel(event, Level.INFO, "com.example.TestService");

        // Then
        Counter counter = meterRegistry.find("test-app.events").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(5.0); // Should use event.metric() value
    }

    @Test
    void shouldHandleMdcContext() {
        // Given
        ObservableEvent event = createTestEvent("test.event", 1);
        MDC.clear(); // Ensure clean state

        // When
        processor.processWithLevel(event, Level.INFO, "com.example.TestService");

        // Then - MDC should be cleaned up after processing
        assertThat(MDC.get("guard4j.event.type")).isNull();
        assertThat(MDC.get("guard4j.event.level")).isNull();
        assertThat(MDC.get("guard4j.event.timestamp")).isNull();
        assertThat(MDC.get("guard4j.event.metric")).isNull();
    }

    @Test
    void shouldProcessInfoEventWithoutTimer() {
        // Given
        ObservableEvent event = createTestEvent("test.event", 1);

        // When
        processor.processWithLevel(event, Level.INFO, "com.example.TestService");

        // Then
        Counter counter = meterRegistry.find("test-app.events").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        Timer timer = meterRegistry.find("test-app.errors").timer();
        assertThat(timer).isNull(); // Timer only for error events
    }

    @Test
    void shouldProcessWarnEventWithTimer() {
        // Given
        ObservableEvent event = createTestEvent("test.event", 1);

        // When
        processor.processWithLevel(event, Level.WARN, "com.example.TestService");

        // Then
        Counter counter = meterRegistry.find("test-app.events").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        Timer timer = meterRegistry.find("test-app.errors").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    void shouldProcessFatalEventWithTimer() {
        // Given
        ObservableEvent event = createTestEvent("test.event", 1);

        // When
        processor.processWithLevel(event, Level.FATAL, "com.example.TestService");

        // Then
        Counter counter = meterRegistry.find("test-app.events").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        Timer timer = meterRegistry.find("test-app.errors").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    void shouldUseCustomMetricsPrefix() {
        // Given
        Guard4jProperties.Observability customObservability = new Guard4jProperties.Observability(
            properties.observability().metricsEnabled(),
            "custom",  // custom metricsPrefix
            properties.observability().loggingEnabled(),
            properties.observability().includeMdc(),
            properties.observability().context()
        );
        Guard4jProperties customProperties = new Guard4jProperties(
            properties.enabled(),
            properties.includeDebugInfo(),
            properties.includeStackTrace(),
            properties.levelOverrides(),
            customObservability,
            properties.web()
        );
        processor = new SpringObservabilityProcessor(meterRegistry, customProperties, "test-app");
        ObservableEvent event = createTestEvent("test.event", 1);

        // When
        processor.processWithLevel(event, Level.ERROR, "com.example.TestService");

        // Then
        Counter counter = meterRegistry.find("custom.events").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);

        Timer timer = meterRegistry.find("custom.errors").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    void shouldTagMetricsWithEventTypeAndLevel() {
        // Given
        ObservableEvent event = createTestEvent("business.event", 1);

        // When
        processor.processWithLevel(event, Level.WARN, "com.example.BusinessService");

        // Then
        Counter counter = meterRegistry.find("test-app.events")
            .tag("event_type", "business.event")
            .tag("level", "warn")
            .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldDefaultToApplicationNameWhenNoPrefixConfigured() {
        // Given - no metrics prefix configured, should use application name
        Guard4jProperties.Observability observabilityWithMetrics = new Guard4jProperties.Observability(
            true,  // metricsEnabled = true
            null,  // no metricsPrefix
            true,  // loggingEnabled
            true,  // includeMdc
            new ContextConfig()  // default context
        );
        Guard4jProperties propsWithoutPrefix = new Guard4jProperties(
            true,  // enabled
            false, // includeDebugInfo
            false, // includeStackTrace
            Map.of(), // levelOverrides
            observabilityWithMetrics,
            new Guard4jProperties.Web(true, true, -100) // default web
        );
        SpringObservabilityProcessor processorWithAppName =
            new SpringObservabilityProcessor(meterRegistry, propsWithoutPrefix, "my-service");

        ObservableEvent event = createTestEvent("test.event", 1);

        // When
        processorWithAppName.processWithLevel(event, Level.ERROR, "com.example.TestService");

        // Then
        Counter counter = meterRegistry.find("my-service.events").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldFallbackToGuard4jWhenNoApplicationName() {
        // Given - no metrics prefix and no meaningful application name
        Guard4jProperties.Observability observabilityWithMetrics = new Guard4jProperties.Observability(
            true,  // metricsEnabled = true
            null,  // no metricsPrefix
            true,  // loggingEnabled
            true,  // includeMdc
            new ContextConfig()  // default context
        );
        Guard4jProperties propsWithoutPrefix = new Guard4jProperties(
            true,  // enabled
            false, // includeDebugInfo
            false, // includeStackTrace
            Map.of(), // levelOverrides
            observabilityWithMetrics,
            new Guard4jProperties.Web(true, true, -100) // default web
        );
        SpringObservabilityProcessor processorWithoutAppName =
            new SpringObservabilityProcessor(meterRegistry, propsWithoutPrefix, "application");

        ObservableEvent event = createTestEvent("test.event", 1);

        // When
        processorWithoutAppName.processWithLevel(event, Level.ERROR, "com.example.TestService");

        // Then
        Counter counter = meterRegistry.find("guard4j.events").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldTestBackwardCompatibilityWithOldProcessMethod() {
        // Given
        ObservableEvent event = createTestEvent("test.event", 1);

        // When - using the old process method for backward compatibility
        processor.process(event);

        // Then - should use INFO level and event-type based logger name
        Counter counter = meterRegistry.find("test-app.events")
            .tag("event_type", "test.event")
            .tag("level", "info")
            .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    private ObservableEvent createTestEvent(String eventType, int metricValue) {
        return new ObservableEvent() {
            @Override
            public String eventType() {
                return eventType;
            }

            @Override
            public int metric() {
                return metricValue;
            }

            @Override
            public Instant timestamp() {
                return Instant.now();
            }
        };
    }
}
