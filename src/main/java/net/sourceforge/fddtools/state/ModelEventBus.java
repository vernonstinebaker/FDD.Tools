package net.sourceforge.fddtools.state;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Lightweight observable model event bus. */
public final class ModelEventBus {
    public enum EventType { NODE_UPDATED, TREE_STRUCTURE_CHANGED, PROJECT_LOADED }
    public static final class Event {
        public final EventType type; public final Object payload;
        public Event(EventType type, Object payload){ this.type=type; this.payload=payload; }
    }
    private final List<Consumer<Event>> listeners = new CopyOnWriteArrayList<>();
    private static final ModelEventBus INSTANCE = new ModelEventBus();
    private ModelEventBus(){}
    public static ModelEventBus get(){ return INSTANCE; }
    public AutoCloseable subscribe(Consumer<Event> l){ listeners.add(l); return ()->listeners.remove(l); }
    public void publish(EventType type, Object payload){ for(var l: listeners){ try { l.accept(new Event(type,payload)); } catch(Exception ignored){} } }
}
