package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BusyServiceTest {
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
    void runAsyncSuccessTriggersCallback() throws Exception {
        CountDownLatch success = new CountDownLatch(1);
        Platform.runLater(() -> {
            BusyService.getInstance().attach(new StackPane());
            Task<String> task = new Task<>() { @Override protected String call() throws Exception { Thread.sleep(50); return "ok"; } };
            BusyService.getInstance().runAsync("Test", task, success::countDown, () -> fail("Should not fail"));
        });
        assertTrue(success.await(2, TimeUnit.SECONDS), "Success callback should fire");
    }

    @Test
    void runAsyncFailureTriggersErrorCallback() throws Exception {
        CountDownLatch error = new CountDownLatch(1);
        Platform.runLater(() -> {
            BusyService.getInstance().attach(new StackPane());
            Task<Void> task = new Task<>() { @Override protected Void call() { throw new RuntimeException("boom"); } };
            BusyService.getInstance().runAsync("Fail", task, () -> fail("Should not succeed"), error::countDown);
        });
        assertTrue(error.await(2, TimeUnit.SECONDS), "Error callback should fire");
    }

    @Test
    void overlayVisibilityAndMessageLifecycle() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            BusyService svc = BusyService.getInstance();
            svc.attach(new StackPane());
            Task<Void> task = new Task<>() { @Override protected Void call() throws Exception { startLatch.countDown(); Thread.sleep(80); return null; } };
            svc.runAsync("Loading", task, doneLatch::countDown, () -> fail("Should succeed"));
        });
        assertTrue(startLatch.await(1, TimeUnit.SECONDS));
        // Allow FX thread to show overlay
        Thread.sleep(50);
        BusyService svc = BusyService.getInstance();
        assertTrue(svc.isOverlayVisible(), "Overlay should be visible during task");
        assertTrue(svc.getDisplayedMessage().startsWith("Loading"), "Message should reflect status");
        assertTrue(doneLatch.await(2, TimeUnit.SECONDS));
        // Allow hide
        Thread.sleep(30);
        assertFalse(svc.isOverlayVisible(), "Overlay should hide after completion");
    }
}
