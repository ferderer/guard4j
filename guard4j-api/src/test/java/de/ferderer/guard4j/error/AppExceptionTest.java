package de.ferderer.guard4j.error;

import de.ferderer.guard4j.classification.Category;
import de.ferderer.guard4j.classification.HttpStatus;
import de.ferderer.guard4j.classification.Level;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class AppExceptionTest {

    private static final TestError TEST_ERROR = new TestError();

    @Test
    void shouldCreateWithErrorCode() {
        AppException exception = new AppException(TEST_ERROR);

        assertThat(exception.errorCode()).isEqualTo(TEST_ERROR);
        assertThat(exception.getMessage()).isEqualTo("Test error occurred");
        assertThat(exception.getCause()).isNull();
        assertThat(exception.data()).isEmpty();
    }

    @Test
    void shouldCreateWithErrorCodeAndCause() {
        RuntimeException cause = new RuntimeException("Original cause");
        AppException exception = new AppException(TEST_ERROR, cause);

        assertThat(exception.errorCode()).isEqualTo(TEST_ERROR);
        assertThat(exception.getMessage()).isEqualTo("Test error occurred");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.data()).isEmpty();
    }

    @Test
    void shouldRejectNullErrorCode() {
        assertThatThrownBy(() -> new AppException(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageEndingWith("because \"errorCode\" is null");

        var cause = new RuntimeException();
        assertThatThrownBy(() -> new AppException(null, cause))
            .isInstanceOf(NullPointerException.class)
            .hasMessageEndingWith("because \"errorCode\" is null");
    }

    @Test
    void shouldAddDataFluently() {
        AppException exception = new AppException(TEST_ERROR)
            .withData("userId", "user123")
            .withData("operation", "transfer")
            .withData("amount", 100.50)
            .withData("nullValue", null);

        assertThat(exception.data())
            .hasSize(4)
            .containsEntry("userId", "user123")
            .containsEntry("operation", "transfer")
            .containsEntry("amount", 100.50)
            .containsEntry("nullValue", null);
    }

    @Test
    void shouldRejectNullDataKey() {
        AppException exception = new AppException(TEST_ERROR);

        assertThatThrownBy(() -> exception.withData(null, "value"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("data key cannot be null");
    }

    @Test
    void shouldAllowDataKeyOverwrite() {
        AppException exception = new AppException(TEST_ERROR)
            .withData("key", "value1")
            .withData("key", "value2");

        assertThat(exception.data())
            .hasSize(1)
            .containsEntry("key", "value2");
    }

    @Test
    void shouldReturnSameInstanceFromFluentMethods() {
        AppException exception = new AppException(TEST_ERROR);
        AppException result = exception.withData("key", "value");

        assertThat(result).isSameAs(exception);
    }

    @Test
    void shouldPreserveCauseInFluentMethods() {
        RuntimeException cause = new RuntimeException("Original cause");
        AppException exception = new AppException(TEST_ERROR, cause)
            .withData("key", "value");

        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldProvideReadableToString() {
        AppException exception = new AppException(TEST_ERROR)
            .withData("userId", "user123")
            .withData("operation", "transfer");

        String toString = exception.toString();

        assertThat(toString)
            .contains("AppException")
            .contains("errorCode=TEST_ERROR")
            .contains("message='Test error occurred'")
            .contains("data={userId=user123, operation=transfer}");
    }

    @Test
    void shouldHandleEmptyDataInToString() {
        AppException exception = new AppException(TEST_ERROR);

        String toString = exception.toString();

        assertThat(toString)
            .contains("AppException")
            .contains("errorCode=TEST_ERROR")
            .contains("message='Test error occurred'")
            .contains("data={}");
    }

    // Test implementation of Error interface
    private static class TestError implements Error {
        @Override
        public String name() {
            return "TEST_ERROR";
        }

        @Override
        public HttpStatus httpStatus() {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        @Override
        public String message() {
            return "Test error occurred";
        }

        @Override
        public Level level() {
            return Level.ERROR;
        }

        @Override
        public Category category() {
            return Category.SYSTEM;
        }
    }
}
