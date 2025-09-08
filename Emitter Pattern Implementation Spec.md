# Guard4j Emitter Pattern Implementation Spec

## Overview
Replace the current `Guard4j.log()` static method with an `Emitter` pattern that follows SLF4J conventions while simplifying the event model.

## Core Changes

### 1. New Emitter Interface
**File**: `guard4j-api/src/main/java/de/ferderer/guard4j/Emitter.java`

```java
public interface Emitter {
    void trace(ObservableEvent event);
    void debug(ObservableEvent event);
    void info(ObservableEvent event);
    void warn(ObservableEvent event);
    void error(ObservableEvent event);
}
```

### 2. Update Guard4j Main Class
**File**: `guard4j-api/src/main/java/de/ferderer/guard4j/Guard4j.java`

**Remove**:
- `log(ObservableEvent event)` method
- `isActive()` method

**Add**:
```java
public static Emitter getEmitter(Class<?> clazz) {
    return EmitterFactory.getEmitter(clazz.getName());
}
```

### 3. New EmitterFactory
**File**: `guard4j-api/src/main/java/de/ferderer/guard4j/EmitterFactory.java`

```java
public class EmitterFactory {
    private static final Map<String, Emitter> emitterCache = new ConcurrentHashMap<>();
    private static ObservabilityProcessor processor;
    
    public static void setProcessor(ObservabilityProcessor processor) {
        EmitterFactory.processor = processor;
    }
    
    public static Emitter getEmitter(String name) {
        return emitterCache.computeIfAbsent(name, className -> 
            new DefaultEmitter(className, processor));
    }
}
```

### 4. Default Emitter Implementation
**File**: `guard4j-api/src/main/java/de/ferderer/guard4j/DefaultEmitter.java`

```java
public class DefaultEmitter implements Emitter {
    private final String className;
    private final ObservabilityProcessor processor;
    
    @Override
    public void info(ObservableEvent event) {
        if (processor != null) {
            processor.processWithLevel(event, Level.INFO, className);
        }
    }
    
    // Similar for trace, debug, warn, error...
}
```

### 5. Simplify ObservableEvent Interface
**File**: `guard4j-api/src/main/java/de/ferderer/guard4j/observability/ObservableEvent.java`

**Remove**:
- `Level level()` method
- `boolean hasMetrics()` method

**Update**:
```java
public interface ObservableEvent {
    
    default String eventType() {
        return getClass().getSimpleName()
            .replaceAll("([a-z])([A-Z])", "$1-$2")
            .toLowerCase();
    }
    
    default int metric() {
        return 1;
    }
    
    default Instant timestamp() {
        return Instant.now();
    }
}
```

### 6. Update ObservabilityProcessor Interface
**File**: `guard4j-api/src/main/java/de/ferderer/guard4j/observability/ObservabilityProcessor.java`

**Add**:
```java
void processWithLevel(ObservableEvent event, Level level, String loggerName);
```

**Keep existing**:
```java
void process(ObservableEvent event); // For backward compatibility
```

### 7. Remove Deprecated Classes
**Delete these files**:
- `EventConfig.java`
- `BusinessEvent.java` 
- `CoreEvents.java`

### 8. Update SpringObservabilityProcessor
**File**: `guard4j-spring/src/main/java/de/ferderer/guard4j/spring/observability/SpringObservabilityProcessor.java`

**Key changes**:
- Replace event-based logger names with class-based: `getClassLogger(String className)`
- Update `processWithLevel()` method to use provided level instead of `event.level()`
- Update metrics to use `event.metric()` value: `counter.increment(event.metric())`
- Change logger cache from event-type to class-name based

**Logger naming**:
```java
private Logger getClassLogger(String className) {
    return loggerCache.computeIfAbsent(className, LoggerFactory::getLogger);
}
```

### 9. Update Framework Auto-Configuration
**Files**: All auto-configuration classes

**Change**:
```java
// Old
Guard4j.setProcessor(observabilityProcessor);

// New  
EmitterFactory.setProcessor(observabilityProcessor);
```

### 10. Update Documentation Examples
**Files**: All README.md, documentation files

**Change usage examples from**:
```java
Guard4j.log(new OrderCancelledEvent(orderId));
```

**To**:
```java
private static final Emitter events = Guard4j.getEmitter(OrderService.class);
events.info(new OrderCancelledEvent(orderId));
```

## Migration Impact

### Breaking Changes
- `Guard4j.log()` method removed
- `EventConfig` interface removed  
- `BusinessEvent` record removed
- `ObservableEvent.level()` method removed
- `ObservableEvent.hasMetrics()` method removed

### New Developer Experience
```java
// Simple event definition
public record PaymentProcessedEvent(String paymentId, BigDecimal amount) implements ObservableEvent {}

// Usage in service class
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final Emitter events = Guard4j.getEmitter(PaymentService.class);
    
    public void processPayment(Payment payment) {
        log.info("Processing payment {}", payment.getId());
        
        // Business event emission
        events.info(new PaymentProcessedEvent(payment.getId(), payment.getAmount()));
    }
}
```

### Logger Names
- Old: `guard4j.events.payment-processed-event`
- New: `com.mycompany.service.PaymentService`

This provides the familiar SLF4J pattern while dramatically simplifying event creation and maintaining all observability benefits.