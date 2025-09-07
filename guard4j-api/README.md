# Guard4j API

The framework-agnostic core of Guard4j providing error handling and observability interfaces for Java applications.

## Overview

This module contains the core interfaces and implementations that define Guard4j's error handling and observability model. It has zero dependencies on any specific framework, making it suitable for use across Spring Boot, Quarkus, Micronaut, and other Java frameworks.

## Key Components

### Error Handling

#### Error Interface
Defines structured error codes with HTTP status mapping, severity levels, and categorization:

```java
public enum PaymentErrorCodes implements Error {
    INSUFFICIENT_FUNDS(
        HttpStatus.UNPROCESSABLE_ENTITY,
        "Insufficient funds",
        Level.WARN,
        Category.BUSINESS
    ),

    GATEWAY_TIMEOUT(
        HttpStatus.GATEWAY_TIMEOUT,
        "Payment gateway timeout",
        Level.ERROR,
        Category.EXTERNAL
    );

    private final HttpStatus httpStatus;
    private final String message;
    private final Level level;
    private final Category category;

    PaymentErrorCodes(HttpStatus httpStatus, String message, Level level, Category category) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.level = level;
        this.category = category;
    }

    @Override public HttpStatus httpStatus() { return httpStatus; }
    @Override public String message() { return message; }
    @Override public Level level() { return level; }
    @Override public Category category() { return category; }
}
```

#### AppException
Framework-agnostic exception with fluent API for contextual data:

```java
// Simple business error
throw new AppException(PaymentErrorCodes.INSUFFICIENT_FUNDS);

// With contextual data
throw new AppException(PaymentErrorCodes.INSUFFICIENT_FUNDS)
    .withData("userId", currentUser.getId())
    .withData("requestedAmount", transfer.getAmount())
    .withData("availableBalance", account.getBalance());
```

### Observability

#### Business Events
Track domain-specific operations and user actions:

```java
public enum UserEvents implements EventConfig {
    USER_REGISTERED(Level.INFO, true),
    PROFILE_UPDATED(Level.INFO, true),
    PASSWORD_CHANGED(Level.WARN, true);

    private final Level level;
    private final boolean hasMetrics;

    UserEvents(Level level, boolean hasMetrics) {
        this.level = level;
        this.hasMetrics = hasMetrics;
    }

    @Override public Level level() { return level; }
    @Override public boolean hasMetrics() { return hasMetrics; }
}

// Event record implementation
public record UserRegisteredEvent(
    String userId,
    String registrationSource,
    EventConfig config
) implements BusinessEvent {

    public UserRegisteredEvent(String userId, String registrationSource) {
        this(userId, registrationSource, UserEvents.USER_REGISTERED);
    }
}

// Log business events
Guard4j.log(new UserRegisteredEvent(userId, registrationSource));
```

#### Error Events
Automatically captured when AppException is thrown (handled by framework integrations).

### Framework Integration

Framework-specific modules automatically configure Guard4j:

- **Spring Boot**: `guard4j-spring-boot-starter`
- **Quarkus**: `guard4j-quarkus`
- **Micronaut**: `guard4j-micronaut`

## Core Interfaces

### Error Handling
- `Error` - Base interface for all error codes
- `AppException` - Main exception class with fluent API
- `HttpStatus` - Framework-agnostic HTTP status representation

### Observability
- `ObservableEvent` - Base interface for all events
- `BusinessEvent` - Domain-specific events with metrics and analytics
- `EventConfig` - Event configuration (log level, metrics)
- `Guard4j` - Main API entry point

### Classification
- `Category` - Error categorization (BUSINESS, VALIDATION, SECURITY, SYSTEM, EXTERNAL)
- `Level` - Unified level for severity, logging, and alerting (TRACE, DEBUG, INFO, WARN, ERROR, FATAL)

## Standard Error Codes

The module provides `CoreError` enum with common error scenarios:

```java
// Validation errors
CoreError.VALIDATION_FAILED
CoreError.REQUIRED_FIELD_MISSING
CoreError.INVALID_FORMAT

// Authentication & Authorization
CoreError.AUTHENTICATION_REQUIRED
CoreError.INVALID_CREDENTIALS
CoreError.ACCESS_DENIED

// Resource management
CoreError.RESOURCE_NOT_FOUND
CoreError.RESOURCE_ALREADY_EXISTS
CoreError.RESOURCE_CONFLICT

// Business logic
CoreError.BUSINESS_RULE_VIOLATION
CoreError.OPERATION_NOT_ALLOWED

// System errors
CoreError.INTERNAL_ERROR
CoreError.DATABASE_ERROR
CoreError.CONFIGURATION_ERROR

// External services
CoreError.EXTERNAL_SERVICE_ERROR
CoreError.EXTERNAL_SERVICE_UNAVAILABLE
CoreError.EXTERNAL_SERVICE_TIMEOUT
```

