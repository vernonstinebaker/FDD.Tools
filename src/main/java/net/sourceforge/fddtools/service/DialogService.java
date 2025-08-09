package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

    public void showAbout(Window owner, String versionText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About FDD Tools");
        alert.setHeaderText("FDD Tools");
        alert.setContentText("Version: " + (versionText == null ? "(dev)" : versionText) + "\nFeature-Driven Development visualization and management.");
        if (owner != null) alert.initOwner(owner);
        alert.showAndWait();
    }

    public void showPreferences(Window owner) {
        // Build lightweight preferences panel (no i18n yet)
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Preferences");
        dialog.setHeaderText("Application Preferences");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Controls
        net.sourceforge.fddtools.util.PreferencesService prefs = net.sourceforge.fddtools.util.PreferencesService.getInstance();

        Spinner<Integer> recentLimit = new Spinner<>(1, 50, prefs.getRecentFilesLimit());
        recentLimit.setEditable(true);

        ComboBox<String> language = new ComboBox<>();
        language.getItems().addAll("system","en","es","ja","zh");
        String lang = prefs.getUiLanguage();
        if (lang == null || lang.isBlank()) lang = "system"; // system default
        language.getSelectionModel().select(lang);
        language.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> theme = new ComboBox<>();
        theme.getItems().addAll("system","light","dark");
        String th = prefs.getTheme();
        if (th == null || th.isBlank()) th = "system";
        theme.getSelectionModel().select(th);
        theme.setMaxWidth(Double.MAX_VALUE);

        Label recentLabel = new Label("Recent Files Limit:");
        Label languageLabel = new Label("Language:");
        Label themeLabel = new Label("Theme:");

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10,10,10,10));

        grid.add(recentLabel,0,0); grid.add(recentLimit,1,0);
        grid.add(languageLabel,0,1); grid.add(language,1,1);
        grid.add(themeLabel,0,2); grid.add(theme,1,2);

        // spacing filler
        Region spacer = new Region();
        GridPane.setHgrow(spacer, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);
        if (owner != null) dialog.initOwner(owner);

        dialog.setResultConverter(bt -> bt);
        ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
        if (result == ButtonType.OK) {
            // Persist changes
            try {
                Integer val = recentLimit.getValue();
                if (val != null) prefs.setRecentFilesLimit(val);
                String selectedLang = language.getSelectionModel().getSelectedItem();
                if ("system".equals(selectedLang)) selectedLang = null; // remove explicit override
                if (selectedLang != null) prefs.setUiLanguage(selectedLang);
                String selectedTheme = theme.getSelectionModel().getSelectedItem();
                if ("system".equals(selectedTheme)) selectedTheme = null;
                if (selectedTheme != null) prefs.setTheme(selectedTheme);
                prefs.flushNow();
                // Apply MRU pruning immediately
                net.sourceforge.fddtools.util.RecentFilesService.getInstance().pruneToLimit();
                // Theme & language live-application are future enhancements (requires stylesheet + resource reload)
            } catch (Exception ignored) { }
        }
    }
}
