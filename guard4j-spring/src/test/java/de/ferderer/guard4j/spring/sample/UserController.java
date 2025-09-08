package de.ferderer.guard4j.spring.sample;

import de.ferderer.guard4j.error.AppException;
import de.ferderer.guard4j.spring.error.SpringError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Sample REST controller demonstrating Guard4j error handling.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        // Validation example
        if (id <= 0) {
            throw new AppException(SpringError.VALIDATION_INVALID_INPUT)
                .withData("field", "id")
                .withData("value", id)
                .withData("message", "User ID must be positive");
        }

        // Not found example
        if (id == 999) {
            throw new AppException(SpringError.DATA_NOT_FOUND)
                .withData("resource", "User")
                .withData("id", id);
        }

        // Business rule example
        if (id == 666) {
            throw new AppException(SpringError.BUSINESS_RULE_VIOLATION)
                .withData("rule", "restricted_user_access")
                .withData("userId", id)
                .withData("message", "Access to user " + id + " is restricted");
        }

        // Success case
        User user = new User(id, "User " + id, "user" + id + "@example.com");
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody Map<String, String> userData) {
        String name = userData.get("name");
        String email = userData.get("email");

        // Validation examples
        if (name == null || name.trim().isEmpty()) {
            throw new AppException(SpringError.VALIDATION_MISSING_REQUIRED_FIELD)
                .withData("field", "name")
                .withData("message", "Name is required");
        }

        if (email == null || !email.contains("@")) {
            throw new AppException(SpringError.VALIDATION_INVALID_FORMAT)
                .withData("field", "email")
                .withData("value", email)
                .withData("message", "Valid email is required");
        }

        // Conflict example
        if ("duplicate@example.com".equals(email)) {
            throw new AppException(SpringError.DATA_DUPLICATE_RESOURCE)
                .withData("resource", "User")
                .withData("conflictField", "email")
                .withData("value", email);
        }

        // Success case
        User user = new User(System.currentTimeMillis(), name, email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody Map<String, String> userData) {
        // Access control example
        if (id == 1) {
            throw new AppException(SpringError.AUTH_ACCESS_DENIED)
                .withData("resource", "admin user")
                .withData("operation", "modify")
                .withData("userId", id);
        }

        // Optimistic locking example
        if ("conflict".equals(userData.get("trigger"))) {
            throw new AppException(SpringError.DATA_OPTIMISTIC_LOCK_FAILURE)
                .withData("resource", "User")
                .withData("id", id)
                .withData("message", "User was modified by another process");
        }

        // External service example
        if ("service-error".equals(userData.get("trigger"))) {
            throw new AppException(SpringError.EXTERNAL_SERVICE_UNAVAILABLE)
                .withData("service", "user-validation-service")
                .withData("operation", "validateUserData")
                .withData("userId", id);
        }

        // Success case
        String name = userData.getOrDefault("name", "Updated User " + id);
        String email = userData.getOrDefault("email", "updated" + id + "@example.com");
        User user = new User(id, name, email);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Operation not allowed example
        if (id == 1) {
            throw new AppException(SpringError.BUSINESS_OPERATION_NOT_ALLOWED)
                .withData("resource", "admin user")
                .withData("operation", "delete")
                .withData("userId", id);
        }

        // Rate limiting example
        if (id == 429) {
            throw new AppException(SpringError.RATE_LIMIT_EXCEEDED)
                .withData("operation", "delete")
                .withData("limit", "10 per minute")
                .withData("message", "Too many delete operations. Please try again later");
        }

        // Success case
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to trigger Spring framework exceptions for testing automatic mapping.
     */
    @GetMapping("/test/{errorType}")
    public ResponseEntity<String> testError(@PathVariable String errorType) throws Exception {
        switch (errorType.toLowerCase()) {
            case "validation":
                // This would normally be triggered by @Valid annotation
                throw new IllegalArgumentException("Invalid argument provided");

            case "missing-param":
                // This would normally be triggered by missing @RequestParam
                throw new org.springframework.web.bind.MissingServletRequestParameterException("name", "String");

            case "access-denied":
                // This would normally be triggered by Spring Security
                throw new org.springframework.security.access.AccessDeniedException("Access denied for testing");

            case "timeout":
                throw new AppException(SpringError.EXTERNAL_SERVICE_TIMEOUT)
                    .withData("operation", "test request")
                    .withData("timeout", "30 seconds");

            default:
                return ResponseEntity.ok("No error triggered for type: " + errorType);
        }
    }

    /**
     * Simple User record for demonstration.
     */
    public record User(Long id, String name, String email) {}
}
