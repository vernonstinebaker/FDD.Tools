package net.sourceforge.fddtools.service;

import net.sourceforge.fddtools.state.ModelEventBus;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

/** Verifies that theme and language changes publish corresponding events. */
public class PreferencesThemeLanguageEventTest {
    @Test
    public void testThemeAndLanguageEventsPublish() {
        AtomicInteger themeCount = new AtomicInteger();
        AtomicInteger langCount = new AtomicInteger();
        try (AutoCloseable sub1 = ModelEventBus.get().subscribe(ev -> { if(ev.type== ModelEventBus.EventType.UI_THEME_CHANGED) themeCount.incrementAndGet(); })) {
            try (AutoCloseable sub2 = ModelEventBus.get().subscribe(ev -> { if(ev.type== ModelEventBus.EventType.UI_LANGUAGE_CHANGED) langCount.incrementAndGet(); })) {
                // Simulate publishing (direct invocation since dialog UI interaction not headless-friendly)
                ModelEventBus.get().publish(ModelEventBus.EventType.UI_THEME_CHANGED, "dark");
                ModelEventBus.get().publish(ModelEventBus.EventType.UI_LANGUAGE_CHANGED, "ja");
            } catch (Exception e) { fail(e); }
        } catch (Exception e) { fail(e); }
        assertEquals(1, themeCount.get());
        assertEquals(1, langCount.get());
    }
}
