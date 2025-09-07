package de.ferderer.guard4j.http;

import de.ferderer.guard4j.classification.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class HttpStatusTest {

    @Test
    void shouldCreateCustomStatus() {
        HttpStatus custom = HttpStatus.of(418, "I'm a teapot");

        assertThat(custom.value()).isEqualTo(418);
        assertThat(custom.reason()).isEqualTo("I'm a teapot");
    }

    @Test
    void shouldDetect1xxInformational() {
        assertThat(HttpStatus.of(100, "Continue").is1xxInformational()).isTrue();
        assertThat(HttpStatus.of(199, "Custom").is1xxInformational()).isTrue();
        assertThat(HttpStatus.of(200, "OK").is1xxInformational()).isFalse();
        assertThat(HttpStatus.of(99, "Invalid").is1xxInformational()).isFalse();
    }

    @Test
    void shouldDetect2xxSuccessful() {
        assertThat(HttpStatus.OK.is2xxSuccessful()).isTrue();
        assertThat(HttpStatus.CREATED.is2xxSuccessful()).isTrue();
        assertThat(HttpStatus.of(299, "Custom").is2xxSuccessful()).isTrue();
        assertThat(HttpStatus.BAD_REQUEST.is2xxSuccessful()).isFalse();
        assertThat(HttpStatus.of(199, "Info").is2xxSuccessful()).isFalse();
    }

    @Test
    void shouldDetect3xxRedirection() {
        assertThat(HttpStatus.of(300, "Multiple Choices").is3xxRedirection()).isTrue();
        assertThat(HttpStatus.of(399, "Custom").is3xxRedirection()).isTrue();
        assertThat(HttpStatus.OK.is3xxRedirection()).isFalse();
        assertThat(HttpStatus.BAD_REQUEST.is3xxRedirection()).isFalse();
    }

    @Test
    void shouldDetect4xxClientError() {
        assertThat(HttpStatus.BAD_REQUEST.is4xxClientError()).isTrue();
        assertThat(HttpStatus.NOT_FOUND.is4xxClientError()).isTrue();
        assertThat(HttpStatus.TOO_MANY_REQUESTS.is4xxClientError()).isTrue();
        assertThat(HttpStatus.of(499, "Custom").is4xxClientError()).isTrue();
        assertThat(HttpStatus.OK.is4xxClientError()).isFalse();
        assertThat(HttpStatus.INTERNAL_SERVER_ERROR.is4xxClientError()).isFalse();
    }

    @Test
    void shouldDetect5xxServerError() {
        assertThat(HttpStatus.INTERNAL_SERVER_ERROR.is5xxServerError()).isTrue();
        assertThat(HttpStatus.SERVICE_UNAVAILABLE.is5xxServerError()).isTrue();
        assertThat(HttpStatus.GATEWAY_TIMEOUT.is5xxServerError()).isTrue();
        assertThat(HttpStatus.of(599, "Custom").is5xxServerError()).isTrue();
        assertThat(HttpStatus.OK.is5xxServerError()).isFalse();
        assertThat(HttpStatus.BAD_REQUEST.is5xxServerError()).isFalse();
    }

    @Test
    void shouldDetectErrorStatus() {
        // 4xx errors
        assertThat(HttpStatus.BAD_REQUEST.isError()).isTrue();
        assertThat(HttpStatus.NOT_FOUND.isError()).isTrue();

        // 5xx errors
        assertThat(HttpStatus.INTERNAL_SERVER_ERROR.isError()).isTrue();
        assertThat(HttpStatus.SERVICE_UNAVAILABLE.isError()).isTrue();

        // Non-errors
        assertThat(HttpStatus.OK.isError()).isFalse();
        assertThat(HttpStatus.CREATED.isError()).isFalse();
        assertThat(HttpStatus.of(300, "Redirect").isError()).isFalse();
    }

    @Test
    void shouldCompareByValue() {
        HttpStatus status1 = HttpStatus.of(404, "Not Found");
        HttpStatus status2 = HttpStatus.of(404, "Resource Not Found");
        HttpStatus status3 = HttpStatus.of(500, "Internal Server Error");

        assertThat(status1)
            .isEqualTo(status2) // Same value, different reason
            .isNotEqualTo(status3) // Different value
            .hasSameHashCodeAs(status2);
    }

    @Test
    void shouldProvideReadableToString() {
        HttpStatus status = HttpStatus.of(404, "Not Found");

        assertThat(status).hasToString("404 Not Found");
    }

    @Test
    void shouldHaveCorrectPredefinedStatuses() {
        // Test a few key predefined statuses
        assertThat(HttpStatus.OK.value()).isEqualTo(200);
        assertThat(HttpStatus.OK.reason()).isEqualTo("OK");

        assertThat(HttpStatus.BAD_REQUEST.value()).isEqualTo(400);
        assertThat(HttpStatus.BAD_REQUEST.reason()).isEqualTo("Bad Request");

        assertThat(HttpStatus.NOT_FOUND.value()).isEqualTo(404);
        assertThat(HttpStatus.NOT_FOUND.reason()).isEqualTo("Not Found");

        assertThat(HttpStatus.INTERNAL_SERVER_ERROR.value()).isEqualTo(500);
        assertThat(HttpStatus.INTERNAL_SERVER_ERROR.reason()).isEqualTo("Internal Server Error");

        assertThat(HttpStatus.TOO_MANY_REQUESTS.value()).isEqualTo(429);
        assertThat(HttpStatus.TOO_MANY_REQUESTS.reason()).isEqualTo("Too Many Requests");
    }

    @Test
    void shouldHandleEdgeCaseStatusCodes() {
        // Edge cases for range checking
        HttpStatus boundary1xx = HttpStatus.of(100, "Continue");
        HttpStatus boundary2xx = HttpStatus.of(200, "OK");
        HttpStatus boundary3xx = HttpStatus.of(300, "Multiple Choices");
        HttpStatus boundary4xx = HttpStatus.of(400, "Bad Request");
        HttpStatus boundary5xx = HttpStatus.of(500, "Internal Server Error");

        assertThat(boundary1xx.is1xxInformational()).isTrue();
        assertThat(boundary2xx.is2xxSuccessful()).isTrue();
        assertThat(boundary3xx.is3xxRedirection()).isTrue();
        assertThat(boundary4xx.is4xxClientError()).isTrue();
        assertThat(boundary5xx.is5xxServerError()).isTrue();

        // Test boundaries
        HttpStatus upperBound1xx = HttpStatus.of(199, "Custom");
        HttpStatus upperBound2xx = HttpStatus.of(299, "Custom");

        assertThat(upperBound1xx.is1xxInformational()).isTrue();
        assertThat(upperBound1xx.is2xxSuccessful()).isFalse();
        assertThat(upperBound2xx.is2xxSuccessful()).isTrue();
        assertThat(upperBound2xx.is3xxRedirection()).isFalse();
    }
}
