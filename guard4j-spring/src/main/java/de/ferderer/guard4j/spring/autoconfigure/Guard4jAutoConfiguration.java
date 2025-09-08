package de.ferderer.guard4j.spring.autoconfigure;

import de.ferderer.guard4j.EmitterFactory;
import de.ferderer.guard4j.observability.ContextConfig;
import de.ferderer.guard4j.observability.ContextExtractor;
import de.ferderer.guard4j.spring.observability.SpringContextExtractor;
import de.ferderer.guard4j.spring.observability.SpringObservabilityProcessor;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * Main auto-configuration for Guard4j Spring Boot integration.
 *
 * <p>This configuration automatically sets up Guard4j for Spring Boot applications
 * when the library is present on the classpath. It enables conditional configuration
 * for different Spring modules (Web, Security, Data JPA, etc.).
 *
 * <p>Configuration can be customized via application properties:
 * <pre>{@code
 * guard4j:
 *   enabled: true  # Enable/disable Guard4j (default: true)
 *   include-debug-info: false  # Include debug info in responses
 *   observability:
 *     context:
 *       enabled: true  # Enable context extraction
 *       include-trace-id: true  # Include trace ID in context
 *       include-user-id: true   # Include user ID in context
 *       include-correlation-id: true  # Include correlation ID in context
 *   web:
 *     enabled: true  # Enable web exception handling
 *     handle-spring-exceptions: true  # Map Spring exceptions
 * }</pre>
 */
@AutoConfiguration
@ConditionalOnProperty(name = "guard4j.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(Guard4jProperties.class)
@Import({
    Guard4jWebAutoConfiguration.class
})
public class Guard4jAutoConfiguration {
    
    /**
     * Create the context configuration bean from properties.
     */
    @Bean
    @ConditionalOnProperty(name = "guard4j.observability.context.enabled", havingValue = "true", matchIfMissing = true)
    public ContextConfig contextConfig(Guard4jProperties properties) {
        // Use the safe accessor to get observability configuration with defaults
        return properties.getObservabilityOrDefault().context();
    }
    
    /**
     * Create the Spring context extractor bean.
     */
    @Bean
    @ConditionalOnBean(ContextConfig.class)
    @ConditionalOnProperty(name = "guard4j.observability.context.enabled", havingValue = "true", matchIfMissing = true)
    public ContextExtractor contextExtractor(ContextConfig contextConfig) {
        return new SpringContextExtractor(contextConfig);
    }

    /**
     * Create the Spring observability processor bean and configure it in EmitterFactory.
     *
     * @param meterRegistry the Micrometer meter registry
     * @param properties the Guard4j configuration properties
     * @param environment the Spring environment for resolving properties
     * @return the configured observability processor
     */
    @Bean
    @ConditionalOnClass({MeterRegistry.class, SpringObservabilityProcessor.class})
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnProperty(name = "guard4j.observability.enabled", havingValue = "true", matchIfMissing = true)
    public SpringObservabilityProcessor springObservabilityProcessor(
            MeterRegistry meterRegistry,
            Guard4jProperties properties,
            Environment environment,
            @Autowired(required = false) ContextExtractor contextExtractor) {
        String applicationName = environment.getProperty("spring.application.name", "guard4j");
        SpringObservabilityProcessor processor = new SpringObservabilityProcessor(meterRegistry, properties, applicationName);

        // Set context extractor if available
        if (contextExtractor != null) {
            processor.setContextExtractor(contextExtractor);
        }

        // Configure the processor in EmitterFactory
        EmitterFactory.setProcessor(processor);

        return processor;
    }

}
