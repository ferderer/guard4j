package de.ferderer.guard4j.observability;

import de.ferderer.guard4j.classification.Level;
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
     * Process an observability event with a specific level and logger name.
     *
     * <p>This method is used by the new Emitter pattern to provide
     * explicit level control and class-based logger naming.
     *
     * @param event the event to process
     * @param level the logging level to use
     * @param loggerName the logger name (typically a class name)
     * @since 2.0.0
     */
    void processWithLevel(ObservableEvent event, Level level, String loggerName);

    /**
     * Process events asynchronously (default behavior).
     */
    default CompletableFuture<Void> processAsync(ObservableEvent event) {
        return CompletableFuture.runAsync(() -> process(event));
    }
}
