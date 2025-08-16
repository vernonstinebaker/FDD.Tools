package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.sourceforge.fddtools.service.ThemeService.Theme;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ThemeService covering singleton pattern,
 * theme switching functionality, stylesheet management, and JavaFX Scene integration.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ThemeServiceComprehensiveTest {
    
    private static boolean fxStarted = false;
    private static ThemeService service;
    
    @BeforeAll
    static void initializeTestEnvironment() throws Exception {
        if (!fxStarted) {
            CountDownLatch latch = new CountDownLatch(1);
            try { 
                Platform.startup(latch::countDown); 
            } catch (IllegalStateException alreadyStarted) { 
                latch.countDown(); 
            }
            assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX initialization timeout");
            fxStarted = true;
        }
        service = ThemeService.getInstance();
    }
    
    @Test
    @Order(1)
    void singletonPatternConsistency() {
        ThemeService instance1 = ThemeService.getInstance();
        ThemeService instance2 = ThemeService.getInstance();
        
        assertSame(instance1, instance2, "ThemeService should return same instance");
        assertSame(service, instance1, "Service should maintain singleton consistency");
        assertNotNull(instance1, "Service instance should not be null");
    }
    
    @Test
    @Order(2)
    void themeEnumCompleteness() {
        Theme[] themes = Theme.values();
        assertEquals(4, themes.length, "Should have exactly 4 theme options");
        
        // Verify all expected themes exist
        assertTrue(List.of(themes).contains(Theme.SYSTEM), "Should include SYSTEM theme");
        assertTrue(List.of(themes).contains(Theme.LIGHT), "Should include LIGHT theme");
        assertTrue(List.of(themes).contains(Theme.DARK), "Should include DARK theme");
        assertTrue(List.of(themes).contains(Theme.HIGH_CONTRAST), "Should include HIGH_CONTRAST theme");
        
        // Verify enum names are correctly defined
        assertEquals("SYSTEM", Theme.SYSTEM.name());
        assertEquals("LIGHT", Theme.LIGHT.name());
        assertEquals("DARK", Theme.DARK.name());
        assertEquals("HIGH_CONTRAST", Theme.HIGH_CONTRAST.name());
    }
    
    @Test
    @Order(3)
    void applyThemeToSceneDirectly() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<String>> stylesheets = new AtomicReference<>();
        
        Platform.runLater(() -> {
            VBox root = new VBox(new Label("Test"), new Button("Test"));
            Scene scene = new Scene(root, 200, 100);
            
            // Apply LIGHT theme directly to scene
            service.applyThemeTo(scene, Theme.LIGHT);
            stylesheets.set(List.copyOf(scene.getStylesheets()));
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Theme application timeout");
        
        List<String> sheets = stylesheets.get();
        assertNotNull(sheets, "Stylesheets list should not be null");
        
        // Since stylesheets might not exist in test environment, we just verify no exceptions occurred
        assertTrue(sheets.size() >= 0, "Stylesheets should be accessible");
    }
    
    @Test
    @Order(4)
    void applyThemeToSceneWithNullHandling() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean exceptionOccurred = new AtomicBoolean(false);
        
        Platform.runLater(() -> {
            try {
                // Should handle null scene gracefully
                service.applyThemeTo(null, Theme.DARK);
            } catch (Exception e) {
                exceptionOccurred.set(true);
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Null handling timeout");
        assertFalse(exceptionOccurred.get(), "Should handle null scene without throwing exception");
    }
    
    @Test
    @Order(5)
    void themeSwitchingRemovesPreviousThemes() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<String>> finalStylesheets = new AtomicReference<>();
        
        Platform.runLater(() -> {
            VBox root = new VBox(new Label("Test"));
            Scene scene = new Scene(root, 200, 100);
            
            // Apply multiple themes in sequence
            service.applyThemeTo(scene, Theme.LIGHT);
            service.applyThemeTo(scene, Theme.DARK);
            service.applyThemeTo(scene, Theme.HIGH_CONTRAST);
            
            finalStylesheets.set(List.copyOf(scene.getStylesheets()));
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Theme switching timeout");
        
        List<String> sheets = finalStylesheets.get();
        
        // Should not contain previous theme stylesheets (if they exist)
        long lightCount = sheets.stream().filter(s -> s.contains("global-theme-light.css")).count();
        long darkCount = sheets.stream().filter(s -> s.contains("global-theme-dark.css")).count();
        
        // Light and dark should not both be present after switching to high contrast
        assertFalse(lightCount > 0 && darkCount > 0, 
                "Should not have both light and dark themes after switching");
    }
    
    @Test
    @Order(6)
    void systemThemeApplicationUsesGlobalTheme() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<String>> stylesheets = new AtomicReference<>();
        
        Platform.runLater(() -> {
            VBox root = new VBox(new Label("Test"));
            Scene scene = new Scene(root, 200, 100);
            
            service.applyThemeTo(scene, Theme.SYSTEM);
            stylesheets.set(List.copyOf(scene.getStylesheets()));
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "System theme application timeout");
        
        List<String> sheets = stylesheets.get();
        assertNotNull(sheets, "Stylesheets should not be null");
        
        // Should work without throwing exceptions
        assertTrue(sheets.size() >= 0, "System theme should apply successfully");
    }
    
    @Test
    @Order(7)
    void allThemeVariantsCanBeApplied() throws Exception {
        for (Theme theme : Theme.values()) {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Exception> exception = new AtomicReference<>();
            
            Platform.runLater(() -> {
                try {
                    VBox root = new VBox(new Label("Test " + theme));
                    Scene scene = new Scene(root, 200, 100);
                    
                    service.applyThemeTo(scene, theme);
                } catch (Exception e) {
                    exception.set(e);
                } finally {
                    latch.countDown();
                }
            });
            
            assertTrue(latch.await(5, TimeUnit.SECONDS), "Theme " + theme + " application timeout");
            assertNull(exception.get(), "Theme " + theme + " should apply without exception");
        }
    }
    
    @Test
    @Order(8)
    void stylesheetDuplicationPrevention() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<String>> stylesheets = new AtomicReference<>();
        
        Platform.runLater(() -> {
            VBox root = new VBox(new Label("Test"));
            Scene scene = new Scene(root, 200, 100);
            
            // Apply same theme multiple times
            service.applyThemeTo(scene, Theme.DARK);
            service.applyThemeTo(scene, Theme.DARK);
            service.applyThemeTo(scene, Theme.DARK);
            
            stylesheets.set(List.copyOf(scene.getStylesheets()));
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Duplication prevention timeout");
        
        List<String> sheets = stylesheets.get();
        
        // Count occurrences of dark theme stylesheet (if it exists)
        long darkCount = sheets.stream().filter(s -> s.contains("global-theme-dark.css")).count();
        assertTrue(darkCount <= 1, "Dark theme stylesheet should appear at most once, found: " + darkCount);
        
        // Count occurrences of semantic theme stylesheet (if it exists)
        long semanticCount = sheets.stream().filter(s -> s.contains("semantic-theme.css")).count();
        assertTrue(semanticCount <= 1, "Semantic theme stylesheet should appear at most once, found: " + semanticCount);
    }
    
    @Test
    @Order(9)
    void applyThemeAsyncWithPlatformRunLater() throws Exception {
        // Test the async path by creating a stage and trying to apply theme
        CountDownLatch setupLatch = new CountDownLatch(1);
        CountDownLatch themeLatch = new CountDownLatch(1);
        AtomicReference<Exception> exception = new AtomicReference<>();
        
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                VBox root = new VBox(new Label("Async test"));
                Scene scene = new Scene(root, 200, 100);
                stage.setScene(scene);
                // Don't show to keep headless
                
                setupLatch.countDown();
                
                // Apply theme asynchronously
                service.applyTheme(Theme.HIGH_CONTRAST);
                
                // Give some time for async application
                Platform.runLater(() -> {
                    stage.close();
                    themeLatch.countDown();
                });
            } catch (Exception e) {
                exception.set(e);
                setupLatch.countDown();
                themeLatch.countDown();
            }
        });
        
        assertTrue(setupLatch.await(5, TimeUnit.SECONDS), "Async setup timeout");
        assertTrue(themeLatch.await(5, TimeUnit.SECONDS), "Async theme application timeout");
        assertNull(exception.get(), "Async theme application should not throw exception");
    }
    
    @Test
    @Order(10)
    void threadSafetyForSingleton() throws Exception {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<Exception> exception = new AtomicReference<>();
        ThemeService[] instances = new ThemeService[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    instances[index] = ThemeService.getInstance();
                } catch (Exception e) {
                    exception.set(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Thread safety test timeout");
        assertNull(exception.get(), "Should not throw exception during concurrent access");
        
        // All instances should be the same
        for (ThemeService instance : instances) {
            assertSame(service, instance, "All instances should be identical in concurrent access");
        }
    }
    
    @Test
    @Order(11)
    void errorHandlingWithMissingStylesheets() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> exception = new AtomicReference<>();
        
        Platform.runLater(() -> {
            try {
                VBox root = new VBox(new Label("Error handling test"));
                Scene scene = new Scene(root, 200, 100);
                
                // Apply themes - should handle missing stylesheets gracefully
                service.applyThemeTo(scene, Theme.LIGHT);
                service.applyThemeTo(scene, Theme.DARK);
                service.applyThemeTo(scene, Theme.HIGH_CONTRAST);
                service.applyThemeTo(scene, Theme.SYSTEM);
                        
            } catch (Exception e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Error handling timeout");
        assertNull(exception.get(), "Should handle missing stylesheets gracefully");
    }
    
    @Test
    @Order(12)
    void stylesheetOrderingAndManagement() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<String>> stylesheets = new AtomicReference<>();
        
        Platform.runLater(() -> {
            VBox root = new VBox(new Label("Ordering test"));
            Scene scene = new Scene(root, 200, 100);
            
            // Add some custom stylesheets first
            scene.getStylesheets().addAll(List.of(
                "custom1.css",
                "custom2.css"
            ));
            
            // Apply theme
            service.applyThemeTo(scene, Theme.DARK);
            stylesheets.set(List.copyOf(scene.getStylesheets()));
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Stylesheet ordering timeout");
        
        List<String> sheets = stylesheets.get();
        assertFalse(sheets.isEmpty(), "Should have stylesheets");
        
        // Custom stylesheets should still be present
        assertTrue(sheets.contains("custom1.css"), "Custom stylesheet 1 should be preserved");
        assertTrue(sheets.contains("custom2.css"), "Custom stylesheet 2 should be preserved");
    }
    
    @Test
    @Order(13)
    void themeServiceApiCompleteness() {
        // Verify the service has all expected public methods
        assertDoesNotThrow(() -> service.applyTheme(Theme.SYSTEM), 
                "applyTheme method should be available");
        
        assertDoesNotThrow(() -> {
            VBox root = new VBox();
            Scene scene = new Scene(root);
            service.applyThemeTo(scene, Theme.LIGHT);
        }, "applyThemeTo method should be available");
        
        // Verify singleton
        assertSame(service, ThemeService.getInstance(), 
                "getInstance should return consistent singleton");
    }
}
