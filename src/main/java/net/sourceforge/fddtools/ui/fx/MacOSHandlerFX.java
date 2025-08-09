package net.sourceforge.fddtools.ui.fx;

import java.awt.Desktop;
import java.awt.Taskbar;
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sourceforge.fddtools.ui.fx.AboutDialogFX;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * JavaFX-compatible macOS integration using Java 9+ Desktop API.
 * This replaces the Swing-based ModernMacOSHandler for JavaFX applications.
 */
public class MacOSHandlerFX {
    private static final Logger LOGGER = LoggerFactory.getLogger(MacOSHandlerFX.class);
    
    /**
     * Sets up macOS handlers for About, Preferences, and Quit for JavaFX applications.
     * 
     * @param mainWindow The main JavaFX window
     * @param primaryStage The primary JavaFX stage
     * @return true if handlers were successfully set up, false otherwise
     */
    public static boolean setupMacOSHandlers(FDDMainWindowFX mainWindow, Stage primaryStage) {
        if (!Desktop.isDesktopSupported()) {
            LOGGER.warn("Desktop API not supported on this platform");
            return false;
        }
        
        Desktop desktop = Desktop.getDesktop();
        boolean success = true;
        
        // Try to set application name using AWT's Application API
        try {
            // Force AWT initialization to pick up our system properties
            java.awt.EventQueue.invokeLater(() -> {
                // This forces AWT to initialize and should pick up our apple.awt.application.name property
                String appName = System.getProperty("apple.awt.application.name");
                LOGGER.info("AWT initialized - application name should be: {}", appName);
            });
        } catch (Exception e) {
            LOGGER.info("AWT initialization issue", e);
        }
        
        // About handler
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            try {
                desktop.setAboutHandler(e -> {
                    LOGGER.info("About menu triggered via Desktop API");
                    Platform.runLater(() -> {
                        AboutDialogFX about = new AboutDialogFX(primaryStage);
                        about.showAndWait();
                    });
                });
                LOGGER.info("About handler registered successfully");
            } catch (Exception e) {
                LOGGER.warn("Failed to set About handler", e);
                success = false;
            }
        } else {
            LOGGER.warn("APP_ABOUT action not supported");
            success = false;
        }
        
        // Preferences handler
        if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
            try {
                desktop.setPreferencesHandler(e -> {
                    LOGGER.info("Preferences menu triggered via Desktop API");
                    Platform.runLater(() -> {
                        // Call the main window's preferences method
                        mainWindow.showPreferencesDialog();
                    });
                });
                LOGGER.info("Preferences handler registered successfully");
            } catch (Exception e) {
                LOGGER.warn("Failed to set Preferences handler", e);
                success = false;
            }
        } else {
            LOGGER.warn("APP_PREFERENCES action not supported");
        }
        
        // Quit handler
        if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            try {
                desktop.setQuitHandler((e, response) -> {
                    LOGGER.info("Quit menu triggered via Desktop API");
                    Platform.runLater(() -> {
                        if (mainWindow.canClose()) {
                            response.performQuit();
                            Platform.exit();
                        } else {
                            response.cancelQuit();
                        }
                    });
                });
                LOGGER.info("Quit handler registered successfully");
            } catch (Exception e) {
                LOGGER.warn("Failed to set Quit handler", e);
                success = false;
            }
        } else {
            LOGGER.warn("APP_QUIT_HANDLER action not supported");
        }
        
        // Optional: Request user attention (dock bouncing)
        if (desktop.isSupported(Desktop.Action.APP_REQUEST_FOREGROUND)) {
            LOGGER.info("APP_REQUEST_FOREGROUND supported");
        }
        
        // Log system properties for debugging
    LOGGER.info("Java version: {}", System.getProperty("java.version"));
    LOGGER.info("OS: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
    LOGGER.info("Application name property: {}", System.getProperty("apple.awt.application.name"));
    LOGGER.info("About menu name property: {}", System.getProperty("com.apple.mrj.application.apple.menu.about.name"));
        
        return success;
    }
    
    /**
     * Checks if the modern Desktop API is available and functional.
     * 
     * @return true if Desktop API is available, false otherwise
     */
    public static boolean isDesktopSupported() {
        return Desktop.isDesktopSupported();
    }
    
    /**
     * Sets additional macOS system properties for proper application identification.
     * This should be called early in the application lifecycle.
     */
    public static void setMacOSProperties() {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            // Set application name properties
            System.setProperty("apple.awt.application.name", "FDD Tools");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "FDD Tools");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            
            // Set dock properties
            System.setProperty("apple.awt.application.appearance", "system");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
            
            // Force dock icon and name
            System.setProperty("java.awt.headless", "false");
            
            // Try to set dock icon programmatically
            setDockIcon();
            
            LOGGER.info("macOS system properties configured for FDD Tools");
        }
    }
    
    /**
     * Attempts to set the dock icon using AWT/Swing APIs for better integration
     */
    private static void setDockIcon() {
        try {
            if (Taskbar.isTaskbarSupported()) {
                Taskbar taskbar = Taskbar.getTaskbar();
                if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    // Try to load our application icon
                    InputStream iconStream = MacOSHandlerFX.class.getResourceAsStream("/FDDTools.png");
                    if (iconStream != null) {
                        Image dockIcon = ImageIO.read(iconStream);
                        taskbar.setIconImage(dockIcon);
                        LOGGER.info("Successfully set dock icon via Taskbar API");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to set dock icon: {}", e.getMessage());
        }
    }
}
