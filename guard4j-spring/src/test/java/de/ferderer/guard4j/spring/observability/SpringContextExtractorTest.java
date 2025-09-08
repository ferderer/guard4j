package de.ferderer.guard4j.spring.observability;

import de.ferderer.guard4j.observability.ContextConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringContextExtractorTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private SpringContextExtractor contextExtractor;
    private ContextConfig config;

    @BeforeEach
    void setUp() {
        config = new ContextConfig(true, true, true, true, new ArrayList<>());
        
        contextExtractor = new SpringContextExtractor(config);
        
        // Clean up any existing MDC and security context
        MDC.clear();
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void extractContext_shouldReturnEmptyWhenNoContextAvailable() {
        Map<String, String> context = contextExtractor.extractContext();
        
        assertThat(context).isEmpty();
    }

    @Test
    void extractTraceId_shouldExtractFromMDC() {
        MDC.put("traceId", "trace-123");
        
        var traceId = contextExtractor.extractTraceId();
        
        assertThat(traceId).hasValue("trace-123");
    }

    @Test
    void extractTraceId_shouldExtractFromAlternativeMDCKeys() {
        MDC.put("trace_id", "trace-456");
        
        var traceId = contextExtractor.extractTraceId();
        
        assertThat(traceId).hasValue("trace-456");
    }

    @Test
    void extractTraceId_shouldExtractFromHttpHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Trace-Id", "trace-header-789");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        var traceId = contextExtractor.extractTraceId();
        
        assertThat(traceId).hasValue("trace-header-789");
    }

    @Test
    void extractUserId_shouldExtractFromSpringSecurity() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        var userId = contextExtractor.extractUserId();
        
        assertThat(userId).hasValue("testuser");
    }

    @Test
    void extractUserId_shouldIgnoreAnonymousUser() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        var userId = contextExtractor.extractUserId();
        
        assertThat(userId).isEmpty();
    }

    @Test
    void extractUserId_shouldExtractFromMDC() {
        MDC.put("userId", "mdc-user-123");
        
        var userId = contextExtractor.extractUserId();
        
        assertThat(userId).hasValue("mdc-user-123");
    }

    @Test
    void extractCorrelationId_shouldExtractFromMDC() {
        MDC.put("correlationId", "corr-123");
        
        var correlationId = contextExtractor.extractCorrelationId();
        
        assertThat(correlationId).hasValue("corr-123");
    }

    @Test
    void extractCorrelationId_shouldExtractFromHttpHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-ID", "corr-header-456");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        var correlationId = contextExtractor.extractCorrelationId();
        
        assertThat(correlationId).hasValue("corr-header-456");
    }

    @Test
    void extractContext_shouldIncludeAllConfiguredFields() {
        // Setup MDC context
        MDC.put("traceId", "trace-123");
        MDC.put("correlationId", "corr-456");
        
        // Setup Spring Security
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        Map<String, String> context = contextExtractor.extractContext();
        
        assertThat(context)
            .hasSize(3)
            .containsEntry("traceId", "trace-123")
            .containsEntry("userId", "testuser")
            .containsEntry("correlationId", "corr-456");
    }

    @Test
    void extractContext_shouldRespectConfiguration() {
        // Create a new config with different settings
        ContextConfig customConfig = new ContextConfig(true, false, true, false, new ArrayList<>());
        SpringContextExtractor customExtractor = new SpringContextExtractor(customConfig);
        
        MDC.put("traceId", "trace-123");
        MDC.put("correlationId", "corr-456");
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        Map<String, String> context = customExtractor.extractContext();
        
        assertThat(context)
            .hasSize(1)
            .containsEntry("userId", "testuser")
            .doesNotContainKey("traceId")
            .doesNotContainKey("correlationId");
    }

    @Test
    void extractContext_shouldIncludeCustomFields() {
        // Add custom field configuration
        var customFields = new ArrayList<ContextConfig.CustomField>();
        customFields.add(new ContextConfig.CustomField("tenantId", ContextConfig.FieldSource.MDC, "tenant"));
        ContextConfig customConfig = new ContextConfig(true, true, true, true, customFields);
        SpringContextExtractor customExtractor = new SpringContextExtractor(customConfig);
        
        MDC.put("tenant", "tenant-abc");
        
        Map<String, String> context = customExtractor.extractContext();
        
        assertThat(context).containsEntry("tenantId", "tenant-abc");
    }

    @Test
    void extractContext_shouldHandleCustomFieldFromHeaders() {
        // Add custom field configuration
        var customFields = new ArrayList<ContextConfig.CustomField>();
        customFields.add(new ContextConfig.CustomField("clientVersion", ContextConfig.FieldSource.HEADER, "X-Client-Version"));
        ContextConfig customConfig = new ContextConfig(true, true, true, true, customFields);
        SpringContextExtractor customExtractor = new SpringContextExtractor(customConfig);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Client-Version", "1.2.3");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        Map<String, String> context = customExtractor.extractContext();
        
        assertThat(context).containsEntry("clientVersion", "1.2.3");
    }
}
