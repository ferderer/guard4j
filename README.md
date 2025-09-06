# Guard4j

Production-grade error handling and observability for Java applications. Framework-agnostic core with seamless Spring Boot, Quarkus, and Micronaut integration.

## The Problem

Enterprise Java applications typically suffer from inconsistent error handling that breaks down in production:

- Generic `Map<String, Object>` error responses that clients can't reliably parse
- Framework-specific error handling that doesn't work when you switch frameworks
- No built-in observability - errors disappear into logs without metrics or alerting
- Testing nightmares where MockMvc tests pass but production servlet errors fail

## The Solution

Guard4j provides type-safe error handling with automatic observability that works identically across all major Java frameworks:

```java
// Clean, fluent error creation with business context
throw new AppException(ErrorCodes.BUSINESS_RULE_VIOLATION)
    .withUserId(user.getId())
    .withData("rule", "DAILY_TRANSFER_LIMIT")
    .withData("limit", 10000)
    .withData("attempted", 15000);
```

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 422,
  "error": "Unprocessable Entity", 
  "code": "BUSINESS_RULE_VIOLATION",
  "data": {
    "userId": "user123",
    "rule": "DAILY_TRANSFER_LIMIT",
    "limit": 10000,
    "attempted": 15000,
    "retryable": false,
    "severity": "medium"
  }
}
```

## Key Features

- **Type-safe error responses** - No more `Map<String, Object>` - structured, predictable JSON
- **Framework-agnostic** - Same error handling code works in Spring Boot, Quarkus, and Micronaut
- **Automatic observability** - Built-in metrics, structured logging, and alerting integration
- **Production-ready** - Configurable alert levels, retry logic, and environment-specific behavior
- **Zero-config setup** - Add dependency, start throwing better exceptions

## Quick Start (Spring Boot)

### 1. Add Dependency

```xml
<dependency>
    <groupId>de.ferderer.guard4j</groupId>
    <artifactId>guard4j-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Use in Your Code

```java
@RestController
public class TransferController {
    
    @PostMapping("/transfers")
    public TransferResult transfer(@RequestBody TransferRequest request) {
        // Business rule validation with context
        if (account.getBalance() < request.getAmount()) {
            throw new AppException(ErrorCodes.BUSINESS_RULE_VIOLATION)
                .withUserId(request.getUserId())
                .withData("rule", "INSUFFICIENT_FUNDS")
                .withData("balance", account.getBalance())
                .withData("requested", request.getAmount());
        }
        
        return transferService.execute(request);
    }
}
```

### 3. Get Structured Errors + Automatic Observability

Guard4j automatically provides:
- **Structured error responses** that clients can reliably parse
- **Micrometer metrics** for error rates, categories, and business impact
- **Structured logging** with correlation IDs and business context
- **Framework exception mapping** - Spring validation errors become consistent ErrorResponse objects

## Framework Support

| Framework | Status | Artifact |
|-----------|--------|----------|
| Spring Boot 3.x | ✅ Ready | `guard4j-spring-boot-starter` |
| Quarkus 3.x | ✅ Ready | `guard4j-quarkus` |
| Micronaut 4.x | ✅ Ready | `guard4j-micronaut` |

## Custom Error Codes

Create domain-specific error codes for your business logic:

```java
public enum PaymentErrorCodes implements ErrorCode {
    PAYMENT_GATEWAY_UNAVAILABLE(
        HttpStatus.SERVICE_UNAVAILABLE,
        Message.of("payment.gateway.unavailable", "Payment gateway temporarily unavailable"),
        Severity.CRITICAL,
        Category.EXTERNAL
    ),
    DAILY_LIMIT_EXCEEDED(
        HttpStatus.UNPROCESSABLE_ENTITY, 
        Message.of("payment.daily_limit", "Daily payment limit exceeded"),
        Severity.WARN,
        Category.BUSINESS
    );
    
    // Standard ErrorCode implementation...
}
```

## Business Event Observability

Guard4j also provides unified observability for business events:

```java
// Log business events with automatic context injection
Guard4j.log(QuizCompletedEvent.of(quiz.getId(), score, duration));

// Automatic metrics: guard4j.quiz_completed{user_type="premium", locale="en"}
// Structured logs with user context, session info, and business data
```

## Requirements

- **Java 17+**
- **Spring Boot 3.0+** / **Quarkus 3.0+** / **Micronaut 4.0+**

## Documentation

- [Getting Started Guide](https://guard4j.dev/docs/getting-started)
- [Framework-Specific Setup](https://guard4j.dev/docs/frameworks)
- [Custom Error Codes](https://guard4j.dev/docs/guides/custom-error-codes)
- [Production Configuration](https://guard4j.dev/docs/guides/production)
- [API Reference](https://guard4j.dev/docs/api)

## Examples

Complete working examples for all supported frameworks:

- [Trading API - Spring Boot](examples/trading-spring-boot/)
- [Trading API - Quarkus](examples/trading-quarkus/)  
- [Trading API - Micronaut](examples/trading-micronaut/)

## License

Apache License 2.0 - see [LICENSE](LICENSE) for details.

## Contributing

Contributions welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

---

**Why Guard4j?** As an independent Java consultant since 2001, I've seen the same error handling mistakes in enterprise projects across many companies. Guard4j provides the production-grade error handling and observability that Java applications actually need.
