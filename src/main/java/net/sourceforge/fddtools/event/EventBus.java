package net.sourceforge.fddtools.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Lightweight synchronous event bus for internal model/UI decoupling (no external deps). */
public final class EventBus {
    private static final EventBus INSTANCE = new EventBus();
    public static EventBus getInstance() { return INSTANCE; }

    private final List<Consumer<ModelEvent>> listeners = new CopyOnWriteArrayList<>();

    private EventBus() {}

    public void publish(ModelEvent evt) {
        if (evt == null) return;
        for (Consumer<ModelEvent> l : listeners) {
            try { l.accept(evt); } catch (RuntimeException ignored) { }
        }
    }

    /** Register a listener; returns AutoCloseable to unsubscribe. */
    public AutoCloseable subscribe(Consumer<ModelEvent> l) {
        listeners.add(l);
        return () -> listeners.remove(l);
    }
}
