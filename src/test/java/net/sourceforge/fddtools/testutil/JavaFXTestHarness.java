package net.sourceforge.fddtools.testutil;

import javafx.application.Platform;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Shared, headless JavaFX Platform initialization for tests.
 */
public final class JavaFXTestHarness {
    private static volatile boolean started = false;

    private JavaFXTestHarness() {}

    /** Initialize JavaFX platform (idempotent). */
    public static void init() {
        if (started) return;
        synchronized (JavaFXTestHarness.class) {
            if (started) return;
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(latch::countDown);
            } catch (IllegalStateException already) {
                latch.countDown(); // already started
            }
            try { latch.await(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            started = true;
        }
    }

    /** Execute runnable on FX thread synchronously (best-effort). */
    public static void runAndWait(Runnable r) {
        if (Platform.isFxApplicationThread()) { r.run(); return; }
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { try { r.run(); } finally { latch.countDown(); } });
        try { latch.await(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
