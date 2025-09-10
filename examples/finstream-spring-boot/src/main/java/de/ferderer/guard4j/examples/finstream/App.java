package de.ferderer.guard4j.examples.finstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * FinStream - Real-time Stock Price Streaming Demo
 *
 * Demonstrates Guard4j's framework-agnostic error handling capabilities
 * through a real-world stock price streaming application.
 *
 * Features:
 * - Real-time stock price updates via WebSocket (1-second intervals)
 * - External API integration with Finnhub (rate limiting, timeouts, errors)
 * - Comprehensive error handling across all application layers
 * - Production-ready observability with VictoriaMetrics and VictoriaLogs
 * - Angular frontend with reactive error notifications
 *
 * Key Guard4j Demonstrations:
 * - External service error handling (FINNHUB_TIMEOUT, FINNHUB_RATE_LIMIT_EXCEEDED)
 * - Validation error handling (INVALID_STOCK_SYMBOL)
 * - WebSocket error scenarios (WEBSOCKET_CONNECTION_FAILED, PRICE_BROADCAST_FAILED)
 * - Structured error responses with contextual data
 * - Business event tracking and observability
 *
 * Demo Stocks (12 tech companies):
 * AAPL, GOOGL, MSFT, AMZN, TSLA, NVDA, META, NFLX, AMD, CRM, UBER, SPOT
 *
 * @author Guard4j Examples Team
 * @version 1.0.0
 * @see de.ferderer.guard4j.examples.finstream.error.FinStreamError
 */
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration.class,
})
@EnableScheduling
public class App {

    public static void main(String[] args) {
        System.out.println("🚀 Starting FinStream - Guard4j Stock Price Demo");
        System.out.println("📊 Real-time stock updates with comprehensive error handling");
        System.out.println("🔗 WebSocket endpoint: /ws");
        System.out.println("📈 REST API: /api/stocks");
        System.out.println("📊 Metrics: /actuator/prometheus");
        System.out.println("❤️  Health: /actuator/health");

        SpringApplication.run(App.class, args);

        System.out.println("✅ FinStream application started successfully!");
        System.out.println("🎯 Connect to http://localhost:8080 to see the demo");
        System.out.println("⚠️  Spring Boot default error handling is DISABLED - using Guard4j exclusively");
    }
}
