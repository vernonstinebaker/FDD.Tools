package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Disabled due to JavaFX timing instability; overlay paths indirectly exercised via integration flows.")
class BusyServiceTest {
    private static boolean fxStarted = false;
    private static StackPane root;
    @BeforeAll
    static void initFx() throws Exception {
        if (!fxStarted) {
            CountDownLatch startup = new CountDownLatch(1);
            try { Platform.startup(startup::countDown); } catch (IllegalStateException already) { startup.countDown(); }
            startup.await();
            fxStarted = true;
            CountDownLatch attachLatch = new CountDownLatch(1);
            Platform.runLater(() -> {
                root = new StackPane();
                BusyService.getInstance().attach(root);
                BusyService.getInstance().setOverlayDelayMsForTests(30);
                attachLatch.countDown();
            });
            attachLatch.await(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void runAsyncSuccessTriggersCallback() throws Exception {
        CountDownLatch success = new CountDownLatch(1);
        Platform.runLater(() -> {
            Task<String> task = new Task<>() { @Override protected String call() throws Exception { Thread.sleep(50); return "ok"; } };
            BusyService.getInstance().runAsync("Test", task, success::countDown, () -> fail("Should not fail"));
        });
    assertTrue(success.await(6, TimeUnit.SECONDS), "Success callback should fire");
    }

    @Test
    void runAsyncFailureTriggersErrorCallback() throws Exception {
        CountDownLatch error = new CountDownLatch(1);
        Platform.runLater(() -> {
            Task<Void> task = new Task<>() { @Override protected Void call() { throw new RuntimeException("boom"); } };
            BusyService.getInstance().runAsync("Fail", task, () -> fail("Should not succeed"), error::countDown);
        });
    assertTrue(error.await(6, TimeUnit.SECONDS), "Error callback should fire");
    }

    @Test
    void overlayVisibilityAndMessageLifecycle() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            BusyService svc = BusyService.getInstance();
            Task<Void> task = new Task<>() { @Override protected Void call() throws Exception { startLatch.countDown(); Thread.sleep(300); return null; } };
            svc.runAsync("Loading", task, doneLatch::countDown, () -> fail("Should succeed"));
        });
        assertTrue(startLatch.await(1, TimeUnit.SECONDS));
        BusyService svc = BusyService.getInstance();
        long showDeadline = System.currentTimeMillis() + 800; // generous upper bound for visibility
        boolean becameVisible = false;
        while (System.currentTimeMillis() < showDeadline) {
            if (svc.isOverlayVisible()) { becameVisible = true; break; }
            Thread.sleep(25);
        }
        assertTrue(becameVisible, "Overlay should be visible during task");
        assertTrue(svc.getDisplayedMessage().startsWith("Loading"), "Message should reflect status");
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS));
        long hideDeadline = System.currentTimeMillis() + 400; // allow time to hide
        while (svc.isOverlayVisible() && System.currentTimeMillis() < hideDeadline) {
            Thread.sleep(20);
        }
        assertFalse(svc.isOverlayVisible(), "Overlay should hide after completion");
    }
}
