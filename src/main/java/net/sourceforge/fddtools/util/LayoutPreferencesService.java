package net.sourceforge.fddtools.util;

import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Persists simple layout preferences (e.g., split pane divider positions) between sessions.
 */
public final class LayoutPreferencesService {
    private static final String NODE = "layout";
    private static final String KEY_MAIN_DIVIDER = "mainDivider"; // horizontal: tree vs canvas
    private static final String KEY_RIGHT_DIVIDER = "rightDivider"; // vertical: canvas vs info panels

    private static LayoutPreferencesService instance;
    private final Preferences prefs;

    private LayoutPreferencesService() {
        prefs = Preferences.userNodeForPackage(LayoutPreferencesService.class).node(NODE);
    }

    public static synchronized LayoutPreferencesService getInstance() {
        if (instance == null) {
            instance = new LayoutPreferencesService();
        }
        return instance;
    }

    public Optional<Double> getMainDividerPosition() {
        double val = prefs.getDouble(KEY_MAIN_DIVIDER, -1.0d);
        if (val < 0) return Optional.empty();
        return Optional.of(val);
    }

    public void setMainDividerPosition(double pos) {
        if (Double.isFinite(pos) && pos > 0.05 && pos < 0.95) {
            prefs.putDouble(KEY_MAIN_DIVIDER, pos);
        }
    }

    public Optional<Double> getRightDividerPosition() {
        double val = prefs.getDouble(KEY_RIGHT_DIVIDER, -1.0d);
        if (val < 0) return Optional.empty();
        return Optional.of(val);
    }

    public void setRightDividerPosition(double pos) {
        if (Double.isFinite(pos) && pos > 0.05 && pos < 0.95) {
            prefs.putDouble(KEY_RIGHT_DIVIDER, pos);
        }
    }
}
