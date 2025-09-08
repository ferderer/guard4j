# Guard4j Enhanced Observability Roadmap

## Overview
This roadmap implements enhanced observability features building on the completed Emitter pattern infrastructure. The goal is to provide rich context for observability systems while maintaining Guard4j's focused scope.

## Completed ✅
- **Emitter Pattern Infrastructure**: `Guard4j.getEmitter(Class)` with level-based methods
- **Simplified ObservableEvent**: Auto-derived event types and metric values
- **Basic Framework Integration**: Spring Boot starter with observability processor

## Phase 1: Enhanced Context Extraction

### 1.1 Context Configuration Model
**Files to create/modify:**
- `guard4j-api/src/main/java/de/ferderer/guard4j/observability/ContextConfig.java`
- `guard4j-spring/src/main/java/de/ferderer/guard4j/spring/autoconfigure/Guard4jProperties.java`

**Requirements:**
- Add `Context` configuration class with fields:
  - `boolean includeTraceId = true`
  - `boolean includeUserId = true` 
  - `boolean includeCorrelationId = true`
  - `List<CustomField> customFields = new ArrayList<>()`
- Add `CustomField` record with:
  - `String header` (optional)
  - `String mdcKey` (optional)
  - `String securityAttribute` (optional)
  - `String targetKey` (required)

### 1.2 Context Extractor Interface
**Files to create:**
- `guard4j-api/src/main/java/de/ferderer/guard4j/observability/ContextExtractor.java`

**Requirements:**
```java
public interface ContextExtractor {
    Map<String, String> extractContext(HttpServletRequest request);
    Optional<String> extractTraceId();
    Optional<String> extractUserId();
    Optional<String> extractCorrelationId();
}
```

### 1.3 Spring Context Extractor Implementation
**Files to create:**
- `guard4j-spring/src/main/java/de/ferderer/guard4j/spring/observability/SpringContextExtractor.java`

**Requirements:**
- Extract trace ID from Micrometer/Sleuth MDC keys: `traceId`, `X-Trace-Id`
- Extract user ID from Spring Security: `SecurityContextHolder.getContext().getAuthentication()`
- Extract correlation ID from headers: `X-Correlation-ID`, `X-Request-ID`
- Support custom field extraction from headers and MDC
- Handle cases where Spring Security is not available (optional dependency)

## Phase 2: ErrorViewBuilder Pattern

### 2.1 ErrorViewBuilder Interface
**Files to create:**
- `guard4j-api/src/main/java/de/ferderer/guard4j/ErrorViewBuilder.java`

**Requirements:**
```java
public interface ErrorViewBuilder {
    Object buildErrorView(ErrorResponse errorResponse, 
                         HttpServletRequest request, 
                         HttpServletResponse response);
}
```

### 2.2 Default REST Implementation
**Files to create:**
- `guard4j-spring/src/main/java/de/ferderer/guard4j/spring/error/RestErrorViewBuilder.java`

**Requirements:**
- Default implementation that returns `ResponseEntity<ErrorResponse>`
- Auto-configured when no user-provided bean exists
- Support for custom HTTP headers if needed

### 2.3 Update Exception Handlers
**Files to modify:**
- `guard4j-spring/src/main/java/de/ferderer/guard4j/spring/error/Guard4jExceptionHandler.java`

**Requirements:**
- Replace direct `ResponseEntity` return with `ErrorViewBuilder.buildErrorView()`
- Maintain same `ErrorResponse` construction logic
- Inject `ErrorViewBuilder` dependency

## Phase 3: Enhanced Logging Configuration

### 3.1 Logging Format Enum
**Files to create:**
- `guard4j-api/src/main/java/de/ferderer/guard4j/observability/LoggingFormat.java`

**Requirements:**
```java
public enum LoggingFormat {
    CONSOLE,  // Human-readable: "Guard4j event: order-cancelled at level ERROR"
    JSON,     // Structured for log aggregation
    MINIMAL   // Just event type: "order-cancelled"
}
```

### 3.2 Enhanced SpringObservabilityProcessor
**Files to modify:**
- `guard4j-spring/src/main/java/de/ferderer/guard4j/spring/observability/SpringObservabilityProcessor.java`

**Requirements:**
- Add `LoggingFormat` support in constructor
- Implement JSON logging mode:
  - Enhanced MDC with context fields
  - Structured log messages with key-value pairs
  - Rich error context (error code, HTTP status, request path)
- Implement minimal logging mode
- Integrate with `ContextExtractor` for rich context

### 3.3 Update Configuration Properties
**Files to modify:**
- `guard4j-spring/src/main/java/de/ferderer/guard4j/spring/autoconfigure/Guard4jProperties.java`

**Requirements:**
- Add `LoggingFormat logging = LoggingFormat.CONSOLE` to Observability class
- Add `Context context = new Context()` to Observability class
- Ensure backward compatibility

## Phase 4: Unified Error Page Support

