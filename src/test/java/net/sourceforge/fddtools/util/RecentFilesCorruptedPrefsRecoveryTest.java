package net.sourceforge.fddtools.util;

import org.junit.jupiter.api.*;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.List;
import java.util.prefs.Preferences; 

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ensures RecentFilesService is resilient to corrupted / non-numeric index preference keys.
 */
public class RecentFilesCorruptedPrefsRecoveryTest {

    @BeforeEach
    void clear() { RecentFilesService.getInstance().clear(); }

    @AfterEach
    void cleanup() { RecentFilesService.getInstance().clear(); }

    @Test
    void ignoresCorruptedKeysAndRetainsValidOnes() throws Exception {
        // Arrange: populate some valid entries
        RecentFilesService svc = RecentFilesService.getInstance();
        File valid1 = File.createTempFile("rfs_valid1", ".tmp");
        File valid2 = File.createTempFile("rfs_valid2", ".tmp");
        svc.addRecentFile(valid1.getAbsolutePath());
        svc.addRecentFile(valid2.getAbsolutePath());

        // Inject corrupted keys directly into underlying Preferences node via reflection
        Preferences prefs = getUnderlyingPreferences();
        prefs.put("recent_notanumber", valid1.getAbsolutePath());
        prefs.put("recent_abc", valid2.getAbsolutePath());
        prefs.put("random_other_key", "junk");

        // Act
        List<String> recents = svc.getRecentFiles();

        // Assert
        assertTrue(recents.contains(valid1.getAbsolutePath()));
        assertTrue(recents.contains(valid2.getAbsolutePath()));
        // None of the corrupted keys should cause duplicates or failures
        assertEquals(2, recents.size());

        // Cleanup temp files
        Files.deleteIfExists(valid1.toPath());
        Files.deleteIfExists(valid2.toPath());
    }

    private Preferences getUnderlyingPreferences() throws Exception {
        Field f = RecentFilesService.class.getDeclaredField("instance");
        f.setAccessible(true);
        Object inst = f.get(null);
        if (inst == null) throw new IllegalStateException("RecentFilesService not initialized");
        Field pf = RecentFilesService.class.getDeclaredField("prefs");
        pf.setAccessible(true);
        return (Preferences) pf.get(inst);
    }
}
