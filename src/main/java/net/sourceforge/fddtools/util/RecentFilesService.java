package net.sourceforge.fddtools.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Manages a Most-Recently-Used (MRU) list of project files.
 * Stored using Java Preferences for simple cross-platform persistence.
 */
public final class RecentFilesService {
    private static final int MAX_ENTRIES = 10;
    private static final String PREF_NODE = "recentFiles"; // sub-node
    private static final String KEY_PREFIX = "recent_";    // keys recent_0 .. recent_n

    private static RecentFilesService instance;
    private final Preferences prefs;

    private RecentFilesService() {
        prefs = Preferences.userNodeForPackage(RecentFilesService.class).node(PREF_NODE);
    }

    public static synchronized RecentFilesService getInstance() {
        if (instance == null) {
            instance = new RecentFilesService();
        }
        return instance;
    }

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
        if (current.size() > MAX_ENTRIES) {
            current = current.subList(0, MAX_ENTRIES);
        }
        persist(current);
    }

    /**
     * Returns a snapshot of current MRU entries (existing files only). Old missing files are pruned.
     */
    public synchronized List<String> getRecentFiles() {
        List<String> list = new ArrayList<>();
        try {
            String[] keys = prefs.keys();
            // Collect into array sized by index for ordering
            List<Entry> entries = new ArrayList<>();
            for (String k : keys) {
                if (k.startsWith(KEY_PREFIX)) {
                    try {
                        int idx = Integer.parseInt(k.substring(KEY_PREFIX.length()));
                        String val = prefs.get(k, null);
                        if (val != null && !val.isBlank()) {
                            entries.add(new Entry(idx, val));
                        }
                    } catch (NumberFormatException ignore) { }
                }
            }
            Collections.sort(entries);
            for (Entry e : entries) {
                File file = new File(e.value);
                if (file.exists()) {
                    list.add(e.value);
                }
            }
        } catch (BackingStoreException e) {
            // If prefs fail, return what we collected so far
        }
        return list;
    }

    /**
     * Clears all MRU entries.
     */
    public synchronized void clear() {
        try {
            prefs.clear();
        } catch (BackingStoreException ignored) { }
    }

    private void persist(List<String> ordered) {
        try {
            prefs.clear();
            for (int i = 0; i < ordered.size(); i++) {
                prefs.put(KEY_PREFIX + i, ordered.get(i));
            }
        } catch (BackingStoreException ignored) { }
    }

    private static class Entry implements Comparable<Entry> {
        final int index; final String value;
        Entry(int index, String value) { this.index = index; this.value = value; }
        @Override public int compareTo(Entry o) { return Integer.compare(this.index, o.index); }
    }
}
