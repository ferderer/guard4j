# Guard4j Metrics Prefix Example

This example demonstrates how Guard4j automatically uses your Spring application name as the metrics prefix.

## Example 1: Application with Name

**application.yml:**
```yaml
spring:
  application:
    name: user-service

guard4j:
  observability:
    enabled: true
    # metrics-prefix not configured - will use "user-service"
```

**Result:** Metrics will be named:
- `user-service.events`
- `user-service.errors`

## Example 2: Custom Metrics Prefix

**application.yml:**
```yaml
spring:
  application:
    name: user-service

guard4j:
  observability:
    enabled: true
    metrics-prefix: "custom-prefix"  # Override application name
```

**Result:** Metrics will be named:
- `custom-prefix.events`
- `custom-prefix.errors`

## Example 3: Default Fallback

**application.yml:**
```yaml
# No spring.application.name configured

guard4j:
  observability:
    enabled: true
    # metrics-prefix not configured - will fallback to "guard4j"
```

**Result:** Metrics will be named:
- `guard4j.events`
- `guard4j.errors`

## Prometheus Query Examples

With application name `user-service`:

```promql
# Total events for this service
sum(user_service_events_total)

# Error rate for this service
rate(user_service_errors_total[5m])

# Events by type
sum by (event_type) (user_service_events_total)
```

This automatic naming ensures your metrics are properly organized by service without requiring manual configuration!
