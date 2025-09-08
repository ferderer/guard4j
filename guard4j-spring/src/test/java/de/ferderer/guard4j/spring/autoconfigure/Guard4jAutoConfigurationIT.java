package de.ferderer.guard4j.spring.autoconfigure;

import de.ferderer.guard4j.spring.error.Guard4jExceptionHandler;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

/**
 * Integration tests for Guard4j auto-configuration.
 */
class Guard4jAutoConfigurationIT {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            Guard4jAutoConfiguration.class,
            WebMvcAutoConfiguration.class
        ));

    @Test
    void shouldAutoConfigureGuard4jWhenEnabled() {
        contextRunner
            .withPropertyValues("guard4j.enabled=true")
            .run(context -> {
                assertThat(context).hasSingleBean(Guard4jProperties.class);
            });
    }

    @Test
    void shouldNotAutoConfigureGuard4jWhenDisabled() {
        contextRunner
            .withPropertyValues("guard4j.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(Guard4jExceptionHandler.class);
            });
    }

    @Test
    void shouldAutoConfigureWebFeaturesWhenWebMvcPresent() {
        contextRunner
            .withPropertyValues(
                "guard4j.enabled=true",
                "guard4j.web.enabled=true"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(Guard4jExceptionHandler.class);
                assertThat(context).hasSingleBean(Guard4jProperties.class);
            });
    }

    @Test
    void shouldNotAutoConfigureWebFeaturesWhenWebDisabled() {
        contextRunner
            .withPropertyValues(
                "guard4j.enabled=true",
                "guard4j.web.enabled=false"
            )
            .run(context -> {
                assertThat(context).doesNotHaveBean(Guard4jExceptionHandler.class);
            });
    }

    @Test
    void shouldUseDefaultPropertiesWhenNotSpecified() {
        contextRunner
            .run(context -> {
                Guard4jProperties properties = context.getBean(Guard4jProperties.class);
                assertThat(properties.enabled()).isTrue();
                assertThat(properties.getWebOrDefault().enabled()).isTrue();
                assertThat(properties.getWebOrDefault().handleSpringExceptions()).isTrue();
                assertThat(properties.includeStackTrace()).isFalse();
                assertThat(properties.getObservabilityOrDefault().metricsEnabled()).isTrue();
            });
    }

    @Test
    void shouldApplyCustomProperties() {
        contextRunner
            .withPropertyValues(
                "guard4j.include-stack-trace=true",
                "guard4j.include-debug-info=true",
                "guard4j.observability.metrics-enabled=false",
                "guard4j.web.handle-spring-exceptions=false"
            )
            .run(context -> {
                Guard4jProperties properties = context.getBean(Guard4jProperties.class);
                assertThat(properties.includeStackTrace()).isTrue();
                assertThat(properties.includeDebugInfo()).isTrue();
                assertThat(properties.getObservabilityOrDefault().metricsEnabled()).isFalse();
                assertThat(properties.getWebOrDefault().handleSpringExceptions()).isFalse();
            });
    }
}
