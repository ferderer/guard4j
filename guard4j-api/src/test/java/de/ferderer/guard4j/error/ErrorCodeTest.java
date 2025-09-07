package de.ferderer.guard4j.error;

import de.ferderer.guard4j.classification.Category;
import de.ferderer.guard4j.classification.HttpStatus;
import de.ferderer.guard4j.classification.Level;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ErrorCodeTest {

    @Test
    void shouldDeriveRetryableFromCategory() {
        // SYSTEM and EXTERNAL categories should be retryable
        TestErrorCode systemError = new TestErrorCode(Category.SYSTEM);
        TestErrorCode externalError = new TestErrorCode(Category.EXTERNAL);

        assertThat(systemError.isRetryable()).isTrue();
        assertThat(externalError.isRetryable()).isTrue();

        // BUSINESS, VALIDATION, and SECURITY categories should not be retryable
        TestErrorCode businessError = new TestErrorCode(Category.BUSINESS);
        TestErrorCode validationError = new TestErrorCode(Category.VALIDATION);
        TestErrorCode securityError = new TestErrorCode(Category.SECURITY);

        assertThat(businessError.isRetryable()).isFalse();
        assertThat(validationError.isRetryable()).isFalse();
        assertThat(securityError.isRetryable()).isFalse();
    }

    @Test
    void shouldConvertNameToClientCode() {
        TestErrorCode authAccessDenied = new TestErrorCode("AUTH_ACCESS_DENIED");
        TestErrorCode validationFailed = new TestErrorCode("VALIDATION_FAILED");
        TestErrorCode systemDatabaseError = new TestErrorCode("SYSTEM_DATABASE_ERROR");
        TestErrorCode singleWord = new TestErrorCode("ERROR");

        assertThat(authAccessDenied.code()).isEqualTo("auth-access-denied");
        assertThat(validationFailed.code()).isEqualTo("validation-failed");
        assertThat(systemDatabaseError.code()).isEqualTo("system-database-error");
        assertThat(singleWord.code()).isEqualTo("error");
    }

    // Test implementation of Error interface
    private static class TestErrorCode implements Error {
        private final String name;
        private final HttpStatus httpStatus;
        private final String message;
        private final Level severity;
        private final Category category;

        TestErrorCode(Category category) {
            this("TEST_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Test", Level.ERROR, category);
        }

        TestErrorCode(Level severity) {
            this("TEST_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Test", severity, Category.SYSTEM);
        }

        TestErrorCode(String name) {
            this(name, HttpStatus.INTERNAL_SERVER_ERROR, "Test", Level.ERROR, Category.SYSTEM);
        }

        TestErrorCode(String name, HttpStatus httpStatus, String message, Level severity, Category category) {
            this.name = name;
            this.httpStatus = httpStatus;
            this.message = message;
            this.severity = severity;
            this.category = category;
        }

        @Override public String name() { return name; }
        @Override public HttpStatus httpStatus() { return httpStatus; }
        @Override public String message() { return message; }
        @Override public Level level() { return severity; }
        @Override public Category category() { return category; }
    }
}
