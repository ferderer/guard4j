package de.ferderer.guard4j.observability;

import de.ferderer.guard4j.classification.Level;

/**
 * Base record for business events that capture domain-specific operations and user actions.
 * 
 * <p>Business events get their configuration from an {@link EventConfig} and can be extended
 * by domain-specific records that add their own fields while passing the appropriate config
 * to this base record.
 *
 * @param config the event configuration defining type, level, and metrics behavior
 * 
 * @since 1.0.0
 */
public record BusinessEvent(EventConfig config) implements ObservableEvent {
    
    @Override 
    public String eventType() { 
        return config().eventType(); 
    }
    
    @Override 
    public Level level() { 
        return config().level(); 
    }
    
    @Override 
    public boolean hasMetrics() { 
        return config().hasMetrics(); 
    }
}
