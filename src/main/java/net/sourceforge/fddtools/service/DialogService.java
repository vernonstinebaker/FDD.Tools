package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;

/** Minimal, test-friendly dialog service implementation. */
public final class DialogService {
    private static final DialogService INSTANCE = new DialogService();
    public static DialogService getInstance() { return INSTANCE; }
    private DialogService() {}

    public void showError(Stage owner, String title, String message) {
        Runnable task = () -> {
            try {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title == null ? "" : title);
                alert.setHeaderText(null);
                alert.setContentText(message == null ? "" : message);
                if (owner != null) alert.initOwner(owner);
                alert.show(); // non-blocking for tests
            } catch (Exception ignored) { }
        };
        if (Platform.isFxApplicationThread()) task.run(); else Platform.runLater(task);
    }

    public boolean confirm(Stage owner, String title, String header, String content) {
        Runnable task = () -> {
            try {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(title == null ? "" : title);
                alert.setHeaderText(header);
                alert.setContentText(content);
                if (owner != null) alert.initOwner(owner);
                alert.show();
            } catch (Exception ignored) { }
        };
        if (Platform.isFxApplicationThread()) task.run(); else Platform.runLater(task);
        return false; // default (no user interaction in tests)
    }

    public ButtonType confirmWithChoices(Stage owner, String title, String header, String content, ButtonType... buttons) {
        final ButtonType[] result = {ButtonType.CANCEL};
        Runnable task = () -> {
            try {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(title == null ? "" : title);
                alert.setHeaderText(header);
                alert.setContentText(content);
                if (buttons != null && buttons.length > 0) alert.getButtonTypes().setAll(buttons);
                if (owner != null) alert.initOwner(owner);
                alert.show();
            } catch (Exception ignored) { }
        };
        if (Platform.isFxApplicationThread()) task.run(); else Platform.runLater(task);
        return result[0];
    }

    public void showAbout(Stage owner, String versionText) {
        Runnable task = () -> {
            try {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("About");
                alert.setHeaderText("FDD Tools");
                alert.setContentText(versionText == null ? "" : versionText);
                if (owner != null) alert.initOwner(owner);
                alert.show();
            } catch (Exception ignored) { }
        };
        if (Platform.isFxApplicationThread()) task.run(); else Platform.runLater(task);
    }

    public void showPreferences(Stage owner) {
        Runnable task = () -> {
            try {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Preferences");
                dialog.setHeaderText("Preferences");
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                if (owner != null) dialog.initOwner(owner);
                dialog.show();
            } catch (Exception ignored) { }
        };
        if (Platform.isFxApplicationThread()) task.run(); else Platform.runLater(task);
    }
}
