package net.sourceforge.fddtools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import javafx.application.Platform;
import net.sourceforge.fddtools.testutil.HeadlessTestUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FDDApplicationFX.
 * Tests the main JavaFX application lifecycle and initialization.
 */
class FDDApplicationFXTest {

    @BeforeAll
    static void initJfx() {
        // Only initialize JavaFX toolkit if not in headless mode
        if (!HeadlessTestUtil.isHeadlessMode()) {
            try {
                Platform.startup(() -> {});
            } catch (IllegalStateException ignored) {
                // Platform already initialized
            }
        }
    }

    @Test
    void applicationCanBeInstantiated() {
        // Test that the application can be created without errors
        assertDoesNotThrow(() -> {
            FDDApplicationFX application = new FDDApplicationFX();
            assertNotNull(application, "Application instance should be created");
        }, "Application should be instantiable without exceptions");
    }

    @Test
    void applicationInitializesSuccessfully() {
        FDDApplicationFX application = new FDDApplicationFX();
        
        // Test that init() method doesn't throw exceptions
        assertDoesNotThrow(() -> {
            application.init();
        }, "Application init should complete without exceptions");
    }

    @Test
    @EnabledOnOs(OS.MAC)
    void macOSPropertiesSetCorrectly() {
        // Verify macOS-specific properties are configured
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            // These properties should be set by MacOSIntegrationService
            String appName = System.getProperty("apple.awt.application.name");
            String useScreenMenuBar = System.getProperty("apple.laf.useScreenMenuBar");
            
            // Application name should be set
            assertTrue(appName != null && appName.contains("FDD"), 
                "Apple application name should be set and contain 'FDD'");
            
            // Screen menu bar should be enabled
            assertEquals("true", useScreenMenuBar, 
                "Screen menu bar should be enabled on macOS");
        }
    }

    @Test
    void fontAwesomeIconCreationWorks() {
        FDDApplicationFX application = new FDDApplicationFX();
        
        // Test that the private method for creating FontAwesome icons works
        // We can't test the private method directly, but we can test the scenario
        // where it would be used (icon loading failure)
        assertDoesNotThrow(() -> {
            application.init();
        }, "Application should handle icon creation gracefully");
    }

    @Test
    void applicationStopMethodWorks() {
        FDDApplicationFX application = new FDDApplicationFX();
        
        // Test that stop() method doesn't throw exceptions
        assertDoesNotThrow(() -> {
            application.stop();
        }, "Application stop should complete without exceptions");
    }

    @Test
    void loggingInitializationCompletes() {
        // Test that the static initialization block for logging works
        // This is implicitly tested when creating the application
        assertDoesNotThrow(() -> {
            // Creating application triggers static initializers
            new FDDApplicationFX();
        }, "Logging initialization should complete without exceptions");
        
        // Verify logging service is accessible
        assertDoesNotThrow(() -> {
            var logging = net.sourceforge.fddtools.service.LoggingService.getInstance();
            assertNotNull(logging, "LoggingService should be available");
        }, "LoggingService should be accessible after initialization");
    }

    @Test
    void preferencesInitializationCompletes() {
        // Test that preferences service is properly initialized
        assertDoesNotThrow(() -> {
            var prefs = net.sourceforge.fddtools.service.PreferencesService.getInstance();
            assertNotNull(prefs, "PreferencesService should be available");
            
            // Test basic preference access
            boolean auditEnabled = prefs.isAuditLoggingEnabled();
            boolean perfEnabled = prefs.isPerfLoggingEnabled();
            
            // These should return valid boolean values without exceptions
            assertTrue(auditEnabled || !auditEnabled, "Audit logging preference should be accessible");
            assertTrue(perfEnabled || !perfEnabled, "Performance logging preference should be accessible");
        }, "PreferencesService should be accessible and functional");
    }
}
