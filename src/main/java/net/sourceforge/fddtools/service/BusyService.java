package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/** Simple busy overlay manager for long-running IO tasks. */
public final class BusyService {
    private static final BusyService INSTANCE = new BusyService();
    public static BusyService getInstance() { return INSTANCE; }
    private StackPane overlayParent;
    private final StackPane overlay = new StackPane();
    private BusyService() {
        overlay.setPickOnBounds(true);
        ProgressIndicator pi = new ProgressIndicator();
        Label lbl = new Label("Working...");
        lbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        Rectangle bg = new Rectangle();
        bg.setFill(Color.color(0,0,0,0.35));
        bg.widthProperty().bind(overlay.widthProperty());
        bg.heightProperty().bind(overlay.heightProperty());
        overlay.getChildren().addAll(bg, new StackPane(pi, lbl));
        overlay.setVisible(false);
    }

    public void attach(StackPane parent) { this.overlayParent = parent; if (!parent.getChildren().contains(overlay)) parent.getChildren().add(overlay); }

    public <T> void runAsync(String status, Task<T> task, Runnable onSuccess, Runnable onError) {
        if (overlayParent == null) { new Thread(task).start(); return; }
        Platform.runLater(() -> overlay.setVisible(true));
        task.setOnSucceeded(e -> { Platform.runLater(() -> overlay.setVisible(false)); if (onSuccess!=null) onSuccess.run(); });
        task.setOnFailed(e -> { Platform.runLater(() -> overlay.setVisible(false)); if (onError!=null) onError.run(); });
        new Thread(task, "busy-task").start();
    }
}
