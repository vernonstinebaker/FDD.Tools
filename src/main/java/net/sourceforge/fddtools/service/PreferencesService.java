package net.sourceforge.fddtools.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    public static final String KEY_LAST_PROJECT_PATH = "project.last.path";
    public static final String KEY_AUTO_LOAD_LAST = "project.autoload.last"; // boolean
    public static final String KEY_LAST_ZOOM = "canvas.last.zoom"; // double
    public static final String KEY_RESTORE_LAST_ZOOM = "canvas.restore.last"; // boolean
    public static final String KEY_LOG_AUDIT_ENABLED = "log.audit.enabled"; // boolean (default true)
    public static final String KEY_LOG_PERF_ENABLED = "log.perf.enabled"; // boolean (default true)
    
    // Layout preferences keys
    public static final String KEY_MAIN_DIVIDER = "layout.mainDivider"; // horizontal: tree vs canvas
    public static final String KEY_RIGHT_DIVIDER = "layout.rightDivider"; // vertical: canvas vs info panels
    
    // Recent files keys
    public static final String KEY_RECENT_FILES_COUNT = "recentFiles.count";
    private static final String KEY_RECENT_FILE_PREFIX = "recentFiles.file_"; // recent_file_0, recent_file_1, etc.

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
            props.putIfAbsent(KEY_LOG_AUDIT_ENABLED, String.valueOf(true));
            props.putIfAbsent(KEY_LOG_PERF_ENABLED, String.valueOf(true));
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

    public String getLastProjectPath() { return get(KEY_LAST_PROJECT_PATH); }
    public void setLastProjectPath(String path) { set(KEY_LAST_PROJECT_PATH, path); }

    public boolean isAutoLoadLastProjectEnabled() { return Boolean.parseBoolean(get(KEY_AUTO_LOAD_LAST)); }
    public void setAutoLoadLastProjectEnabled(boolean enabled) { set(KEY_AUTO_LOAD_LAST, String.valueOf(enabled)); }

    public double getLastZoomLevel() { try { return Double.parseDouble(get(KEY_LAST_ZOOM)); } catch (Exception e) { return 1.0; } }
    public void setLastZoomLevel(double z) { if (z > 0) set(KEY_LAST_ZOOM, String.format(java.util.Locale.US, "%.4f", z)); }

    public boolean isRestoreLastZoomEnabled() { return Boolean.parseBoolean(get(KEY_RESTORE_LAST_ZOOM)); }
    public void setRestoreLastZoomEnabled(boolean enabled) { set(KEY_RESTORE_LAST_ZOOM, String.valueOf(enabled)); }

    public boolean isAuditLoggingEnabled() { return Boolean.parseBoolean(get(KEY_LOG_AUDIT_ENABLED)); }
    public void setAuditLoggingEnabled(boolean enabled) { set(KEY_LOG_AUDIT_ENABLED, String.valueOf(enabled)); }
    public boolean isPerfLoggingEnabled() { return Boolean.parseBoolean(get(KEY_LOG_PERF_ENABLED)); }
    public void setPerfLoggingEnabled(boolean enabled) { set(KEY_LOG_PERF_ENABLED, String.valueOf(enabled)); }

    public void setLastWindowBounds(double x, double y, double w, double h) {
        set(KEY_LAST_WINDOW_X, String.valueOf((int)x));
        set(KEY_LAST_WINDOW_Y, String.valueOf((int)y));
        set(KEY_LAST_WINDOW_W, String.valueOf((int)w));
        set(KEY_LAST_WINDOW_H, String.valueOf((int)h));
    }

    public boolean applyLastWindowBounds(javafx.stage.Stage stage) {
        try {
            int x = Integer.parseInt(get(KEY_LAST_WINDOW_X));
            int y = Integer.parseInt(get(KEY_LAST_WINDOW_Y));
            int w = Integer.parseInt(get(KEY_LAST_WINDOW_W));
            int h = Integer.parseInt(get(KEY_LAST_WINDOW_H));
            if (w > 0 && h > 0) {
                stage.setX(x);
                stage.setY(y);
                stage.setWidth(w);
                stage.setHeight(h);
                return true;
            }
        } catch (Exception ignored) { }
        return false;
    }

    /** Persist immediately (synchronous). */
    public void flushNow() { save(); }

    /** Convenience update with immediate flush. */
    public void updateAndFlush(String key, String value) { set(key, value); save(); }

    // Layout preferences (consolidating LayoutPreferencesService functionality)
    
    public Optional<Double> getMainDividerPosition() {
        try {
            double val = Double.parseDouble(get(KEY_MAIN_DIVIDER));
            if (val > 0) return Optional.of(val);
        } catch (Exception ignored) { }
        return Optional.empty();
    }

    public void setMainDividerPosition(double pos) {
        if (Double.isFinite(pos) && pos > 0.05 && pos < 0.95) {
            set(KEY_MAIN_DIVIDER, String.format(java.util.Locale.US, "%.4f", pos));
        }
    }

    public Optional<Double> getRightDividerPosition() {
        try {
            double val = Double.parseDouble(get(KEY_RIGHT_DIVIDER));
            if (val > 0) return Optional.of(val);
        } catch (Exception ignored) { }
        return Optional.empty();
    }

    public void setRightDividerPosition(double pos) {
        if (Double.isFinite(pos) && pos > 0.05 && pos < 0.95) {
            set(KEY_RIGHT_DIVIDER, String.format(java.util.Locale.US, "%.4f", pos));
        }
    }

    // Recent files management (consolidating RecentFilesService functionality)
    
    /**
     * Adds a file path to the MRU list (deduplicated, most recent first).
     */
    public synchronized void addRecentFile(String path) {
        if (path == null || path.isBlank()) return;
        File f = new File(path);
        if (!f.exists()) return; // ignore non-existing

        List<String> current = getRecentFiles();
        current.remove(path); // dedupe
        current.add(0, path);
        int limit = getRecentFilesLimit();
        if (current.size() > limit) {
            current = current.subList(0, limit);
        }
        persistRecentFiles(current);
    }

    /**
     * Returns a snapshot of current MRU entries (existing files only). Old missing files are pruned.
     */
    public synchronized List<String> getRecentFiles() {
        List<String> list = new ArrayList<>();
        try {
            int count = Integer.parseInt(get(KEY_RECENT_FILES_COUNT));
            for (int i = 0; i < count; i++) {
                String path = get(KEY_RECENT_FILE_PREFIX + i);
                if (path != null && !path.isBlank()) {
                    File file = new File(path);
                    if (file.exists()) {
                        list.add(path);
                    }
                }
            }
        } catch (Exception ignored) { }
        return list;
    }

    /**
     * Clears all MRU entries.
     */
    public synchronized void clearRecentFiles() {
        try {
            int count = Integer.parseInt(get(KEY_RECENT_FILES_COUNT));
            for (int i = 0; i < count; i++) {
                set(KEY_RECENT_FILE_PREFIX + i, null);
            }
        } catch (Exception ignored) { }
        set(KEY_RECENT_FILES_COUNT, "0");
    }

    /** Re-applies the current effective limit, pruning older entries if needed. */
    public synchronized void pruneRecentFilesToLimit() {
        List<String> current = getRecentFiles();
        int limit = getRecentFilesLimit();
        if (current.size() > limit) {
            current = current.subList(0, limit);
            persistRecentFiles(current);
        }
    }

    private void persistRecentFiles(List<String> ordered) {
        // Clear existing entries
        try {
            int oldCount = Integer.parseInt(get(KEY_RECENT_FILES_COUNT));
            for (int i = 0; i < oldCount; i++) {
                set(KEY_RECENT_FILE_PREFIX + i, null);
            }
        } catch (Exception ignored) { }
        
        // Store new entries
        for (int i = 0; i < ordered.size(); i++) {
            set(KEY_RECENT_FILE_PREFIX + i, ordered.get(i));
        }
        set(KEY_RECENT_FILES_COUNT, String.valueOf(ordered.size()));
    }

    // For test support
    public Path getStorePath() { return storePath; }
    public void reload() { load(); }
}
