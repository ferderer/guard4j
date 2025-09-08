# Guard4j Spring Boot Observability

The Guard4j Spring Boot starter provides comprehensive observability features for monitoring error handling and business events in your application.

## Features

- **Micrometer Metrics**: Automatic collection of error counts and timing metrics
- **Enhanced Logging**: Structured logging with MDC context
- **Spring Boot Actuator Integration**: Seamless integration with monitoring systems
- **Configurable Processing**: Fine-grained control over what gets observed

## Configuration

Configure observability features in your `application.yml` or `application.properties`:

```yaml
guard4j:
  observability:
    enabled: true                    # Enable/disable observability (default: true)
    metrics-enabled: true            # Enable metrics collection (default: true)
    metrics-prefix: "guard4j"        # Metrics name prefix (defaults to spring.application.name)
    logging-enabled: true            # Enable enhanced logging (default: true)
    include-mdc: true                # Include MDC context in logs (default: true)
```

### Metrics Prefix Behavior

The `metrics-prefix` property determines how your Guard4j metrics are named:

1. **Explicit Configuration**: If you set `guard4j.observability.metrics-prefix=myapp`, metrics will be named `myapp.events`, `myapp.errors`, etc.

2. **Application Name Default**: If not configured, the prefix defaults to your `spring.application.name` property. For example:
   ```yaml
   spring:
     application:
       name: user-service
   ```
   Results in metrics like `user-service.events`, `user-service.errors`.

3. **Fallback**: If no application name is configured, it falls back to `guard4j`.

This automatic behavior ensures your metrics are properly namespaced by application without requiring explicit configuration.

## Metrics

When metrics are enabled, the following metrics are automatically collected:

### Counter Metrics

- **guard4j.events**: Total count of processed events
  - Tags: `event_type`, `level`

### Timer Metrics

- **guard4j.errors**: Timing metrics for error events (WARN, ERROR, FATAL levels)
  - Tags: `event_type`, `level`

## Logging

Enhanced logging provides structured information about Guard4j events:

```
2025-09-07 18:42:31.502 [main] WARN  o.s.o.SpringObservabilityProcessor -- Guard4j event: business.event at level WARN
```

### MDC Context

When `include-mdc` is enabled, the following MDC properties are added:

- `guard4j.event.type`: The event type identifier
- `guard4j.event.level`: The event level (TRACE, DEBUG, INFO, WARN, ERROR, FATAL)
- `guard4j.event.timestamp`: ISO timestamp of the event
- `guard4j.event.has_metrics`: Whether the event includes metrics

## Spring Boot Actuator Integration

The observability processor integrates seamlessly with Spring Boot Actuator endpoints:

### Metrics Endpoint

Access metrics at `/actuator/metrics`:

```bash
# View all Guard4j metrics
curl http://localhost:8080/actuator/metrics | grep guard4j

# View specific metric
curl http://localhost:8080/actuator/metrics/guard4j.events
curl http://localhost:8080/actuator/metrics/guard4j.errors
```

### Health Endpoint

The observability processor is automatically included in health checks when Spring Boot Actuator is present.

## Monitoring Integration

### Prometheus

Guard4j metrics are automatically exposed in Prometheus format when `micrometer-registry-prometheus` is on the classpath:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Example Prometheus queries:

```promql
# Total Guard4j events by type
sum by (event_type) (guard4j_events_total)

# Error rate over time
rate(guard4j_errors_total[5m])

# Error events by level
sum by (level) (guard4j_events_total{level=~"error|warn|fatal"})
```

### Grafana Dashboard

Create a Grafana dashboard with panels for:

1. **Error Rate**: `rate(guard4j_errors_total[5m])`
2. **Event Distribution**: `sum by (event_type) (guard4j_events_total)`
3. **Error Levels**: `sum by (level) (guard4j_events_total{level=~"error|warn|fatal"})`

## Custom Observability

You can implement custom observability by creating your own `ObservabilityProcessor`:

```java
@Component
public class CustomObservabilityProcessor implements ObservabilityProcessor {

    @Override
    public void process(ObservableEvent event) {
        // Custom observability logic
        // e.g., send to external monitoring system
    }
}
```

## Dependencies

To enable observability features, ensure these dependencies are present:

```xml
<!-- Required for metrics -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Required for core metrics functionality -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
</dependency>

<!-- Optional: Prometheus registry -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

The Guard4j Spring Boot starter automatically includes these as optional dependencies, so they're only activated when present on your classpath.
