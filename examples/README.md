# FinStream Guard4j Examples

This directory contains complete working examples of **Guard4j's framework-agnostic error handling** across different Java frameworks, using a realistic stock price streaming application.

## 🎯 **Overview**

**FinStream** demonstrates Guard4j's comprehensive error handling capabilities through:
- **Real-time stock price streaming** with 1-second updates
- **External API integration** with Finnhub (realistic error scenarios)
- **WebSocket broadcasting** with error recovery
- **Production observability** with VictoriaMetrics + VictoriaLogs + Grafana
- **Business KPI tracking** instead of log parsing (ReTrust pattern)

## 📁 **Project Structure**

```
examples/
├── pom.xml                          # Parent POM for all examples
├── docker-compose.yml               # Shared observability stack
├── config/                          # Shared configuration
│   ├── prometheus.yml               # Metrics scraping
│   └── grafana/                     # Dashboards and datasources
├── .env.example                     # Environment template
├── run-demo.sh                      # Master demo runner
├──
├── finstream-spring-boot/           # Spring Boot implementation
├── finstream-quarkus/               # Quarkus implementation (coming soon)
├── finstream-micronaut/             # Micronaut implementation (coming soon)
└── finstream-frontend/              # Shared Angular frontend (coming soon)
```

## 🚀 **Quick Start**

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven 3.8+
- Finnhub API key (free at [finnhub.io](https://finnhub.io/register))

### Setup
```bash
# Clone and navigate
cd examples/

# Set up environment
cp .env.example .env
# Edit .env and add your FINNHUB_API_KEY

# Start any framework demo
./run-demo.sh spring-boot    # Spring Boot implementation
./run-demo.sh quarkus        # Quarkus implementation (coming soon)
./run-demo.sh micronaut      # Micronaut implementation (coming soon)
```

### Access Points (All Frameworks)
- **Application**: http://localhost:808x (different ports per framework)
- **Grafana**: http://localhost:3000 (admin/admin)
- **VictoriaMetrics**: http://localhost:8428
- **VictoriaLogs**: http://localhost:9428

## 🔍 **Framework Comparison**

| Framework | Port | Status | Error Handling | Business Logic | API Patterns |
|-----------|------|--------|---------------|----------------|--------------|
| **Spring Boot** | 8080 | 🛠️ WIP | Identical | Identical | @RestController |
| **Quarkus** | 8081 | 🚧 Coming | Identical | Identical | JAX-RS |
| **Micronaut** | 8082 | 🚧 Coming | Identical | Identical | @Controller |

**Key Point**: Same `FinStreamError` enum and business logic across all frameworks!

## 📊 **Demo Features**

### Error Scenarios (All Frameworks)
1. **External Service Errors**
   - `FINNHUB_TIMEOUT` - Network delays
   - `FINNHUB_RATE_LIMIT_EXCEEDED` - API limits
   - `FINNHUB_SERVICE_UNAVAILABLE` - Service downtime

2. **Validation Errors**
   - `INVALID_STOCK_SYMBOL` - Format validation
   - `MISSING_STOCK_SYMBOL` - Required fields

3. **WebSocket Errors**
   - `WEBSOCKET_CONNECTION_FAILED` - Connection issues
   - `PRICE_BROADCAST_FAILED` - Broadcasting failures

### Stock Rotation (All Frameworks)
Real-time updates every second for 12 tech stocks:
```
AAPL → GOOGL → MSFT → AMZN → TSLA → NVDA → META → NFLX → AMD → CRM → UBER → SPOT
```

## 🏗️ **Shared Infrastructure**

### Docker Compose Stack
```yaml
services:
  victoriametrics:    # Metrics storage
  victorialogs:       # Log aggregation
  grafana:           # Dashboards
  finstream-spring-boot:   # Port 8080
  finstream-quarkus:       # Port 8081 (coming)
  finstream-micronaut:     # Port 8082 (coming)
  finstream-frontend:      # Port 4200 (coming)
```

### Observability Features
- **Metrics**: Error rates, response times, connection health
- **Logging**: Structured JSON with Guard4j context
- **Dashboards**: Real-time monitoring and error tracking
- **Alerts**: Business-critical error notifications

## 📈 **Implementation Status**

### Spring Boot ✅
- [x] Project foundation and Maven setup
- [ ] Core error handling with Guard4j
- [ ] Finnhub API integration
- [ ] WebSocket real-time updates
- [ ] Complete observability stack

### Quarkus 🚧
- [ ] Project setup
- [ ] JAX-RS resource layer
- [ ] Same business logic as Spring Boot
- [ ] Framework-specific Guard4j integration

### Micronaut 🚧
- [ ] Project setup
- [ ] Controller layer
- [ ] Same business logic as Spring Boot
- [ ] Framework-specific Guard4j integration

### Frontend 🚧
- [ ] Angular 18+ setup
- [ ] Real-time WebSocket service
- [ ] Stock price grid component
- [ ] Error notification system

## 🎯 **Value Proposition**

**Guard4j enables framework migration without changing error handling code:**

```java
// Same error definition across ALL frameworks
public enum FinStreamError implements Error {
    FINNHUB_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "Stock price request timeout", Level.WARN, Category.EXTERNAL)
}

// Same business logic across ALL frameworks
public class StockService {
    public StockPrice getStock(String symbol) {
        try {
            return finnhubClient.getQuote(symbol);
        } catch (ResourceAccessException ex) {
            throw new AppException(FinStreamError.FINNHUB_TIMEOUT, ex)
                .withData("symbol", symbol);
        }
    }
}
```

## 📚 **Documentation**

- [FinStream Roadmap](FINSTREAM_ROADMAP.md) - Detailed implementation plan
- [Spring Boot Demo](finstream-spring-boot/README.md) - Spring Boot specific docs
- [Guard4j Documentation](../README.md) - Main Guard4j documentation

## 🤝 **Contributing**

Each framework implementation should demonstrate identical error handling behavior while following framework-specific patterns and conventions.

## 📄 **License**

Apache License 2.0 - same as Guard4j
