package de.ferderer.guard4j.spring.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sample application demonstrating Guard4j Spring Boot integration.
 *
 * This application showcases:
 * - Automatic Guard4j configuration
 * - Exception mapping and handling
 * - Structured error responses
 * - Zero-configuration setup
 */
@SpringBootApplication
public class Guard4jSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(Guard4jSampleApplication.class, args);
    }
}
