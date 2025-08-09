package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Ensures overlay hides after a failing async task. */
public class BusyServiceFailureOverlayTest {
    private static boolean fxStarted = false;
    @BeforeAll
    static void initFx() throws Exception {
        if (!fxStarted) {
            CountDownLatch latch = new CountDownLatch(1);
            try { Platform.startup(latch::countDown); } catch (IllegalStateException already) { latch.countDown(); }
            latch.await();
            fxStarted = true;
        }
    }

    @Test
    void overlayHidesAfterFailure() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch fail = new CountDownLatch(1);
        Platform.runLater(() -> {
            BusyService svc = BusyService.getInstance();
            svc.attach(new StackPane());
            Task<Void> task = new Task<>() { @Override protected Void call() { start.countDown(); throw new RuntimeException("boom"); } };
            svc.runAsync("WillFail", task, () -> fail.countDown(), fail::countDown);
        });
        assertTrue(start.await(1, TimeUnit.SECONDS));
        BusyService svc = BusyService.getInstance();
        // Optional: poll briefly to allow potential visibility before failure completes.
        long deadline = System.currentTimeMillis() + 200;
        while (!svc.isOverlayVisible() && System.currentTimeMillis() < deadline && fail.getCount() > 0) {
            Thread.sleep(10);
        }
        assertTrue(fail.await(2, TimeUnit.SECONDS));
        // Allow hide to process
        Thread.sleep(50);
        assertFalse(svc.isOverlayVisible(), "Overlay should be hidden after failure");
    }
}
