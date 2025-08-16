package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import java.util.Optional;

import net.sourceforge.fddtools.internationalization.I18n; // added

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
        alert.setTitle(I18n.get("AboutDialog.Title"));
    alert.setHeaderText("FDD Tools"); // App name can be externalized later via I18n key
        alert.setContentText(I18n.get("AboutDialog.Title") + "\n" + I18n.get("App.Description"));
        if (owner != null) alert.initOwner(owner);
        alert.showAndWait();
    }

    public void showPreferences(Window owner) {
        // Build lightweight preferences panel (no i18n yet)
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(I18n.get("Preferences.Dialog.Title"));
        dialog.setHeaderText(I18n.get("Preferences.Dialog.Header"));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Controls
        net.sourceforge.fddtools.service.PreferencesService prefs = net.sourceforge.fddtools.service.PreferencesService.getInstance();

        Spinner<Integer> recentLimit = new Spinner<>(1, 50, prefs.getRecentFilesLimit());
        recentLimit.setEditable(true);

        ComboBox<String> language = new ComboBox<>();
        language.getItems().addAll("system","en","es","ja","zh");
        String lang = prefs.getUiLanguage();
        if (lang == null || lang.isBlank()) lang = "system"; // system default
        language.getSelectionModel().select(lang);
        language.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> theme = new ComboBox<>();
    theme.getItems().addAll("system","light","dark","highcontrast");
        String th = prefs.getTheme();
        if (th == null || th.isBlank()) th = "system";
        theme.getSelectionModel().select(th);
        theme.setMaxWidth(Double.MAX_VALUE);

    Label recentLabel = new Label(I18n.get("Preferences.RecentFilesLimit.Label"));
    Label languageLabel = new Label(I18n.get("Preferences.Language.Label"));
    Label themeLabel = new Label(I18n.get("Preferences.Theme.Label"));
    CheckBox autoLoad = new CheckBox(I18n.get("Preferences.AutoLoadLastProject.Label"));
    autoLoad.setSelected(prefs.isAutoLoadLastProjectEnabled());
    CheckBox restoreZoom = new CheckBox(I18n.get("Preferences.RestoreZoom.Label"));
    restoreZoom.setSelected(prefs.isRestoreLastZoomEnabled());

        CheckBox auditLogging = new CheckBox("Audit Logging");
        auditLogging.setSelected(prefs.isAuditLoggingEnabled());
        CheckBox perfLogging = new CheckBox("Performance Logging");
        perfLogging.setSelected(prefs.isPerfLoggingEnabled());

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10,10,10,10));

        grid.add(recentLabel,0,0); grid.add(recentLimit,1,0);
        grid.add(languageLabel,0,1); grid.add(language,1,1);
    grid.add(themeLabel,0,2); grid.add(theme,1,2);
    grid.add(autoLoad,0,3,2,1);
    grid.add(restoreZoom,0,4,2,1);
        grid.add(auditLogging,0,5,2,1);
        grid.add(perfLogging,0,6,2,1);

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
                if (selectedLang != null) prefs.setUiLanguage(selectedLang); else prefs.updateAndFlush(net.sourceforge.fddtools.service.PreferencesService.KEY_UI_LANGUAGE, null);
                net.sourceforge.fddtools.state.ModelEventBus.get().publish(net.sourceforge.fddtools.state.ModelEventBus.EventType.UI_LANGUAGE_CHANGED, selectedLang);
                String selectedTheme = theme.getSelectionModel().getSelectedItem();
                if ("system".equals(selectedTheme)) selectedTheme = null;
                if (selectedTheme != null) prefs.setTheme(selectedTheme); else prefs.updateAndFlush(net.sourceforge.fddtools.service.PreferencesService.KEY_THEME, null);
                net.sourceforge.fddtools.state.ModelEventBus.get().publish(net.sourceforge.fddtools.state.ModelEventBus.EventType.UI_THEME_CHANGED, selectedTheme);
                prefs.setAutoLoadLastProjectEnabled(autoLoad.isSelected());
                prefs.setRestoreLastZoomEnabled(restoreZoom.isSelected());
            prefs.setAuditLoggingEnabled(auditLogging.isSelected());
            prefs.setPerfLoggingEnabled(perfLogging.isSelected());
            // Apply immediately to runtime service
            var loggingSvc = net.sourceforge.fddtools.service.LoggingService.getInstance();
            loggingSvc.setAuditEnabled(auditLogging.isSelected());
            loggingSvc.setPerfEnabled(perfLogging.isSelected());
                prefs.flushNow();
                PreferencesService.getInstance().pruneRecentFilesToLimit();
            } catch (Exception ignored) { }
        } else {
            // Revert previews
            applyThemePreview(originalTheme);
            applyLanguagePreview(originalLang);
        }
    }

    /** Apply theme preview by swapping a high-level stylesheet on the primary stage scene. */
    private void applyThemePreview(String theme) {
        var svc = ThemeService.getInstance();
        ThemeService.Theme t = ThemeService.Theme.SYSTEM;
        if ("light".equalsIgnoreCase(theme)) t = ThemeService.Theme.LIGHT; else if ("dark".equalsIgnoreCase(theme)) t = ThemeService.Theme.DARK; else if ("highcontrast".equalsIgnoreCase(theme)) t = ThemeService.Theme.HIGH_CONTRAST;
        svc.applyTheme(t);
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
