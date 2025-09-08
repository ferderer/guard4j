package de.ferderer.guard4j.spring.observability;

import de.ferderer.guard4j.observability.ContextConfig;
import de.ferderer.guard4j.observability.ContextExtractor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Spring Boot implementation of ContextExtractor that extracts context from
 * various Spring-specific sources including HTTP requests, Spring Security,
 * and MDC.
 * 
 * <p>This implementation can work both in web and non-web contexts,
 * gracefully handling cases where HTTP request context is not available.
 * 
 * @since 2.1.0
 */
public class SpringContextExtractor implements ContextExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringContextExtractor.class);
    
    private final ContextConfig config;
    
    // Common MDC keys for distributed tracing
    private static final String[] TRACE_ID_KEYS = {
        "traceId", "trace_id", "X-Trace-Id", "traceid",
        "spanId", "span_id", "X-Span-Id", "spanid"
    };
    
    // Common header/MDC keys for correlation
    private static final String[] CORRELATION_ID_KEYS = {
        "correlationId", "correlation_id", "X-Correlation-ID", "X-Correlation-Id",
        "requestId", "request_id", "X-Request-ID", "X-Request-Id"
    };
    
    // Common header/MDC keys for user identification
    private static final String[] USER_ID_KEYS = {
        "userId", "user_id", "X-User-ID", "X-User-Id", "username"
    };
    
    public SpringContextExtractor(ContextConfig config) {
        this.config = config;
    }
    
    @Override
    public Map<String, String> extractContext() {
        Map<String, String> context = new HashMap<>();
        
        // Extract configured built-in context
        if (config.includeTraceId()) {
            extractTraceId().ifPresent(value -> context.put("traceId", value));
        }
        
        if (config.includeUserId()) {
            extractUserId().ifPresent(value -> context.put("userId", value));
        }
        
        if (config.includeCorrelationId()) {
            extractCorrelationId().ifPresent(value -> context.put("correlationId", value));
        }
        
        // Extract custom fields
        for (ContextConfig.CustomField field : config.customFields()) {
            extractCustomField(field).ifPresent(value -> context.put(field.name(), value));
        }
        
        return context;
    }
    
    @Override
    public Optional<String> extractTraceId() {
        // Try MDC first (most common for distributed tracing)
        for (String key : TRACE_ID_KEYS) {
            String value = MDC.get(key);
            if (value != null && !value.trim().isEmpty()) {
                return Optional.of(value);
            }
        }
        
        // Try HTTP headers if in web context
        HttpServletRequest request = getCurrentHttpRequest();
        if (request != null) {
            for (String key : TRACE_ID_KEYS) {
                String value = request.getHeader(key);
                if (value != null && !value.trim().isEmpty()) {
                    return Optional.of(value);
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<String> extractUserId() {
        // Try Spring Security first
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                return Optional.of(auth.getName());
            }
        } catch (Exception e) {
            logger.debug("Could not extract user ID from Spring Security: {}", e.getMessage());
        }
        
        // Try MDC
        for (String key : USER_ID_KEYS) {
            String value = MDC.get(key);
            if (value != null && !value.trim().isEmpty()) {
                return Optional.of(value);
            }
        }
        
        // Try HTTP headers if in web context
        HttpServletRequest request = getCurrentHttpRequest();
        if (request != null) {
            for (String key : USER_ID_KEYS) {
                String value = request.getHeader(key);
                if (value != null && !value.trim().isEmpty()) {
                    return Optional.of(value);
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<String> extractCorrelationId() {
        // Try MDC first
        for (String key : CORRELATION_ID_KEYS) {
            String value = MDC.get(key);
            if (value != null && !value.trim().isEmpty()) {
                return Optional.of(value);
            }
        }
        
        // Try HTTP headers if in web context
        HttpServletRequest request = getCurrentHttpRequest();
        if (request != null) {
            for (String key : CORRELATION_ID_KEYS) {
                String value = request.getHeader(key);
                if (value != null && !value.trim().isEmpty()) {
                    return Optional.of(value);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Extract a custom field value based on its configuration.
     */
    private Optional<String> extractCustomField(ContextConfig.CustomField field) {
        switch (field.source()) {
            case MDC:
                String mdcValue = MDC.get(field.key());
                return mdcValue != null && !mdcValue.trim().isEmpty() ? 
                    Optional.of(mdcValue) : Optional.empty();
                
            case HEADER:
                HttpServletRequest request = getCurrentHttpRequest();
                if (request != null) {
                    String headerValue = request.getHeader(field.key());
                    return headerValue != null && !headerValue.trim().isEmpty() ? 
                        Optional.of(headerValue) : Optional.empty();
                }
                return Optional.empty();
                
            case ATTRIBUTE:
                HttpServletRequest requestForAttr = getCurrentHttpRequest();
                if (requestForAttr != null) {
                    Object attrValue = requestForAttr.getAttribute(field.key());
                    return attrValue != null ? Optional.of(attrValue.toString()) : Optional.empty();
                }
                return Optional.empty();
                
            default:
                logger.warn("Unknown custom field source: {}", field.source());
                return Optional.empty();
        }
    }
    
    /**
     * Get the current HTTP request if we're in a web context.
     * Returns null if not in a web context or if request is not available.
     */
    private HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            // Not in web context or request not available
            return null;
        }
    }
}