## Usage Patterns

### Creating Custom Error Codes

```java
public enum OrderErrorCodes implements Error {
    ORDER_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "Order not found",
        Level.WARN,
        Category.BUSINESS
    ),

    ORDER_ALREADY_SHIPPED(
        HttpStatus.UNPROCESSABLE_ENTITY,
        "Order has already been shipped",
        Level.WARN,
        Category.BUSINESS
    ),

    INVENTORY_SERVICE_DOWN(
        HttpStatus.SERVICE_UNAVAILABLE,
        "Inventory service is unavailable",
        Level.ERROR,
        Category.EXTERNAL
    );

    private final HttpStatus httpStatus;
    private final String message;
    private final Level level;
    private final Category category;

    OrderErrorCodes(HttpStatus httpStatus, String message, Level level, Category category) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.level = level;
        this.category = category;
    }

    @Override public HttpStatus httpStatus() { return httpStatus; }
    @Override public String message() { return message; }
    @Override public Level level() { return level; }
    @Override public Category category() { return category; }
}
```

### Error Handling in Services

```java
@Service
public class OrderService {

    public Order findById(String orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new AppException(OrderErrorCodes.ORDER_NOT_FOUND)
                .withData("orderId", orderId));
    }

    public void cancelOrder(String orderId) {
        Order order = findById(orderId);

        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new AppException(OrderErrorCodes.ORDER_ALREADY_SHIPPED)
                .withData("orderId", orderId)
                .withData("currentStatus", order.getStatus())
                .withData("shippedDate", order.getShippedDate());
        }

        // Cancel order logic...
    }
}
```

### Business Event Tracking

```java
public enum OrderEvents implements EventConfig {
    ORDER_PLACED(Level.INFO, true),
    ORDER_SHIPPED(Level.INFO, true),
    ORDER_CANCELLED(Level.WARN, true);

    private final Level level;
    private final boolean hasMetrics;

    OrderEvents(Level level, boolean hasMetrics) {
        this.level = level;
        this.hasMetrics = hasMetrics;
    }

    @Override public Level level() { return level; }
    @Override public boolean hasMetrics() { return hasMetrics; }
}

// Event record with custom fields
public record OrderPlacedEvent(
    String orderId,
    String customerId,
    BigDecimal orderTotal,
    EventConfig config
) implements BusinessEvent {

    public OrderPlacedEvent(String orderId, String customerId, BigDecimal orderTotal) {
        this(orderId, customerId, orderTotal, OrderEvents.ORDER_PLACED);
    }
}

// In your service
public class OrderService {

    public Order placeOrder(CreateOrderRequest request) {
        Order order = createOrder(request);

        // Log business event
        Guard4j.log(new OrderPlacedEvent(order.getId(), order.getCustomerId(), order.getTotal()));

        return order;
    }
}
```

## Error Categories and Retry Logic

Categories automatically determine retry behavior:

- **BUSINESS** - Business rule violations (not retryable)
- **VALIDATION** - Input validation failures (not retryable)
- **SECURITY** - Authentication/authorization (not retryable)
- **SYSTEM** - Internal system errors (retryable)
- **EXTERNAL** - External service failures (retryable)

## Severity and Alert Levels

Level enum provides unified semantics from least to most severe:

- **TRACE/DEBUG** - Development diagnostics (no alerts)
- **INFO** - Normal operations (low priority alerts)
- **WARN** - Potentially harmful situations (medium priority alerts)
- **ERROR** - Error events that don't stop the application (high priority alerts)
- **FATAL** - Critical system failures (critical alerts requiring immediate attention)

## Framework Integration

This module provides the foundation that framework-specific integrations build upon. Framework modules handle:

- Automatic exception mapping from framework exceptions to Error codes
- HTTP response generation from AppException
- Context collection (user ID, request path, etc.) for observability
- Metrics integration (Micrometer, Prometheus)
- Logging integration (SLF4J, Logback)

## Dependencies

This module has **zero runtime dependencies** beyond the Java standard library, ensuring maximum compatibility across frameworks and minimal dependency conflicts.

Test dependencies:
- JUnit Jupiter 5.13.4
- AssertJ 3.27.4

## Version

**1.0.0** - Initial release with core error handling and observability interfaces.