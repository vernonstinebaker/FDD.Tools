package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Centralized dialog helper to decouple raw Alert usage from UI classes.
 * Future: i18n, theming, queued dialogs, logging hooks.
 */
public final class DialogService {
    private static final DialogService INSTANCE = new DialogService();
    public static DialogService getInstance() { return INSTANCE; }
    private DialogService() {}

    public void showError(Window owner, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            if (owner != null) alert.initOwner(owner);
            alert.showAndWait();
        });
    }

    public boolean confirm(Window owner, String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (owner != null) alert.initOwner(owner);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public ButtonType confirmWithChoices(Window owner, String title, String header, String content, ButtonType... buttons) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getButtonTypes().setAll(buttons);
        if (owner != null) alert.initOwner(owner);
        Optional<ButtonType> result = alert.showAndWait();
        return result.orElse(ButtonType.CANCEL);
    }
}
