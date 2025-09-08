package de.ferderer.guard4j.observability;

import java.util.List;

/**
 * Configuration for context extraction in observability events.
 * 
 * <p>Controls which context information should be extracted and included
 * in observability data (logs, metrics, traces). Context extraction helps
 * correlate events across service boundaries and provides rich debugging
 * information.
 * 
 * @param enabled whether context extraction is enabled
 * @param includeTraceId whether to include trace ID in context
 * @param includeUserId whether to include user ID in context
 * @param includeCorrelationId whether to include correlation ID in context
 * @param customFields list of custom fields to extract
 * @since 2.1.0
 */
public record ContextConfig(
    boolean enabled,
    boolean includeTraceId,
    boolean includeUserId,
    boolean includeCorrelationId,
    List<CustomField> customFields
) {
    
    /**
     * Create a ContextConfig with default values.
     */
    public ContextConfig() {
        this(true, true, true, true, List.of());
    }
    
    /**
     * Source types for custom field extraction.
     */
    public enum FieldSource {
        MDC,
        HEADER,
        ATTRIBUTE
    }
    
    /**
     * Configuration for extracting custom context fields.
     * 
     * <p>Custom fields can be extracted from HTTP headers, MDC (Mapped Diagnostic Context),
     * or request attributes. The source type and key must be specified.
     * 
     * @param name the key to use in the extracted context map (required)
     * @param source the source type to extract from (required)
     * @param key the key/name to look for in the source (required)
     */
    public record CustomField(
        String name,
        FieldSource source,
        String key
    ) {
        public CustomField {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("name is required");
            }
            if (source == null) {
                throw new IllegalArgumentException("source is required");
            }
            if (key == null || key.trim().isEmpty()) {
                throw new IllegalArgumentException("key is required");
            }
        }
    }
}
