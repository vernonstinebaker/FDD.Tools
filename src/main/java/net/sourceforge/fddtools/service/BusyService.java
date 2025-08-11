package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Button;
import javafx.scene.shape.Rectangle;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.util.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/** Simple busy overlay manager for long-running IO tasks. */
public final class BusyService {
    private static final BusyService INSTANCE = new BusyService();
    public static BusyService getInstance() { return INSTANCE; }
    private StackPane overlayParent;
    private final StackPane overlay = new StackPane();
    private final Label messageLabel = new Label(I18n.get("BusyOverlay.Working"));
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final Button cancelButton = new Button(I18n.get("BusyOverlay.Cancel"));
    private Task<?> currentTask; // track current running task (single-task model)
    private static final Logger LOGGER = LoggerFactory.getLogger(BusyService.class);
    // Configurable delay before showing overlay (tweakable for tests)
    private long overlayDelayMs = 180;
    private BusyService() {
        overlay.setPickOnBounds(true);
    messageLabel.getStyleClass().addAll("text-inverse","busy-overlay-message");
        progressIndicator.setMaxSize(80,80);
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        cancelButton.setVisible(false);
        cancelButton.setManaged(false);
        cancelButton.setOnAction(e -> {
            Task<?> task = currentTask;
            if (task != null && task.isRunning()) {
                task.cancel(true);
                logWithContext(buildContext("cancel"), () -> LOGGER.info("User requested task cancel"));
            }
        });
    Rectangle bg = new Rectangle();
    bg.getStyleClass().add("overlay-dim");
        bg.widthProperty().bind(overlay.widthProperty());
        bg.heightProperty().bind(overlay.heightProperty());
        VBox content = new VBox(8, progressIndicator, messageLabel, cancelButton);
    content.getStyleClass().add("busy-overlay-content");
        overlay.getChildren().addAll(bg, content);
        overlay.setVisible(false);
    }

    public void attach(StackPane parent) { this.overlayParent = parent; if (!parent.getChildren().contains(overlay)) parent.getChildren().add(overlay); }

    /**
     * Runs a JavaFX Task on a background thread with overlay + MDC context.
     * MDC keys: action=async:<status>, projectPath (if open), selectedNode (if any)
     */
    public <T> void runAsync(String status, Task<T> task, Runnable onSuccess, Runnable onError) {
        // Backwards-compatible default: no explicit progress binding, not cancellable
        runAsync(status, task, false, false, onSuccess, onError);
    }

    /**
     * Enhanced async runner with optional progress binding & cancel support.
     * If showProgress is true, binds overlay progress + message to task properties.
     */
    public <T> void runAsync(String status, Task<T> task, boolean showProgress, boolean cancellable,
                             Runnable onSuccess, Runnable onError) {
        Map<String,String> ctx = buildContext(status);
        if (overlayParent == null) {
            startThreadWithContext(task, ctx, status, onSuccess, onError);
            return;
        }
        currentTask = task;
        LoggingService ls = LoggingService.getInstance();
        LoggingService.Span span = ls.startPerf("async:"+status, ctx);
        // Delay showing overlay to prevent flicker for very fast tasks
    PauseTransition showDelay = new PauseTransition(Duration.millis(overlayDelayMs));
        showDelay.setOnFinished(e -> {
            if (task != currentTask || task.isDone()) return; // finished before delay elapsed
            overlay.setVisible(true);
            messageLabel.setText(status + "...");
            cancelButton.setVisible(cancellable);
            cancelButton.setManaged(cancellable);
            if (showProgress) {
                progressIndicator.progressProperty().bind(task.progressProperty());
                messageLabel.textProperty().bind(task.messageProperty());
            } else {
                progressIndicator.progressProperty().unbind();
                progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                messageLabel.textProperty().unbind();
            }
        });
        Platform.runLater(showDelay::play);
    task.setOnSucceeded(e -> {
            showDelay.stop();
            Platform.runLater(() -> {
                if (overlay.isVisible()) {
                    cleanupBindings();
                    overlay.setVisible(false);
                }
        if (onSuccess!=null) onSuccess.run();
            });
            logWithContext(ctx, () -> LOGGER.info("Async task succeeded: {}", status));
            ls.audit("asyncSuccess", ctx, () -> status);
            span.metric("result","success").close();
        });
    task.setOnFailed(e -> {
            showDelay.stop();
            Platform.runLater(() -> {
                if (overlay.isVisible()) {
                    cleanupBindings();
                    overlay.setVisible(false);
                }
        if (onError!=null) onError.run();
            });
            logWithContext(ctx, () -> LOGGER.error("Async task failed: {}", status, task.getException()));
            ls.audit("asyncFailure", ctx, () -> status+": "+task.getException().getClass().getSimpleName());
            span.metric("result","failed").close();
        });
        task.setOnCancelled(e -> {
            showDelay.stop();
            Platform.runLater(() -> {
                if (overlay.isVisible()) {
                    cleanupBindings();
                    overlay.setVisible(false);
                }
        if (onError!=null) onError.run(); // treat cancel as error callback for test harness symmetry
            });
            logWithContext(ctx, () -> LOGGER.info("Async task cancelled: {}", status));
            ls.audit("asyncCancelled", ctx, () -> status);
            span.metric("result","cancelled").close();
        });
        startThreadWithContext(task, ctx, status, onSuccess, onError);
    }

    private void cleanupBindings() {
        if (messageLabel.textProperty().isBound()) messageLabel.textProperty().unbind();
        if (progressIndicator.progressProperty().isBound()) progressIndicator.progressProperty().unbind();
        cancelButton.setVisible(false); cancelButton.setManaged(false);
        currentTask = null;
    }

    private <T> void startThreadWithContext(Task<T> task, Map<String,String> ctx, String status, Runnable onSuccess, Runnable onError) {
        Thread t = new Thread(() -> logWithContext(ctx, () -> {
            LOGGER.info("Async task started: {}", status);
            try {
                task.run();
            } catch (Throwable t1) { LOGGER.error("Async task execution error: {}", status, t1); throw t1; }
        }), "busy-task");
        t.start();
    }

    private Map<String,String> buildContext(String status) {
        Map<String,String> ctx = new HashMap<>();
        ctx.put("action", "async:" + status);
        String path = ProjectService.getInstance().getAbsolutePath();
        if (path != null) ctx.put("projectPath", path);
        if (ModelState.getInstance().getSelectedNode() != null) {
            ctx.put("selectedNode", ModelState.getInstance().getSelectedNode().getName());
        }
        return ctx;
    }

    private void logWithContext(Map<String,String> ctx, Runnable r) {
        LoggingService.getInstance().withContext(ctx, r);
    }

    // Testing helpers (package-private)
    boolean isOverlayVisible() { return overlay.isVisible(); }
    String getDisplayedMessage() { return messageLabel.getText(); }
    /** Test hook: adjust overlay delay (milliseconds). */
    void setOverlayDelayMsForTests(long ms) { this.overlayDelayMs = ms; }
}
