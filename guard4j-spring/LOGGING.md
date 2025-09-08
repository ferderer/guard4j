# Example logback-spring.xml configuration for Guard4j

```xml
<configuration>
    <!-- Standard console appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level [%X{guard4j.app:-}] %logger{36} -- %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Guard4j specific appender with rich MDC context -->
    <appender name="GUARD4J" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%X{guard4j.app:-unknown}] [%X{guard4j.event.type}] %-5level %logger{36} -- %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Route Guard4j logs to specific appender -->
    <logger name="de.ferderer.guard4j.spring.observability.SpringObservabilityProcessor" level="INFO" additivity="false">
        <appender-ref ref="GUARD4J"/>
    </logger>

    <!-- Root logger -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

## Benefits of MDC Approach

1. **Standard Logger Names**: Uses predictable, configurable logger names
2. **Rich Context**: Application name and event details available via MDC
3. **Flexible Configuration**: Easy to route logs based on application or event type
4. **Performance**: No runtime logger creation overhead
5. **Familiar**: Standard SLF4J/Logback experience for developers

## Example Log Output

With the configuration above, you'll see logs like:
```
20:32:26.459 [test-app] [business.event] WARN  d.f.g.s.o.SpringObservabilityProcessor -- Guard4j event: business.event at level WARN
20:32:26.694 [test-app] [test.event] INFO  d.f.g.s.o.SpringObservabilityProcessor -- Guard4j event: test.event at level INFO
```
