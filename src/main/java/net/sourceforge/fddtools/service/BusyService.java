package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.sourceforge.fddtools.state.ModelState;
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
    private final Label messageLabel = new Label("Working...");
    private static final Logger LOGGER = LoggerFactory.getLogger(BusyService.class);
    private BusyService() {
        overlay.setPickOnBounds(true);
    ProgressIndicator pi = new ProgressIndicator();
    messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        Rectangle bg = new Rectangle();
        bg.setFill(Color.color(0,0,0,0.35));
        bg.widthProperty().bind(overlay.widthProperty());
        bg.heightProperty().bind(overlay.heightProperty());
    overlay.getChildren().addAll(bg, new StackPane(pi, messageLabel));
        overlay.setVisible(false);
    }

    public void attach(StackPane parent) { this.overlayParent = parent; if (!parent.getChildren().contains(overlay)) parent.getChildren().add(overlay); }

    /**
     * Runs a JavaFX Task on a background thread with overlay + MDC context.
     * MDC keys: action=async:<status>, projectPath (if open), selectedNode (if any)
     */
    public <T> void runAsync(String status, Task<T> task, Runnable onSuccess, Runnable onError) {
        Map<String,String> ctx = buildContext(status);
        if (overlayParent == null) {
            startThreadWithContext(task, ctx, status, onSuccess, onError);
            return;
        }
        Platform.runLater(() -> { messageLabel.setText(status + "..."); overlay.setVisible(true); });
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> overlay.setVisible(false));
            if (onSuccess!=null) onSuccess.run();
            logWithContext(ctx, () -> LOGGER.info("Async task succeeded: {}", status));
        });
        task.setOnFailed(e -> {
            Platform.runLater(() -> overlay.setVisible(false));
            if (onError!=null) onError.run();
            logWithContext(ctx, () -> LOGGER.error("Async task failed: {}", status, task.getException()));
        });
        startThreadWithContext(task, ctx, status, onSuccess, onError);
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
}
