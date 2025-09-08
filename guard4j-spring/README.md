# Guard4j Spring Boot Starter

A Spring Boot starter that provides seamless integration of Guard4j error handling framework with Spring Boot applications.

## Features

- 🚀 **Zero Configuration**: Works out of the box with sensible defaults
- 🔧 **Highly Configurable**: Extensive configuration options via application properties
- 🌐 **Web Integration**: Automatic global exception handling for web applications
- 📊 **Observability Ready**: Built-in Micrometer metrics, enhanced logging, and Spring Boot Actuator integration
- 🔄 **Exception Mapping**: Automatic mapping of Spring exceptions to Guard4j errors
- 🎯 **Conditional Activation**: Features activate based on classpath presence
- 🧪 **Test Friendly**: Easy to disable for testing scenarios
- 💼 **Business KPI Support**: Event-based metrics for business intelligence and real-time dashboards

## Quick Start

### 1. Add Dependency

```xml
<dependency>
    <groupId>de.ferderer</groupId>
    <artifactId>guard4j-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Use in Your Application

```java
@RestController
public class UserController {
    private static final Emitter events = Guard4j.getEmitter(UserController.class);

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        events.info(new UserRequestedEvent(id));

        if (id <= 0) {
            events.warn(new InvalidUserIdEvent(id));
            throw new AppException(SpringError.SPRING_VALIDATION_FAILED,
                "User ID must be positive");
        }

        User user = userService.findById(id);
        if (user == null) {
            events.warn(new UserNotFoundEvent(id));
            throw new AppException(SpringError.SPRING_DATA_NOT_FOUND,
                "User not found with ID: " + id);
        }

        events.info(new UserRetrievedEvent(user.getId(), user.getStatus()));
        return user;
    }
}

// Simple event definitions
public record UserRequestedEvent(Long userId) implements ObservableEvent {}
public record UserRetrievedEvent(Long userId, String status) implements ObservableEvent {}
public record UserNotFoundEvent(Long userId) implements ObservableEvent {}
```

**Automatic Output:**
- **Metrics**: `guard4j_user_retrieved_total{status="active"}`, `guard4j_user_not_found_total`
- **Logs**: Structured JSON with correlation IDs and business context

### 3. Automatic Error Responses

The starter automatically handles exceptions and returns structured JSON responses:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "SPRING_DATA_NOT_FOUND",
  "message": "User not found with ID: 123",
  "path": "/users/123"
}
```

## Configuration

### Basic Configuration

```properties
# Enable/disable Guard4j (default: true)
guard4j.enabled=true

# Enable exception mapping (default: true)
guard4j.exception-mapping.enabled=true
```

### Web Configuration

```properties
# Enable web features (default: true)
guard4j.web.enabled=true

# Enable global exception handler (default: true)
guard4j.web.global-exception-handler.enabled=true

# Exception handler order (default: 1000)
guard4j.web.global-exception-handler.order=1000

# Include stack traces - disable in production (default: false)
guard4j.web.include-stack-trace=false

# Include request path (default: true)
guard4j.web.include-path=true
```

### Observability Configuration

```properties
# Enable observability features (default: true)
guard4j.observability.enabled=true

# Enable metrics collection (default: true when observability enabled)
guard4j.observability.metrics-enabled=true

# Metrics name prefix (defaults to spring.application.name, then "guard4j")
guard4j.observability.metrics-prefix=guard4j

# Enable enhanced logging (default: true when observability enabled)
guard4j.observability.logging-enabled=true

# Include MDC context in logs (default: true)
guard4j.observability.include-mdc=true
```

**Logging Context**: Guard4j uses SLF4J MDC (Mapped Diagnostic Context) to enrich log entries with contextual information:
- `guard4j.app` - Your application name (from `spring.application.name`)
- `guard4j.event.type` - The event type (e.g., "VALIDATION_FAILED")
- `guard4j.event.level` - The event level (ERROR, WARN, INFO, etc.)
- `guard4j.event.timestamp` - When the event occurred
- `guard4j.event.has_metrics` - Whether metrics are enabled for this event

The logger name is `de.ferderer.guard4j.spring.observability.SpringObservabilityProcessor`, allowing standard configuration in your `logback-spring.xml` while providing rich context for filtering and routing logs in multi-service environments.

