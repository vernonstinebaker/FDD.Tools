package net.sourceforge.fddtools.util;

import net.sourceforge.fddtools.state.ModelEventBus;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Lightweight i18n helper centralizing ResourceBundle lookup and dynamic reload.
 * New JavaFX UI code should use I18n.get(key) instead of hard-coded literals.
 * Reload is triggered automatically when a UI_LANGUAGE_CHANGED event is published
 * (payload is a BCP-47 language tag or null for system default).
 */
public final class I18n {
    private static volatile ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
    private static volatile String currentTag = null; // null => system default

    static {
        // Subscribe to language change events
        ModelEventBus.get().subscribe(ev -> {
            if (ev.type == ModelEventBus.EventType.UI_LANGUAGE_CHANGED) {
                String tag = ev.payload instanceof String ? (String) ev.payload : null;
                reload(tag);
            }
        });
    }

    private I18n() {}

    /** Reload bundle for the specified language tag (null => system default). */
    public static synchronized void reload(String languageTag) {
        try {
            Locale locale = (languageTag == null || languageTag.isBlank()) ? Locale.getDefault() : Locale.forLanguageTag(languageTag);
            currentTag = (languageTag == null || languageTag.isBlank()) ? null : languageTag;
            ResourceBundle.clearCache();
            bundle = ResourceBundle.getBundle("messages", locale);
        } catch (Exception ignored) { /* keep previous bundle */ }
    }

    /** Current language tag in use (null means system default). */
    public static String getCurrentTag() { return currentTag; }

    /** Retrieve localized string; falls back to the key itself if missing. */
    public static String get(String key) {
        if (key == null) return "";
        try { return bundle.getString(key); } catch (MissingResourceException e) { return key; }
    }
}
