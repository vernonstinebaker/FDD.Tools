package net.sourceforge.fddtools.ui;

import java.awt.Desktop;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Modern macOS integration using Java 9+ Desktop API.
 * This replaces the legacy OSXAdapter approach.
 */
public class ModernMacOSHandler {
    private static final Logger LOGGER = Logger.getLogger(ModernMacOSHandler.class.getName());
    
    /**
     * Sets up modern macOS handlers for About, Preferences, and Quit.
     * 
     * @param frame The main application frame
     * @return true if handlers were successfully set up, false otherwise
     */
    public static boolean setupMacOSHandlers(FDDFrame frame) {
        if (!Desktop.isDesktopSupported()) {
            LOGGER.warning("Desktop API not supported on this platform");
            return false;
        }
        
        Desktop desktop = Desktop.getDesktop();
        boolean success = true;
        
        // About handler
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            try {
                desktop.setAboutHandler(e -> {
                    LOGGER.info("About menu triggered via Desktop API");
                    // Call the frame's about method
                    frame.about();
                });
                LOGGER.info("About handler registered successfully");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to set About handler", e);
                success = false;
            }
        } else {
            LOGGER.warning("APP_ABOUT action not supported");
            success = false;
        }
        
        // Preferences handler
        if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
            try {
                desktop.setPreferencesHandler(e -> {
                    LOGGER.info("Preferences menu triggered via Desktop API");
                    frame.options();
                });
                LOGGER.info("Preferences handler registered successfully");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to set Preferences handler", e);
                success = false;
            }
        } else {
            LOGGER.warning("APP_PREFERENCES action not supported");
        }
        
        // Quit handler
        if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            try {
                desktop.setQuitHandler((e, response) -> {
                    LOGGER.info("Quit menu triggered via Desktop API");
                    // Call the frame's quit method
                    if (frame.quit()) {
                        response.performQuit();
                    } else {
                        response.cancelQuit();
                    }
                });
                LOGGER.info("Quit handler registered successfully");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to set Quit handler", e);
                success = false;
            }
        } else {
            LOGGER.warning("APP_QUIT_HANDLER action not supported");
        }
        
        // Optional: Request user attention (dock bouncing)
        if (desktop.isSupported(Desktop.Action.APP_REQUEST_FOREGROUND)) {
            // Can be used to request user attention when needed
            LOGGER.info("APP_REQUEST_FOREGROUND supported");
        }
        
        // Log system properties for debugging
        LOGGER.info("Java version: " + System.getProperty("java.version"));
        LOGGER.info("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        
        return success;
    }
    
    /**
     * Checks if the modern Desktop API is available and functional.
     * 
     * @return true if Desktop API is available, false otherwise
     */
    public static boolean isDesktopAPIAvailable() {
        if (!Desktop.isDesktopSupported()) {
            return false;
        }
        
        Desktop desktop = Desktop.getDesktop();
        // Check if at least one of our required actions is supported
        return desktop.isSupported(Desktop.Action.APP_ABOUT) ||
               desktop.isSupported(Desktop.Action.APP_PREFERENCES) ||
               desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER);
    }
}