package net.sourceforge.fddtools.testutil;

import javafx.application.Platform;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Consolidated JavaFX testing utilities (startup + runOnFxAndWait with configurable timeout). */
public final class FxTestUtil {
    private static volatile boolean started = false;
    private FxTestUtil() {}

    public static void ensureStarted() {
        if (started) return;
        synchronized (FxTestUtil.class) {
            if (started) return;
            
            // Configure headless environment before starting JavaFX
            HeadlessTestUtil.configureHeadlessEnvironment();
            
            CountDownLatch latch = new CountDownLatch(1);
            try { 
                Platform.startup(latch::countDown); 
            } catch (IllegalStateException already) { 
                latch.countDown(); 
            }
            try { 
                latch.await(10, TimeUnit.SECONDS); 
            } catch (InterruptedException e) { 
                Thread.currentThread().interrupt(); 
            }
            started = true;
        }
    }

    public static void runOnFxAndWait(int timeoutSeconds, Runnable r) throws Exception {
        ensureStarted();
        if (Platform.isFxApplicationThread()) { r.run(); return; }
        AtomicReference<Throwable> fail = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { try { r.run(); } catch (Throwable t){ fail.set(t);} finally { latch.countDown(); }});
        boolean finished = latch.await(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) throw new IllegalStateException("FX task did not finish in "+timeoutSeconds+"s");
        if (fail.get()!=null) {
            if (fail.get() instanceof Exception e) throw e; else throw new RuntimeException(fail.get());
        }
    }
}
