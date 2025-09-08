package de.ferderer.guard4j.spring.error;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SpringError exception mapping.
 */
class SpringErrorIT {

    @Test
    void shouldMapKnownExceptionDirectly() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        SpringError error = SpringError.fromException(exception);

        assertThat(error).isEqualTo(SpringError.AUTH_ACCESS_DENIED);
        assertThat(error.message()).isEqualTo("Access denied to requested resource");
        assertThat(error.httpStatus().value()).isEqualTo(403);
    }

    @Test
    void shouldMapValidationExceptionDirectly() {
        MissingServletRequestParameterException exception =
            new MissingServletRequestParameterException("param", "String");

        SpringError error = SpringError.fromException(exception);

        assertThat(error).isEqualTo(SpringError.VALIDATION_MISSING_PARAMETER);
        assertThat(error.message()).isEqualTo("Required request parameter is missing");
        assertThat(error.httpStatus().value()).isEqualTo(400);
    }

    @Test
    void shouldReturnUnknownErrorForUnmappedException() {
        RuntimeException exception = new RuntimeException("Unknown error");

        SpringError error = SpringError.fromException(exception);

        assertThat(error).isEqualTo(SpringError.UNKNOWN_ERROR);
        assertThat(error.message()).isEqualTo("Unknown error occurred");
        assertThat(error.httpStatus().value()).isEqualTo(500);
    }

    @Test
    void shouldReturnInternalServerErrorWithFallback() {
        RuntimeException exception = new RuntimeException("Critical error");

        SpringError error = SpringError.fromExceptionWithFallback(exception);

        assertThat(error).isEqualTo(SpringError.INTERNAL_SERVER_ERROR);
        assertThat(error.message()).isEqualTo("Internal server error occurred");
        assertThat(error.httpStatus().value()).isEqualTo(500);
    }

    @Test
    void shouldKeepMappedErrorWithFallback() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        SpringError error = SpringError.fromExceptionWithFallback(exception);

        assertThat(error).isEqualTo(SpringError.AUTH_ACCESS_DENIED);
        assertThat(error.message()).isEqualTo("Access denied to requested resource");
    }

    @Test
    void shouldHandleNullException() {
        SpringError error = SpringError.fromException(null);

        assertThat(error).isEqualTo(SpringError.UNKNOWN_ERROR);

        SpringError fallbackError = SpringError.fromExceptionWithFallback(null);
        assertThat(fallbackError).isEqualTo(SpringError.INTERNAL_SERVER_ERROR);
    }
}
