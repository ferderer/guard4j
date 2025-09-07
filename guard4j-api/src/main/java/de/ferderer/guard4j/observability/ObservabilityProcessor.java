package de.ferderer.guard4j.observability;

import java.util.concurrent.CompletableFuture;

/**
 * Main processor for observability events.
 * Handles logging, metrics, and tracing based on event configuration.
 */
public interface ObservabilityProcessor {
    
    /**
     * Process an observability event.
     */
    void process(ObservableEvent event);
    
    /**
     * Process events asynchronously (default behavior).
     */
    default CompletableFuture<Void> processAsync(ObservableEvent event) {
        return CompletableFuture.runAsync(() -> process(event));
    }
}