> 📖 **Detailed Observability Guide**: See [OBSERVABILITY.md](OBSERVABILITY.md) for comprehensive documentation on metrics, monitoring, and Grafana integration.
```

## Spring Error Codes

The starter provides comprehensive error codes for common Spring scenarios:

### Authentication & Security
- `SPRING_AUTHENTICATION_FAILED` - Authentication failures
- `SPRING_ACCESS_DENIED` - Authorization failures
- `SPRING_INVALID_TOKEN` - JWT/token validation failures
- `SPRING_SESSION_EXPIRED` - Session timeouts

### Validation
- `SPRING_VALIDATION_FAILED` - Bean validation failures
- `SPRING_INVALID_REQUEST_BODY` - Malformed request bodies
- `SPRING_MISSING_PARAMETER` - Missing required parameters
- `SPRING_TYPE_MISMATCH` - Parameter type conversion failures

### Data & Persistence
- `SPRING_DATA_NOT_FOUND` - Entity not found
- `SPRING_CONSTRAINT_VIOLATION` - Database constraint violations
- `SPRING_DATA_INTEGRITY_VIOLATION` - Data integrity issues
- `SPRING_OPTIMISTIC_LOCK_FAILURE` - Concurrent modification conflicts

### Business Logic
- `SPRING_BUSINESS_RULE_VIOLATION` - Custom business rule failures
- `SPRING_RESOURCE_CONFLICT` - Resource conflicts
- `SPRING_OPERATION_NOT_ALLOWED` - Forbidden operations

### External Services
- `SPRING_EXTERNAL_SERVICE_ERROR` - Third-party service failures
- `SPRING_TIMEOUT_ERROR` - Request timeouts
- `SPRING_RATE_LIMIT_EXCEEDED` - Rate limiting

## Conditional Features

Features automatically activate based on classpath presence:

- **Web Features**: Activated when `spring-boot-starter-web` is present
- **Security Features**: Activated when `spring-boot-starter-security` is present
- **Data Features**: Activated when `spring-boot-starter-data-jpa` is present
- **Validation Features**: Activated when `spring-boot-starter-validation` is present

## Exception Mapping

The starter automatically maps Spring exceptions to Guard4j errors:

```java
// Spring Security exceptions
AccessDeniedException → SPRING_ACCESS_DENIED
AuthenticationException → SPRING_AUTHENTICATION_FAILED

// Validation exceptions
MethodArgumentNotValidException → SPRING_VALIDATION_FAILED
ConstraintViolationException → SPRING_VALIDATION_FAILED

// Data exceptions
EntityNotFoundException → SPRING_DATA_NOT_FOUND
DataIntegrityViolationException → SPRING_CONSTRAINT_VIOLATION

// Web exceptions
HttpMessageNotReadableException → SPRING_INVALID_REQUEST_BODY
MissingServletRequestParameterException → SPRING_MISSING_PARAMETER
```

## Custom Exception Mapping

You can extend exception mapping by implementing your own exception handlers:

```java
@ControllerAdvice
@Order(500) // Higher precedence than Guard4j handler
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        AppException appEx = new AppException(
            SpringError.SPRING_BUSINESS_RULE_VIOLATION,
            ex.getMessage()
        );

        return ResponseEntity
            .status(appEx.getHttpStatus().getValue())
            .body(ErrorResponse.fromAppException(appEx, "/custom"));
    }
}
```

## Testing

### Disable for Unit Tests

```properties
# application-test.properties
guard4j.enabled=false
```

### Test with MockMvc

```java
@SpringBootTest
@AutoConfigureTestDatabase
@Testcontainers
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnErrorWhenUserNotFound() throws Exception {
        mockMvc.perform(get("/users/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("SPRING_DATA_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("User not found with ID: 999"))
            .andExpect(jsonPath("$.path").value("/users/999"));
    }
}
```

## Environment-Specific Configuration

### Development
```properties
guard4j.web.include-stack-trace=true
guard4j.observability.enabled=true
guard4j.observability.level-overrides.SPRING_VALIDATION_FAILED=DEBUG
```

### Production
```properties
guard4j.web.include-stack-trace=false
guard4j.observability.enabled=true
guard4j.observability.metrics.enabled=true
guard4j.observability.tracing.enabled=false
```

### Testing
```properties
guard4j.web.global-exception-handler.enabled=false
guard4j.observability.enabled=false
```

## Integration with Spring Boot Actuator

When Spring Boot Actuator is present, Guard4j metrics are automatically exposed:

```
/actuator/metrics/guard4j.errors.total
/actuator/metrics/guard4j.errors.duration
```

## Migration from Manual Configuration

If you were using Guard4j manually, migration is simple:

1. Replace manual bean definitions with the starter dependency
2. Move configuration to `application.properties`
3. Remove manual `@ComponentScan` for Guard4j packages
4. The starter handles everything automatically

## Compatibility

- **Spring Boot**: 3.0.0+
- **Java**: 17+
- **Guard4j API**: 1.0.0+

## License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.
