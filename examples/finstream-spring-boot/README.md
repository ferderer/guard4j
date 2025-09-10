# FinStream Spring Boot Demo

Real-time stock price streaming application showcasing **Guard4j's framework-agnostic error handling** with Spring Boot.

## 🎯 **Overview**

This demo application demonstrates Guard4j's comprehensive error handling capabilities through a realistic stock price streaming scenario featuring:

- **Real-time Updates**: Stock prices update every second via WebSocket
- **External API Integration**: Finnhub stock price API with realistic error scenarios
- **Rich Error Handling**: Comprehensive error scenarios across all application layers
- **Production Observability**: VictoriaMetrics + VictoriaLogs + Grafana integration

## 🚀 **Quick Start**

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Finnhub API key (free at [finnhub.io](https://finnhub.io/register))

### Running the Demo
```bash
# From examples/ directory
cp .env.example .env
# Edit .env and add your FINNHUB_API_KEY

# Start the complete stack
./run-demo.sh spring-boot
```

### Access Points
- **Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus
- **Grafana**: http://localhost:3000 (admin/admin)

## 📊 **Demo Features**

### Error Scenarios Demonstrated
1. **External Service Errors**
   - `FINNHUB_TIMEOUT` - Network timeout scenarios
   - `FINNHUB_RATE_LIMIT_EXCEEDED` - API rate limiting
   - `FINNHUB_SERVICE_UNAVAILABLE` - Service downtime

2. **Validation Errors**
   - `INVALID_STOCK_SYMBOL` - Malformed stock symbols
   - `MISSING_STOCK_SYMBOL` - Required parameter validation

3. **WebSocket Errors**
   - `WEBSOCKET_CONNECTION_FAILED` - Connection issues
   - `PRICE_BROADCAST_FAILED` - Broadcasting failures

### Stock Rotation
The demo continuously fetches prices for 12 tech stocks in 1-second intervals:
```
AAPL → GOOGL → MSFT → AMZN → TSLA → NVDA → META → NFLX → AMD → CRM → UBER → SPOT
```

## 🏗️ **Architecture**

```
App (Spring Boot - ErrorMvcAutoConfiguration EXCLUDED)
├── StockController (REST API)
├── StockService (Business Logic)
├── FinnhubClient (External API)
├── StockScheduler (1-second rotation)
├── WebSocketConfig (STOMP messaging)
└── StockBroadcastService (Real-time updates)
```

**Key Design Decision**: Spring Boot's default error handling (`ErrorMvcAutoConfiguration`) is completely excluded to showcase Guard4j's comprehensive error handling capabilities exclusively.

## 🔧 **Configuration**

Key configuration properties (see `application.properties`):

```properties
# Complete Spring Boot error handling exclusion
server.error.whitelabel.enabled=false
spring.mvc.throw-exception-if-no-handler-found=true
spring.web.resources.add-mappings=false

# Guard4j - Exclusive error handling control
guard4j.enabled=true
guard4j.web.enabled=true
guard4j.web.handle-spring-exceptions=true
guard4j.web.handle-validation-exceptions=true
guard4j.web.handle-security-exceptions=true
guard4j.observability.metrics-enabled=true

# Finnhub API
finnhub.api-key=${FINNHUB_API_KEY}
finnhub.timeout=5000

# Demo Stocks
finstream.demo.stocks=AAPL,GOOGL,MSFT,AMZN,TSLA,NVDA,META,NFLX,AMD,CRM,UBER,SPOT
finstream.demo.rotation-interval=1000
```

**Important**: This demo completely disables Spring Boot's default error handling to showcase Guard4j's capabilities exclusively.

## 📈 **Observability**

### Metrics
- Stock fetch success/failure rates
- API response times by symbol
- Error counts by category
- WebSocket connection health

### Logging
- Structured JSON logs with Guard4j context
- Error correlation with trace IDs
- Business event tracking

### Dashboards
- Real-time error monitoring
- Stock price fetch performance
- WebSocket connection status

## 🔗 **API Endpoints**

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/stocks/{symbol}` | GET | Get current stock price |
| `/api/stocks/search` | GET | Search stock symbols |
| `/ws` | WebSocket | Real-time price updates |
| `/actuator/health` | GET | Health check |
| `/actuator/prometheus` | GET | Metrics endpoint |

## 📝 **Implementation Status**

- [x] **Phase 1**: Project Foundation
- [ ] **Phase 2**: Core Error Handling
- [ ] **Phase 3**: Finnhub Integration
- [ ] **Phase 4**: Real-time Updates
- [ ] **Phase 5**: Angular Frontend
- [ ] **Phase 6**: Observability Integration
- [ ] **Phase 7**: Testing & Documentation

See [FINSTREAM_ROADMAP.md](../FINSTREAM_ROADMAP.md) for detailed implementation plan.

## 🤝 **Related Examples**

- [FinStream Quarkus](../finstream-quarkus/) - Same demo with Quarkus
- [FinStream Micronaut](../finstream-micronaut/) - Same demo with Micronaut
- [FinStream Frontend](../finstream-frontend/) - Angular frontend (shared)

## 📄 **License**

Apache License 2.0 - same as Guard4j
