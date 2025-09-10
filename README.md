# Guard4j
*– Guard4j events describe what happens in your domain, not how you measure it.*

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
    .withData("rule", "DAILY_TRANSFER_LIMIT")
    .withData("limit", 10000)
    .withData("attempted", 15000);
```

**Emitter Factory Pattern**
```java
public class PaymentService {
    private static final Emitter events = Guard4j.getEmitter(PaymentService.class);

    public void processPayment(Payment payment) {
        // Single event call generates both metrics and structured logs
        events.info(new PaymentProcessedEvent(payment.getId(), payment.getAmount()));
    }
}
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
    private static final Emitter events = Guard4j.getEmitter(TransferController.class);

    @PostMapping("/transfers")
    public TransferResult transfer(@RequestBody TransferRequest request) {
        // Business rule validation with context
        if (account.getBalance() < request.getAmount()) {
            // Exception handling
            throw new AppException(ErrorCodes.BUSINESS_RULE_VIOLATION)
                .withData("rule", "INSUFFICIENT_FUNDS")
                .withData("balance", account.getBalance())
                .withData("requested", request.getAmount());
        }

        // Business event observability - generates both metrics and logs
        events.info(new TransferInitiatedEvent(request.getUserId(), request.getAmount()));

        TransferResult result = transferService.execute(request);

        events.info(new TransferCompletedEvent(result.getTransferId(), result.getStatus()));
        return result;
    }
}

// Simple event definition
public record TransferInitiatedEvent(String userId, BigDecimal amount) implements ObservableEvent {}
public record TransferCompletedEvent(String transferId, String status) implements ObservableEvent {}
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
| Spring Boot 3.x | ✅ Production Ready | `guard4j-spring-boot-starter` |
| Quarkus 3.x | 🛠️ **In Active Development** | `guard4j-quarkus` |
| Micronaut 4.x | 🚧 Coming Soon | `guard4j-micronaut` |


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

// Usage with business events
public class PaymentService {
    private static final Emitter events = Guard4j.getEmitter(PaymentService.class);

    public void processPayment(Payment payment) {
        if (exceedsDailyLimit(payment)) {
            events.warn(new PaymentLimitExceededEvent(payment.getUserId(), payment.getAmount()));
            throw new AppException(PaymentErrorCodes.DAILY_LIMIT_EXCEEDED)
                .withUserId(payment.getUserId())
                .withData("amount", payment.getAmount())
                .withData("dailyLimit", getDailyLimit(payment.getUserId()));
        }

        events.info(new PaymentProcessedEvent(payment.getId(), payment.getAmount()));
    }
}
```

## Business Event Observability

Guard4j's **Emitter Factory pattern** provides unified observability for business events:

```java
public class LoanProcessorService {
    private static final Emitter events = Guard4j.getEmitter(LoanProcessorService.class);

    public void processLoan(LoanApplication loan) {
        // Replace verbose MeterRegistry + Logger calls with single event
        events.info(new LoanProcessingStartedEvent(loan.getId(), loan.getType()));

        try {
            LoanDecision decision = evaluateRules(loan);

            // Business event generates automatic metrics + structured logs
            events.info(new LoanProcessedEvent(loan, decision));
        } catch (Exception ex) {
            events.error(new LoanProcessingFailedEvent(loan, ex));
            throw ex;
        }
    }
}

// Simple event definitions
public record LoanProcessingStartedEvent(String loanId, String loanType) implements ObservableEvent {}

public record LoanProcessedEvent(String loanId, BigDecimal amount, String decision, long processingTimeMs)
    implements ObservableEvent {

    @Override
    public int metric() {
        return "APPROVED".equals(decision) ? 1 : 0; // Success rate tracking
    }
}
```

**Automatic Output:**
- **Metrics**: `guard4j_loan_processed_total{decision="approved", loan_type="mortgage"}`
- **Logs**: `{"level":"INFO","logger":"com.company.LoanProcessorService","event_type":"loan-processed","loan_id":"12345",...}`

## Architecture

Guard4j follows the **Emitter Factory Pattern** for clean, type-safe observability:

```java
// Get emitter for your class (cached, thread-safe)
private static final Emitter events = Guard4j.getEmitter(MyService.class);

// Emit events with appropriate log levels
events.info(new BusinessEvent(...));    // Business metrics + INFO logs
events.warn(new WarningEvent(...));     // Alert metrics + WARN logs
events.error(new ErrorEvent(...));      // Error metrics + ERROR logs
```

**Key Benefits:**
- **Single Event Definition**: One record generates both metrics and structured logs
- **Type Safety**: Compile-time validation of event structure
- **Logger Correlation**: Events use your class logger name for perfect correlation
- **Framework Agnostic**: Same API works across Spring Boot, Quarkus, Micronaut

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

- [FinStream Trading API - Spring Boot](examples/finstream-spring-boot/) 🛠️ **In Active Development**
- [FinStream Trading API - Quarkus](examples/finstream-quarkus/) 🚧 **Coming Soon**
- [FinStream Trading API - Micronaut](examples/finstream-micronaut/) 🚧 **Coming Soon**

## Roadmap

### Current Focus (September 2025)
- **Quarkus Extension** development for ReTrust production deployment

### Next Steps
- Finalize Quarkus extension with native compilation support
- Community feedback and improvements

## License

Apache License 2.0 - see [LICENSE](LICENSE) for details.

## Contributing

Contributions welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

---

**Why Guard4j?** As an independent consultant since 2001 (Java/Spring since 2009), I've seen the same error handling mistakes in enterprise projects across many companies. Guard4j provides the production-grade error handling and observability that Java applications actually need.
