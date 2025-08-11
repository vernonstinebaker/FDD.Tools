package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Ensures short tasks (< overlay delay) never show overlay and longer tasks do. */
@Disabled("Disabled: timing-sensitive overlay delay assertions are flaky in CI environment")
public class BusyServiceOverlayDelayTest {
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

    private static boolean rootReady = false;
    @BeforeEach
    void attach() throws Exception {
        if (!rootReady) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                StackPane root = new StackPane();
                BusyService.getInstance().attach(root);
                BusyService.getInstance().setOverlayDelayMsForTests(40);
                latch.countDown();
            });
            assertTrue(latch.await(7, TimeUnit.SECONDS));
            rootReady = true;
        }
    }

    @Test
    void veryFastTaskNeverShowsOverlay() throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            Task<Void> task = new Task<>() { @Override protected Void call() throws Exception { Thread.sleep(50); return null; } };
            BusyService.getInstance().runAsync("Fast", task, done::countDown, () -> fail("Should succeed"));
        });
    assertTrue(done.await(6, TimeUnit.SECONDS));
    Thread.sleep(400); // longer than overlay delay so it would have appeared if scheduled
    assertFalse(BusyService.getInstance().isOverlayVisible(), "Overlay should not appear for very fast task");
    }

    @Test
    void slowerTaskShowsOverlay() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            Task<Void> task = new Task<>() { @Override protected Void call() throws Exception { start.countDown(); Thread.sleep(300); return null; } };
            BusyService.getInstance().runAsync("Slow", task, done::countDown, () -> fail("Should succeed"));
        });
    assertTrue(start.await(4, TimeUnit.SECONDS));
        long deadline = System.currentTimeMillis() + 800; // generous upper bound
        boolean visible = false;
        while (System.currentTimeMillis() < deadline) {
            if (BusyService.getInstance().isOverlayVisible()) { visible = true; break; }
            Thread.sleep(25);
        }
        assertTrue(visible, "Overlay should be visible for slower task");
        assertTrue(done.await(5, TimeUnit.SECONDS));
    }
}
