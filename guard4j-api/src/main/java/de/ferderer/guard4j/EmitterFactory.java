package de.ferderer.guard4j;

import de.ferderer.guard4j.observability.ObservabilityProcessor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and managing Emitter instances.
 *
 * <p>This factory maintains a cache of emitters per class name to ensure
 * consistent behavior and performance. Each emitter is associated with
 * a specific class name for proper logger identification.
 *
 * <p>The factory requires an {@link ObservabilityProcessor} to be set
 * before emitters can process events. This is typically done by framework
 * integrations during application startup.
 *
 * @since 2.0.0
 */
public class EmitterFactory {

    private static final Map<String, Emitter> emitterCache = new ConcurrentHashMap<>();
    static ObservabilityProcessor processor;

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private EmitterFactory() {}

    /**
     * Sets the observability processor for handling events.
     *
     * <p>This method is typically called automatically by framework integrations
     * during application startup. Manual configuration is usually not required.
     *
     * <p><strong>Thread Safety:</strong> This method is thread-safe and can be
     * called from multiple threads.
     *
     * @param processor the observability processor to use for event processing,
     *                  or null to disable event processing
     */
    public static void setProcessor(ObservabilityProcessor processor) {
        EmitterFactory.processor = processor;
    }

    /**
     * Gets an emitter for the specified class name.
     *
     * <p>Emitters are cached per class name to ensure consistent behavior
     * and performance. If no processor is configured, the emitter will
     * silently ignore all events.
     *
     * @param className the fully qualified class name for the emitter
     * @return an emitter instance for the specified class name, never null
     */
    public static Emitter getEmitter(String className) {
        return emitterCache.computeIfAbsent(className, name ->
            new DefaultEmitter(name, processor));
    }

    /**
     * Clears the emitter cache.
     *
     * <p>This method is primarily intended for testing purposes.
     * In production, emitters should be cached for the lifetime of the application.
     */
    static void clearCache() {
        emitterCache.clear();
    }
}
