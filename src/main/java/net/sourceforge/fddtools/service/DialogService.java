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
    CheckBox autoLoad = new CheckBox("Auto-load last project");
    autoLoad.setSelected(prefs.isAutoLoadLastProjectEnabled());
    CheckBox restoreZoom = new CheckBox("Restore last zoom on open");
    restoreZoom.setSelected(prefs.isRestoreLastZoomEnabled());

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10,10,10,10));

        grid.add(recentLabel,0,0); grid.add(recentLimit,1,0);
        grid.add(languageLabel,0,1); grid.add(language,1,1);
    grid.add(themeLabel,0,2); grid.add(theme,1,2);
    grid.add(autoLoad,0,3,2,1);
    grid.add(restoreZoom,0,4,2,1);

        // spacing filler
        Region spacer = new Region();
        GridPane.setHgrow(spacer, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);
        if (owner != null) dialog.initOwner(owner);

        // Live apply listeners (non-persistent until OK)
        final String originalTheme = th;
        final String originalLang = lang;
    theme.valueProperty().addListener((obs,ov,nv)-> { applyThemePreview(nv); net.sourceforge.fddtools.state.ModelEventBus.get().publish(net.sourceforge.fddtools.state.ModelEventBus.EventType.UI_THEME_CHANGED, nv); });
    language.valueProperty().addListener((obs,ov,nv)-> { applyLanguagePreview(nv); net.sourceforge.fddtools.state.ModelEventBus.get().publish(net.sourceforge.fddtools.state.ModelEventBus.EventType.UI_LANGUAGE_CHANGED, nv); });

        dialog.setResultConverter(bt -> bt);
        ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
        if (result == ButtonType.OK) {
            try {
                Integer val = recentLimit.getValue();
                if (val != null) prefs.setRecentFilesLimit(val);
                String selectedLang = language.getSelectionModel().getSelectedItem();
                if ("system".equals(selectedLang)) selectedLang = null;
                if (selectedLang != null) prefs.setUiLanguage(selectedLang); else prefs.updateAndFlush(net.sourceforge.fddtools.util.PreferencesService.KEY_UI_LANGUAGE, null);
                net.sourceforge.fddtools.state.ModelEventBus.get().publish(net.sourceforge.fddtools.state.ModelEventBus.EventType.UI_LANGUAGE_CHANGED, selectedLang);
                String selectedTheme = theme.getSelectionModel().getSelectedItem();
                if ("system".equals(selectedTheme)) selectedTheme = null;
                if (selectedTheme != null) prefs.setTheme(selectedTheme); else prefs.updateAndFlush(net.sourceforge.fddtools.util.PreferencesService.KEY_THEME, null);
                net.sourceforge.fddtools.state.ModelEventBus.get().publish(net.sourceforge.fddtools.state.ModelEventBus.EventType.UI_THEME_CHANGED, selectedTheme);
                prefs.setAutoLoadLastProjectEnabled(autoLoad.isSelected());
                prefs.setRestoreLastZoomEnabled(restoreZoom.isSelected());
                prefs.flushNow();
                net.sourceforge.fddtools.util.RecentFilesService.getInstance().pruneToLimit();
            } catch (Exception ignored) { }
        } else {
            // Revert previews
            applyThemePreview(originalTheme);
            applyLanguagePreview(originalLang);
        }
    }

    /** Apply theme preview by swapping a high-level stylesheet on the primary stage scene. */
    private void applyThemePreview(String theme) {
        Platform.runLater(() -> {
            try {
                String normalized = (theme==null||theme.isBlank()||"system".equals(theme))?"system":theme;
                // Acquire primary stage via any showing window
                javafx.stage.Window w = javafx.stage.Window.getWindows().stream().filter(javafx.stage.Window::isShowing).findFirst().orElse(null);
                if (w instanceof javafx.stage.Stage stage) {
                    var scene = stage.getScene();
                    if (scene != null) {
                        scene.getStylesheets().removeIf(s-> s.contains("global-theme-light.css") || s.contains("global-theme-dark.css"));
                        if ("light".equalsIgnoreCase(normalized)) {
                            addStylesheet(scene, "/styles/global-theme-light.css");
                        } else if ("dark".equalsIgnoreCase(normalized)) {
                            addStylesheet(scene, "/styles/global-theme-dark.css");
                        } else {
                            // system -> rely on default + base global-theme.css
                            addStylesheet(scene, "/styles/global-theme.css");
                        }
                    }
                }
            } catch (Exception ignored) { }
        });
    }

    private void addStylesheet(javafx.scene.Scene scene, String resource) {
        try { var url = getClass().getResource(resource); if (url!=null) { String u = url.toExternalForm(); if(!scene.getStylesheets().contains(u)) scene.getStylesheets().add(u);} } catch (Exception ignored) {}
    }

    /** Trigger lightweight language preview by reloading resource bundle (static). */
    private void applyLanguagePreview(String lang) {
        // For now, simply reload Messages bundle; full UI relabel would need observers.
        try {
            String base = "messages";
            java.util.Locale locale = (lang==null||lang.isBlank()||"system".equals(lang)) ? java.util.Locale.getDefault() : java.util.Locale.forLanguageTag(lang);
            java.util.ResourceBundle.clearCache();
            java.util.ResourceBundle.getBundle(base, locale);
            // Future: publish an event for UI components to refresh labels.
        } catch (Exception ignored) { }
    }
}