### 4.1 Error Controller for Standard Error Pages
**Files to create:**
- `guard4j-spring/src/main/java/de/ferderer/guard4j/spring/error/Guard4jErrorController.java`

**Requirements:**
- Implement Spring Boot's `ErrorController` interface
- Handle `/error` mapping for servlet container errors
- Extract error details from servlet request attributes
- Convert to `ErrorResponse` format
- Use same `ErrorViewBuilder` as exception handler
- Support for 404, 500, and other standard HTTP errors

### 4.2 Error Page Registration
**Files to create:**
- `guard4j-spring/src/main/java/de/ferderer/guard4j/spring/autoconfigure/Guard4jErrorPageAutoConfiguration.java`

**Requirements:**
- Auto-configure `ErrorPageRegistrar` bean
- Register `/error` as default error page
- Conditional on web application type
- Allow disabling via configuration

### 4.3 Servlet Error Mapping
**Files to create:**
- `guard4j-spring/src/main/java/de/ferderer/guard4j/spring/error/ServletErrorMapper.java`

**Requirements:**
- Map servlet error attributes to Guard4j `Error` codes
- Handle status codes: 400, 401, 403, 404, 405, 500, 503
- Extract exception information when available
- Create appropriate `ErrorResponse` objects

## Phase 5: Enhanced Metrics with Context

### 5.1 Context-Aware Metrics
**Files to modify:**
- `guard4j-spring/src/main/java/de/ferderer/guard4j/spring/observability/SpringObservabilityProcessor.java`

**Requirements:**
- Add context fields as metric tags when available
- Support configurable tag inclusion (avoid high cardinality)
- Add tags: `user_type`, `tenant_id`, `trace_id` (configurable)
- Maintain performance with tag caching

### 5.2 Metric Configuration
**Files to modify:**
- `guard4j-spring/src/main/java/de/ferderer/guard4j/spring/autoconfigure/Guard4jProperties.java`

**Requirements:**
- Add metrics configuration section:
  - `boolean includeContextTags = false` (default false for cardinality)
  - `Set<String> allowedContextTags = Set.of("user_type", "tenant_id")`
  - `int maxTagValues = 1000` (cardinality protection)

## Phase 6: Documentation and Examples

### 6.1 Enhanced Documentation
**Files to create/update:**
- `OBSERVABILITY.md` - Comprehensive observability guide
- `ERROR_VIEW_BUILDER.md` - Custom error rendering guide  
- `CONTEXT_EXTRACTION.md` - Rich context configuration guide

**Requirements:**
- Document all new configuration options
- Provide examples for VictoriaMetrics/VictoriaLogs integration
- Show SSR error page examples
- Demonstrate custom context extraction

### 6.2 Example Implementations
**Files to create:**
- `examples/spring-boot-ssr/` - Server-side rendering example
- `examples/spring-boot-hybrid/` - API + SSR hybrid example
- `examples/observability-stack/` - VictoriaMetrics + VictoriaLogs setup

**Requirements:**
- Working examples for each major use case
- Docker Compose setups for observability stack
- Sample error page templates
- Custom ErrorViewBuilder implementations

## Phase 7: Testing and Integration

### 7.1 Integration Tests
**Files to create:**
- Test suites for each new component
- End-to-end observability testing
- ErrorViewBuilder integration tests
- Context extraction validation

### 7.2 Performance Testing
**Requirements:**
- Benchmark context extraction overhead
- Validate metric cardinality protection
- Test JSON logging performance impact

## Configuration Examples

### Development Configuration
```yaml
guard4j:
  observability:
    logging: console
    context:
      include-trace-id: false
      include-user-id: false
```

### Production Configuration  
```yaml
guard4j:
  observability:
    logging: json
    context:
      include-trace-id: true
      include-user-id: true
      include-correlation-id: true
      custom-fields:
        - header: "X-Tenant-ID"
          target-key: "tenantId"
    metrics:
      include-context-tags: true
      allowed-context-tags: ["tenantId", "userType"]
```

### SSR Application Configuration
```yaml
guard4j:
  web:
    error-pages:
      enabled: true

# Custom ErrorViewBuilder bean for SSR
@Bean
public ErrorViewBuilder errorViewBuilder() {
    return (errorResponse, request, response) -> {
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(errorResponse.status()).body(errorResponse);
        } else {
            ModelAndView mav = new ModelAndView("error/application-error");
            mav.addObject("error", errorResponse);
            return mav;
        }
    };
}
```

## Success Criteria

1. **Rich Context**: All events include trace ID, user ID, and custom fields when configured
2. **Flexible Error Rendering**: Same error data can render as JSON API responses or HTML pages
3. **Enhanced Observability**: JSON logs with full context for log aggregation systems
4. **Zero-Config Defaults**: Works out of the box for REST APIs, configurable for advanced use cases
5. **Performance**: Context extraction adds <1ms overhead, metric cardinality protection prevents explosions

This roadmap maintains Guard4j's focused scope while providing the rich observability context needed for production monitoring systems.