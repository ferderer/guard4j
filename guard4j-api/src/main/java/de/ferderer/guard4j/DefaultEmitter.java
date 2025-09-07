package de.ferderer.guard4j;

import de.ferderer.guard4j.classification.Level;
import de.ferderer.guard4j.observability.ObservabilityProcessor;
import de.ferderer.guard4j.observability.ObservableEvent;

/**
 * Default implementation of the Emitter interface.
 *
 * <p>This implementation delegates event processing to the configured
 * {@link ObservabilityProcessor}, adding the appropriate logging level
 * and class name context.
 *
 * <p>If no processor is configured, events are silently ignored to
 * prevent application failures due to observability issues.
 *
 * @since 2.0.0
 */
class DefaultEmitter implements Emitter {

    private final String className;

    /**
     * Creates a new DefaultEmitter for the specified class.
     *
     * @param className the fully qualified class name
     * @param processor the observability processor, may be null (for compatibility)
     */
    DefaultEmitter(String className, ObservabilityProcessor processor) {
        this.className = className;
        // Note: We don't store the processor as it may be updated in the factory
    }

    @Override
    public void trace(ObservableEvent event) {
        processWithLevel(event, Level.TRACE);
    }

    @Override
    public void debug(ObservableEvent event) {
        processWithLevel(event, Level.DEBUG);
    }

    @Override
    public void info(ObservableEvent event) {
        processWithLevel(event, Level.INFO);
    }

    @Override
    public void warn(ObservableEvent event) {
        processWithLevel(event, Level.WARN);
    }

    @Override
    public void error(ObservableEvent event) {
        processWithLevel(event, Level.ERROR);
    }

    /**
     * Processes an event with the specified level.
     *
     * <p>Updates the processor reference from the factory to handle
     * cases where the processor is set after emitter creation.
     *
     * @param event the event to process
     * @param level the logging level
     */
    private void processWithLevel(ObservableEvent event, Level level) {
        if (event == null) {
            throw new NullPointerException("Event cannot be null");
        }

        // Get the current processor (may have been updated since emitter creation)
        ObservabilityProcessor currentProcessor = EmitterFactory.processor;
        if (currentProcessor != null) {
            currentProcessor.processWithLevel(event, level, className);
        }
    }
}
