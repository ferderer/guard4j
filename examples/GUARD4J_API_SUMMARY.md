# Guard4j API Summary

## Factory & Business Events

### Core Factory

```java
// Get an emitter for your class
private static final Emitter events = Guard4j.getEmitter(PaymentService.class);
```

### Event Definition

Simple records implementing `ObservableEvent`:

```java
public record PaymentProcessedEvent(String paymentId, BigDecimal amount) 
    implements ObservableEvent {}

public record UserRegisteredEvent(String userId, String userType) 
    implements ObservableEvent {
    
    @Override
    public int metric() {
        return "premium".equals(userType) ? 5 : 1; // Custom metric value
    }
}
```

### Event Emission

SLF4J-style methods for different levels:

```java
public class PaymentService {
    private static final Emitter events = Guard4j.getEmitter(PaymentService.class);
    
    public void processPayment(Payment payment) {
        // Business event emission
        events.info(new PaymentProcessedEvent(payment.getId(), payment.getAmount()));
        
        // Different levels available
        events.debug(new PaymentStartedEvent(payment.getId()));
        events.warn(new PaymentRetryEvent(payment.getId(), attempt));
        events.error(new PaymentFailedEvent(payment.getId(), reason));
    }
}
```

### Automatic Features

- **Metrics**: Auto-incremented counters (`guard4j.payment_processed`)
- **Logging**: Class-based logger names (`com.company.PaymentService`)  
- **Context**: MDC enrichment (app name, event type, timestamp)
- **Async**: Non-blocking event processing
- **Framework Integration**: Auto-configured in Spring Boot, Quarkus, Micronaut

### Key Interfaces

- `Guard4j.getEmitter(Class)` - Factory method
- `Emitter` - SLF4J-style emission (trace, debug, info, warn, error)
- `ObservableEvent` - Simple event contract (type + optional metric)
- `EmitterFactory` - Internal factory (configured by frameworks)
