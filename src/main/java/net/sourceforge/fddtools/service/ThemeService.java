package net.sourceforge.fddtools.service;

import javafx.application.Platform;

/** Theme management including semantic theme switching (system/light/dark/highcontrast). */
public final class ThemeService {
    private static final ThemeService INSTANCE = new ThemeService();
    public static ThemeService getInstance(){ return INSTANCE; }
    private ThemeService() {}

    public enum Theme { SYSTEM, LIGHT, DARK, HIGH_CONTRAST }

    public void applyTheme(Theme theme) {
        Platform.runLater(() -> {
            javafx.stage.Window w = javafx.stage.Window.getWindows().stream().filter(javafx.stage.Window::isShowing).findFirst().orElse(null);
            if (!(w instanceof javafx.stage.Stage stage)) return;
            var scene = stage.getScene(); if (scene==null) return;
            applyThemeTo(scene, theme);
        });
    }

    /** Direct scene application (synchronous) primarily for tests. */
    public void applyThemeTo(javafx.scene.Scene scene, Theme theme) {
        if (scene == null) return;
        // Ensure semantic base first
        var semantic = getClass().getResource("/styles/semantic-theme.css");
        if (semantic != null) {
            String u = semantic.toExternalForm();
            if (!scene.getStylesheets().contains(u)) scene.getStylesheets().add(0, u);
        }
        // Remove previous variants
        scene.getStylesheets().removeIf(s -> s.contains("global-theme-light.css") || s.contains("global-theme-dark.css") || s.contains("global-theme-highcontrast.css") || s.contains("global-theme.css"));
        switch (theme) {
            case LIGHT -> add(scene, "/styles/global-theme-light.css");
            case DARK -> add(scene, "/styles/global-theme-dark.css");
            case HIGH_CONTRAST -> add(scene, "/styles/global-theme-highcontrast.css");
            case SYSTEM -> add(scene, "/styles/global-theme.css");
        }
    }

    private void add(javafx.scene.Scene scene, String res){ try { var url=getClass().getResource(res); if(url!=null){ String u=url.toExternalForm(); if(!scene.getStylesheets().contains(u)) scene.getStylesheets().add(u);} } catch (Exception ignored) {}}
}
