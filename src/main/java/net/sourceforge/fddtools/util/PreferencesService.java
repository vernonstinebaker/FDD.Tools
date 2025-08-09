package net.sourceforge.fddtools.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Lightweight application preferences backed by a simple properties file on disk.
 * This intentionally avoids overloading Java Preferences for settings that users
 * may wish to migrate manually (e.g., copying config file). Thread-safe (RW lock).
 */
public final class PreferencesService {
    private static final String FILE_NAME = "fddtools.properties";
    private static final PreferencesService INSTANCE = new PreferencesService();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Properties props = new Properties();
    private final Path storePath;

    // Keys
    public static final String KEY_RECENTS_LIMIT = "recentFiles.limit";
    public static final String KEY_UI_LANGUAGE = "ui.language";
    public static final String KEY_THEME = "ui.theme"; // placeholder (light|dark|system)
    public static final String KEY_LAST_WINDOW_X = "window.last.x";
    public static final String KEY_LAST_WINDOW_Y = "window.last.y";
    public static final String KEY_LAST_WINDOW_W = "window.last.w";
    public static final String KEY_LAST_WINDOW_H = "window.last.h";

    // Defaults
    private static final int DEFAULT_RECENTS_LIMIT = 10;

    private PreferencesService() {
        // Store in user home directory under .fddtools
        Path dir = Path.of(System.getProperty("user.home"), ".fddtools");
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) { }
        storePath = dir.resolve(FILE_NAME);
        load();
    }

    public static PreferencesService getInstance() { return INSTANCE; }

    private void load() {
        lock.writeLock().lock();
        try {
            props.clear();
            if (Files.isRegularFile(storePath)) {
                try (InputStream in = Files.newInputStream(storePath, StandardOpenOption.READ)) {
                    props.load(in);
                }
            }
            // Ensure defaults present (don't persist until save explicitly requested)
            props.putIfAbsent(KEY_RECENTS_LIMIT, String.valueOf(DEFAULT_RECENTS_LIMIT));
        } catch (IOException ignored) { }
        finally { lock.writeLock().unlock(); }
    }

    public void save() {
        lock.readLock().lock();
        try (OutputStream out = Files.newOutputStream(storePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            props.store(out, "FDD Tools Preferences");
        } catch (IOException ignored) { }
        finally { lock.readLock().unlock(); }
    }

    private String get(String key) {
        lock.readLock().lock();
        try { return props.getProperty(key); } finally { lock.readLock().unlock(); }
    }

    private void set(String key, String value) {
        lock.writeLock().lock();
        try {
            if (value == null) props.remove(key); else props.setProperty(key, value);
        } finally { lock.writeLock().unlock(); }
    }

    public int getRecentFilesLimit() {
        try { return Integer.parseInt(get(KEY_RECENTS_LIMIT)); } catch (Exception e) { return DEFAULT_RECENTS_LIMIT; }
    }

    public void setRecentFilesLimit(int limit) {
        if (limit < 1) limit = 1; else if (limit > 50) limit = 50; // clamp
        set(KEY_RECENTS_LIMIT, String.valueOf(limit));
    }

    public String getUiLanguage() { return get(KEY_UI_LANGUAGE); }
    public void setUiLanguage(String lang) { if (lang != null && !lang.isBlank()) set(KEY_UI_LANGUAGE, lang); }

    public String getTheme() { return get(KEY_THEME); }
    public void setTheme(String theme) { if (theme != null) set(KEY_THEME, theme); }

    public void setLastWindowBounds(double x, double y, double w, double h) {
        set(KEY_LAST_WINDOW_X, String.valueOf((int)x));
        set(KEY_LAST_WINDOW_Y, String.valueOf((int)y));
        set(KEY_LAST_WINDOW_W, String.valueOf((int)w));
        set(KEY_LAST_WINDOW_H, String.valueOf((int)h));
    }

    public java.util.Optional<java.awt.Rectangle> getLastWindowBounds() {
        try {
            int x = Integer.parseInt(get(KEY_LAST_WINDOW_X));
            int y = Integer.parseInt(get(KEY_LAST_WINDOW_Y));
            int w = Integer.parseInt(get(KEY_LAST_WINDOW_W));
            int h = Integer.parseInt(get(KEY_LAST_WINDOW_H));
            if (w > 0 && h > 0) return java.util.Optional.of(new java.awt.Rectangle(x,y,w,h));
        } catch (Exception ignored) { }
        return java.util.Optional.empty();
    }

    /** Persist immediately (synchronous). */
    public void flushNow() { save(); }

    /** Convenience update with immediate flush. */
    public void updateAndFlush(String key, String value) { set(key, value); save(); }

    // For test support
    public Path getStorePath() { return storePath; }
    public void reload() { load(); }
}
