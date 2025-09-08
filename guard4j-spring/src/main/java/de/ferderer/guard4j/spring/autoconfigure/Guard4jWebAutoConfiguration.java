package de.ferderer.guard4j.spring.autoconfigure;

import de.ferderer.guard4j.spring.error.Guard4jExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Auto-configuration for Guard4j web exception handling.
 *
 * <p>This configuration is activated when:
 * <ul>
 *   <li>Spring Web MVC is on the classpath</li>
 *   <li>Application is a servlet web application</li>
 *   <li>Web exception handling is enabled (default: true)</li>
 * </ul>
 *
 * <p>Provides global exception handling via {@code @ControllerAdvice}
 * that converts Guard4j {@code AppException} and Spring framework exceptions
 * into structured JSON error responses.
 */
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@ConditionalOnClass(DispatcherServlet.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "guard4j.web.enabled", havingValue = "true", matchIfMissing = true)
public class Guard4jWebAutoConfiguration {

    /**
     * Global exception handler that converts exceptions to structured error responses.
     *
     * @param properties Guard4j configuration properties
     * @return configured exception handler
     */
    @Bean
    public Guard4jExceptionHandler guard4jExceptionHandler(Guard4jProperties properties) {
        return new Guard4jExceptionHandler(properties);
    }
}
