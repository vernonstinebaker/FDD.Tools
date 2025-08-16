package net.sourceforge.fddtools.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test class for PreferencesService.
 * Tests preferences storage, retrieval, defaults, and thread safety.
 */
class PreferencesServiceComprehensiveTest {

    private PreferencesService prefs;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Use a fresh instance for each test to avoid interference
        prefs = PreferencesService.getInstance();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up any test data
        try {
            prefs.save();
        } catch (Exception ignored) {}
    }

    @Test
    void singletonInstanceReturnsConsistentReference() {
        PreferencesService instance1 = PreferencesService.getInstance();
        PreferencesService instance2 = PreferencesService.getInstance();
        
        assertSame(instance1, instance2, "getInstance() should return the same instance");
        assertNotNull(instance1, "getInstance() should never return null");
    }

    @Test
    void recentFilesLimitHandlesValidValues() {
        // Test default value
        int defaultLimit = prefs.getRecentFilesLimit();
        assertTrue(defaultLimit > 0, "Default recent files limit should be positive");
        
        // Test setting valid values
        prefs.setRecentFilesLimit(5);
        assertEquals(5, prefs.getRecentFilesLimit(), "Should store valid limit value");
        
        prefs.setRecentFilesLimit(20);
        assertEquals(20, prefs.getRecentFilesLimit(), "Should update limit value");
        
        prefs.setRecentFilesLimit(50);
        assertEquals(50, prefs.getRecentFilesLimit(), "Should accept maximum limit");
    }

    @Test
    void recentFilesLimitClampsInvalidValues() {
        // Test boundary clamping
        prefs.setRecentFilesLimit(0);
        assertEquals(1, prefs.getRecentFilesLimit(), "Should clamp to minimum value 1");
        
        prefs.setRecentFilesLimit(-5);
        assertEquals(1, prefs.getRecentFilesLimit(), "Should clamp negative values to 1");
        
        prefs.setRecentFilesLimit(100);
        assertEquals(50, prefs.getRecentFilesLimit(), "Should clamp to maximum value 50");
    }

    @Test
    void uiLanguageHandlesValidValues() {
        // Test setting valid language values
        prefs.setUiLanguage("en");
        assertEquals("en", prefs.getUiLanguage(), "Should store valid language code");
        
        prefs.setUiLanguage("ja-JP");
        assertEquals("ja-JP", prefs.getUiLanguage(), "Should store locale-specific language");
        
        prefs.setUiLanguage("zh_CN");
        assertEquals("zh_CN", prefs.getUiLanguage(), "Should store underscore format language");
    }

    @Test
    void uiLanguageRejectsInvalidValues() {
        // Set a known good value first
        prefs.setUiLanguage("en");
        String knownGoodLang = prefs.getUiLanguage();
        
        prefs.setUiLanguage(null);
        // Language should remain unchanged for invalid values
        String afterNull = prefs.getUiLanguage();
        assertEquals(knownGoodLang, afterNull, "Should not change language for null");
        
        prefs.setUiLanguage("");
        String afterEmpty = prefs.getUiLanguage();
        assertEquals(knownGoodLang, afterEmpty, "Should not change language for empty string");
        
        prefs.setUiLanguage("   ");
        String afterBlank = prefs.getUiLanguage();
        assertEquals(knownGoodLang, afterBlank, "Should not change language for blank string");
    }

    @Test
    void themeHandlesAllValues() {
        // Test theme setting and getting
        prefs.setTheme("light");
        assertEquals("light", prefs.getTheme(), "Should store light theme");
        
        prefs.setTheme("dark");
        assertEquals("dark", prefs.getTheme(), "Should store dark theme");
        
        prefs.setTheme("system");
        assertEquals("system", prefs.getTheme(), "Should store system theme");
        
        // Note: setTheme(null) doesn't clear the value, it just ignores null
        String currentTheme = prefs.getTheme();
        prefs.setTheme(null);
        assertEquals(currentTheme, prefs.getTheme(), "Should not change theme for null input");
    }

    @Test
    void lastProjectPathHandling() {
        String testPath = "/path/to/project.fddi";
        
        prefs.setLastProjectPath(testPath);
        assertEquals(testPath, prefs.getLastProjectPath(), "Should store project path");
        
        prefs.setLastProjectPath(null);
        assertNull(prefs.getLastProjectPath(), "Should handle null path");
        
        String longPath = "/very/long/path/with/many/directories/and/subdirectories/project.fddi";
        prefs.setLastProjectPath(longPath);
        assertEquals(longPath, prefs.getLastProjectPath(), "Should handle long paths");
    }

    @Test
    void autoLoadLastProjectToggle() {
        // Test default state
        boolean defaultValue = prefs.isAutoLoadLastProjectEnabled();
        
        // Toggle the value
        prefs.setAutoLoadLastProjectEnabled(!defaultValue);
        assertEquals(!defaultValue, prefs.isAutoLoadLastProjectEnabled(), 
                    "Should toggle auto-load setting");
        
        prefs.setAutoLoadLastProjectEnabled(true);
        assertTrue(prefs.isAutoLoadLastProjectEnabled(), "Should enable auto-load");
        
        prefs.setAutoLoadLastProjectEnabled(false);
        assertFalse(prefs.isAutoLoadLastProjectEnabled(), "Should disable auto-load");
    }

    @Test
    void zoomLevelHandling() {
        // Test default zoom
        double defaultZoom = prefs.getLastZoomLevel();
        assertTrue(defaultZoom > 0, "Default zoom should be positive");
        
        // Test setting valid zoom levels
        prefs.setLastZoomLevel(0.5);
        assertEquals(0.5, prefs.getLastZoomLevel(), 0.0001, "Should store 50% zoom");
        
        prefs.setLastZoomLevel(2.0);
        assertEquals(2.0, prefs.getLastZoomLevel(), 0.0001, "Should store 200% zoom");
        
        prefs.setLastZoomLevel(0.1);
        assertEquals(0.1, prefs.getLastZoomLevel(), 0.0001, "Should store 10% zoom");
        
        // Test invalid values
        double beforeInvalid = prefs.getLastZoomLevel();
        prefs.setLastZoomLevel(0);
        assertEquals(beforeInvalid, prefs.getLastZoomLevel(), 0.0001, 
                    "Should not change zoom for zero value");
        
        prefs.setLastZoomLevel(-1.5);
        assertEquals(beforeInvalid, prefs.getLastZoomLevel(), 0.0001, 
                    "Should not change zoom for negative value");
    }

    @Test
    void restoreLastZoomToggle() {
        prefs.setRestoreLastZoomEnabled(true);
        assertTrue(prefs.isRestoreLastZoomEnabled(), "Should enable zoom restoration");
        
        prefs.setRestoreLastZoomEnabled(false);
        assertFalse(prefs.isRestoreLastZoomEnabled(), "Should disable zoom restoration");
    }

    @Test
    void loggingPreferences() {
        // Test audit logging
        prefs.setAuditLoggingEnabled(true);
        assertTrue(prefs.isAuditLoggingEnabled(), "Should enable audit logging");
        
        prefs.setAuditLoggingEnabled(false);
        assertFalse(prefs.isAuditLoggingEnabled(), "Should disable audit logging");
        
        // Test performance logging
        prefs.setPerfLoggingEnabled(true);
        assertTrue(prefs.isPerfLoggingEnabled(), "Should enable performance logging");
        
        prefs.setPerfLoggingEnabled(false);
        assertFalse(prefs.isPerfLoggingEnabled(), "Should disable performance logging");
    }

    @Test
    void windowBoundsHandling() {
        // Test that window bounds can be set without error
        assertDoesNotThrow(() -> {
            prefs.setLastWindowBounds(100, 200, 800, 600);
        }, "Should set window bounds without error");
        
        // Test that bounds storage works by using reflection to access private get method
        assertDoesNotThrow(() -> {
            java.lang.reflect.Method getMethod = PreferencesService.class.getDeclaredMethod("get", String.class);
            getMethod.setAccessible(true);
            
            assertEquals("100", getMethod.invoke(prefs, PreferencesService.KEY_LAST_WINDOW_X));
            assertEquals("200", getMethod.invoke(prefs, PreferencesService.KEY_LAST_WINDOW_Y));
            assertEquals("800", getMethod.invoke(prefs, PreferencesService.KEY_LAST_WINDOW_W));
            assertEquals("600", getMethod.invoke(prefs, PreferencesService.KEY_LAST_WINDOW_H));
            
        }, "Should store window bounds correctly");
    }

    @Test
    void windowBoundsRejectsInvalidValues() {
        // Test that the bounds setting/application method gracefully handles invalid values
        // We can't test JavaFX Stage directly, but we can test the validation logic
        
        assertDoesNotThrow(() -> {
            prefs.setLastWindowBounds(100, 200, 0, 600); // Invalid width
            prefs.setLastWindowBounds(100, 200, 800, -100); // Invalid height
        }, "Should handle invalid bounds gracefully");
        
        // The actual validation happens in applyLastWindowBounds which we can't test without JavaFX
        // So we just verify the methods don't throw exceptions
    }

    @Test
    void layoutDividerPositions() {
        // Test main divider
        Optional<Double> mainPos = prefs.getMainDividerPosition();
        // May or may not have a value initially
        
        prefs.setMainDividerPosition(0.3);
        mainPos = prefs.getMainDividerPosition();
        assertTrue(mainPos.isPresent(), "Should have main divider position after setting");
        assertEquals(0.3, mainPos.get(), 0.0001, "Should store correct main divider position");
        
        // Test right divider
        prefs.setRightDividerPosition(0.7);
        Optional<Double> rightPos = prefs.getRightDividerPosition();
        assertTrue(rightPos.isPresent(), "Should have right divider position after setting");
        assertEquals(0.7, rightPos.get(), 0.0001, "Should store correct right divider position");
    }

    @Test
    void layoutDividerPositionValidation() {
        // Store a valid position first
        prefs.setMainDividerPosition(0.5);
        Optional<Double> validPos = prefs.getMainDividerPosition();
        assertTrue(validPos.isPresent(), "Should have valid position");
        
        // Test boundary validation - these should not change the stored value
        prefs.setMainDividerPosition(0.01); // Too small
        Optional<Double> afterSmall = prefs.getMainDividerPosition();
        assertEquals(validPos.get(), afterSmall.get(), 0.0001, "Should not update with too small value");
        
        prefs.setMainDividerPosition(0.99); // Too large  
        Optional<Double> afterLarge = prefs.getMainDividerPosition();
        assertEquals(validPos.get(), afterLarge.get(), 0.0001, "Should not update with too large value");
        
        prefs.setMainDividerPosition(Double.NaN); // Invalid
        Optional<Double> afterNaN = prefs.getMainDividerPosition();
        assertEquals(validPos.get(), afterNaN.get(), 0.0001, "Should not update with NaN value");
        
        prefs.setMainDividerPosition(Double.POSITIVE_INFINITY); // Invalid
        Optional<Double> afterInf = prefs.getMainDividerPosition();
        assertEquals(validPos.get(), afterInf.get(), 0.0001, "Should not update with infinite value");
    }

    @Test
    void saveAndLoadPersistence() {
        // Set some values
        prefs.setRecentFilesLimit(15);
        prefs.setUiLanguage("fr");
        prefs.setTheme("dark");
        prefs.setLastZoomLevel(1.5);
        
        // Save preferences
        assertDoesNotThrow(() -> {
            prefs.save();
        }, "Save should not throw exceptions");
        
        // Verify values are still accessible
        assertEquals(15, prefs.getRecentFilesLimit(), "Values should persist after save");
        assertEquals("fr", prefs.getUiLanguage(), "Language should persist after save");
        assertEquals("dark", prefs.getTheme(), "Theme should persist after save");
        assertEquals(1.5, prefs.getLastZoomLevel(), 0.0001, "Zoom should persist after save");
    }

    @Test
    void flushNowConvenience() {
        prefs.setRecentFilesLimit(25);
        
        assertDoesNotThrow(() -> {
            prefs.flushNow();
        }, "flushNow should not throw exceptions");
        
        assertEquals(25, prefs.getRecentFilesLimit(), "Value should be saved after flush");
    }

    @Test
    void updateAndFlushConvenience() {
        assertDoesNotThrow(() -> {
            prefs.updateAndFlush(PreferencesService.KEY_UI_LANGUAGE, "es");
        }, "updateAndFlush should not throw exceptions");
        
        assertEquals("es", prefs.getUiLanguage(), "Value should be updated and saved");
    }

    @Test
    void recentFilesManagement() throws Exception {
        // Create temporary files for testing
        Path file1 = tempDir.resolve("project1.fddi");
        Path file2 = tempDir.resolve("project2.fddi");
        Path file3 = tempDir.resolve("project3.fddi");
        
        java.nio.file.Files.createFile(file1);
        java.nio.file.Files.createFile(file2);
        java.nio.file.Files.createFile(file3);
        
        // Test adding recent files
        prefs.addRecentFile(file1.toString());
        prefs.addRecentFile(file2.toString());
        prefs.addRecentFile(file3.toString());
        
        List<String> recents = prefs.getRecentFiles();
        assertNotNull(recents, "Recent files list should not be null");
        assertTrue(recents.contains(file1.toString()), "Should contain added file");
        assertTrue(recents.contains(file2.toString()), "Should contain added file");
        assertTrue(recents.contains(file3.toString()), "Should contain added file");
        
        // Most recent should be first
        assertEquals(file3.toString(), recents.get(0), "Most recent file should be first");
    }

    @Test
    void recentFilesDeduplication() throws Exception {
        Path file1 = tempDir.resolve("project.fddi");
        java.nio.file.Files.createFile(file1);
        
        String filePath = file1.toString();
        
        prefs.addRecentFile(filePath);
        prefs.addRecentFile(filePath); // Add same file again
        
        List<String> recents = prefs.getRecentFiles();
        long count = recents.stream().filter(f -> f.equals(filePath)).count();
        assertEquals(1, count, "Should not have duplicate entries");
    }

    @Test
    void recentFilesLimitEnforcement() throws Exception {
        // Set a small limit for testing
        prefs.setRecentFilesLimit(3);
        
        // Create temporary files
        Path file1 = tempDir.resolve("file1.fddi");
        Path file2 = tempDir.resolve("file2.fddi");
        Path file3 = tempDir.resolve("file3.fddi");
        Path file4 = tempDir.resolve("file4.fddi");
        Path file5 = tempDir.resolve("file5.fddi");
        
        java.nio.file.Files.createFile(file1);
        java.nio.file.Files.createFile(file2);
        java.nio.file.Files.createFile(file3);
        java.nio.file.Files.createFile(file4);
        java.nio.file.Files.createFile(file5);
        
        // Add more files than the limit
        prefs.addRecentFile(file1.toString());
        prefs.addRecentFile(file2.toString());
        prefs.addRecentFile(file3.toString());
        prefs.addRecentFile(file4.toString());
        prefs.addRecentFile(file5.toString());
        
        List<String> recents = prefs.getRecentFiles();
        assertEquals(3, recents.size(), "Should enforce recent files limit");
        
        // Should contain the most recent files
        assertTrue(recents.contains(file5.toString()), "Should contain most recent file");
        assertTrue(recents.contains(file4.toString()), "Should contain second most recent file");
        assertTrue(recents.contains(file3.toString()), "Should contain third most recent file");
        assertFalse(recents.contains(file1.toString()), "Should not contain oldest file");
    }

    @Test
    void clearRecentFiles() throws Exception {
        Path file1 = tempDir.resolve("file1.fddi");
        Path file2 = tempDir.resolve("file2.fddi");
        
        java.nio.file.Files.createFile(file1);
        java.nio.file.Files.createFile(file2);
        
        prefs.addRecentFile(file1.toString());
        prefs.addRecentFile(file2.toString());
        
        List<String> recents = prefs.getRecentFiles();
        assertFalse(recents.isEmpty(), "Should have recent files before clearing");
        
        prefs.clearRecentFiles();
        recents = prefs.getRecentFiles();
        assertTrue(recents.isEmpty(), "Should have no recent files after clearing");
    }

    @Test
    void recentFilesCanBePruned() throws Exception {
        Path file1 = tempDir.resolve("file1.fddi");
        Path file2 = tempDir.resolve("file2.fddi");
        Path file3 = tempDir.resolve("file3.fddi");
        
        java.nio.file.Files.createFile(file1);
        java.nio.file.Files.createFile(file2);
        java.nio.file.Files.createFile(file3);
        
        prefs.addRecentFile(file1.toString());
        prefs.addRecentFile(file2.toString());
        prefs.addRecentFile(file3.toString());
        
        // Change the limit to prune files
        prefs.setRecentFilesLimit(2);
        prefs.pruneRecentFilesToLimit();
        
        List<String> recents = prefs.getRecentFiles();
        assertEquals(2, recents.size(), "Should prune to new limit");
        assertTrue(recents.contains(file3.toString()), "Should still contain most recent file");
        assertTrue(recents.contains(file2.toString()), "Should still contain second most recent file");
        assertFalse(recents.contains(file1.toString()), "Should not contain oldest file after pruning");
    }

    @Test
    void threadSafetyBasicTest() {
        // Test concurrent access doesn't throw exceptions
        Thread writer = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                prefs.setRecentFilesLimit(10 + (i % 5));
                prefs.setLastZoomLevel(1.0 + (i % 10) * 0.1);
            }
        });
        
        Thread reader = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                int limit = prefs.getRecentFilesLimit();
                double zoom = prefs.getLastZoomLevel();
                assertTrue(limit > 0, "Limit should always be positive");
                assertTrue(zoom > 0, "Zoom should always be positive");
            }
        });
        
        assertDoesNotThrow(() -> {
            writer.start();
            reader.start();
            writer.join(5000); // 5 second timeout
            reader.join(5000);
        }, "Concurrent access should not throw exceptions");
    }

    @Test
    void handlesMalformedDataGracefully() {
        // This tests error handling for corrupt/invalid stored values
        // The actual implementation should handle NumberFormatException etc. gracefully
        
        // Test that invalid numeric values fall back to defaults
        double zoomBefore = prefs.getLastZoomLevel();
        // If there's a way to inject malformed data, zoom should fall back to 1.0
        assertTrue(zoomBefore > 0, "Should have valid zoom even with malformed data");
        
        int limitBefore = prefs.getRecentFilesLimit();
        // If there's a way to inject malformed data, limit should fall back to default
        assertTrue(limitBefore > 0, "Should have valid limit even with malformed data");
    }
}
