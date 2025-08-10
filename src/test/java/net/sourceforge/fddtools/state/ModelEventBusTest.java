package net.sourceforge.fddtools.state;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ModelEventBusTest {
    @Test
    void publishesEventsToSubscribers() throws Exception {
        ModelEventBus bus = ModelEventBus.get();
        final boolean[] received = {false};
        try (AutoCloseable sub = bus.subscribe(ev -> { if(ev.type== ModelEventBus.EventType.NODE_UPDATED) received[0]=true; })) {
            bus.publish(ModelEventBus.EventType.NODE_UPDATED, "payload");
            assertTrue(received[0]);
        }
    }
}
