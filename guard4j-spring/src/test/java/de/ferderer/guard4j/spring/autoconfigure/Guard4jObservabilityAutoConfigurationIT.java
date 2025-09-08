package de.ferderer.guard4j.spring.autoconfigure;

import de.ferderer.guard4j.spring.observability.SpringObservabilityProcessor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Integration tests for Guard4j observability auto-configuration.
 */
class Guard4jObservabilityAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            Guard4jAutoConfiguration.class
        ));

    @Test
    void shouldCreateObservabilityProcessorWhenMeterRegistryPresent() {
        contextRunner
            .withUserConfiguration(MeterRegistryConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(SpringObservabilityProcessor.class);
                assertThat(context).hasSingleBean(MeterRegistry.class);
                assertThat(context).hasSingleBean(Guard4jProperties.class);
            });
    }

    @Test
    void shouldNotCreateObservabilityProcessorWhenMeterRegistryAbsent() {
        contextRunner
            .run(context -> {
                assertThat(context).doesNotHaveBean(SpringObservabilityProcessor.class);
                assertThat(context).doesNotHaveBean(MeterRegistry.class);
            });
    }

    @Test
    void shouldNotCreateObservabilityProcessorWhenDisabled() {
        contextRunner
            .withUserConfiguration(MeterRegistryConfiguration.class)
            .withPropertyValues("guard4j.observability.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(SpringObservabilityProcessor.class);
                assertThat(context).hasSingleBean(MeterRegistry.class);
            });
    }

    @Test
    void shouldRespectObservabilityConfiguration() {
        contextRunner
            .withUserConfiguration(MeterRegistryConfiguration.class)
            .withPropertyValues(
                "guard4j.observability.metrics-enabled=false",
                "guard4j.observability.logging-enabled=true",
                "guard4j.observability.include-mdc=false",
                "guard4j.observability.metrics-prefix=custom"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(SpringObservabilityProcessor.class);
                Guard4jProperties properties = context.getBean(Guard4jProperties.class);
                assertThat(properties.getObservabilityOrDefault().metricsEnabled()).isFalse();
                assertThat(properties.getObservabilityOrDefault().loggingEnabled()).isTrue();
                assertThat(properties.getObservabilityOrDefault().includeMdc()).isFalse();
                assertThat(properties.getObservabilityOrDefault().metricsPrefix()).isEqualTo("custom");
            });
    }

    @Test
    void shouldUseDefaultObservabilityConfiguration() {
        contextRunner
            .withUserConfiguration(MeterRegistryConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(SpringObservabilityProcessor.class);
                Guard4jProperties properties = context.getBean(Guard4jProperties.class);
                assertThat(properties.getObservabilityOrDefault().metricsEnabled()).isTrue();
                assertThat(properties.getObservabilityOrDefault().loggingEnabled()).isTrue();
                assertThat(properties.getObservabilityOrDefault().includeMdc()).isTrue();
                // metrics prefix is null by default, resolved at runtime to application name
                assertThat(properties.getObservabilityOrDefault().metricsPrefix()).isNull();
            });
    }

    @Test
    void shouldUseApplicationNameAsDefaultMetricsPrefix() {
        contextRunner
            .withUserConfiguration(MeterRegistryConfiguration.class)
            .withPropertyValues("spring.application.name=my-test-app")
            .run(context -> {
                assertThat(context).hasSingleBean(SpringObservabilityProcessor.class);
                Guard4jProperties properties = context.getBean(Guard4jProperties.class);
                // Properties should still be null, but the processor should use application name
                assertThat(properties.getObservabilityOrDefault().metricsPrefix()).isNull();

                // We can't easily test the effective prefix without making the processor expose it,
                // but the unit tests cover this behavior
            });
    }

    @Configuration
    static class MeterRegistryConfiguration {

        @Bean
        public MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }
}
