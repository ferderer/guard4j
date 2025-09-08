package de.ferderer.guard4j.spring.error;

import de.ferderer.guard4j.error.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Guard4j web exception handling.
 */
@SpringBootTest(
    classes = {Guard4jExceptionHandlerIT.TestApplication.class, Guard4jExceptionHandlerIT.TestController.class}
)
@TestPropertySource(properties = {
        "guard4j.enabled=true",
        "guard4j.web.enabled=true",
        "guard4j.include-stack-trace=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class Guard4jExceptionHandlerIT {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Test
    void shouldHandleAppExceptionWithStructuredResponse() throws Exception {
        mockMvc.perform(get("/test/app-exception"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("DATA_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Requested resource not found"))
                .andExpect(jsonPath("$.path").value("/test/app-exception"))
                .andExpect(jsonPath("$.data.resource").value("test-item"))
                .andExpect(jsonPath("$.data.id").value("123"));
    }

    @Test
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/test/generic-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("Internal server error occurred"));
    }

    @SpringBootApplication
    static class TestApplication {
        // Guard4j auto-configuration will be applied automatically
    }

    @RestController
    static class TestController {

        @GetMapping("/test/app-exception")
        public String appException() {
            throw new AppException(SpringError.DATA_NOT_FOUND)
                    .withData("resource", "test-item")
                    .withData("id", "123");
        }

        @GetMapping("/test/generic-exception")
        public String genericException() {
            throw new RuntimeException("Something went wrong");
        }
    }
}
