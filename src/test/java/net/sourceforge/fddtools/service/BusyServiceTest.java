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
}
