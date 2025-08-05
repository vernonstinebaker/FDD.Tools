package net.sourceforge.fddtools.ui;

import java.awt.Desktop;

/**
 * Utility to test Desktop API support on the current platform.
 * Run this to diagnose macOS integration issues.
 */
public class DesktopAPITest {
    
    public static void main(String[] args) {
        System.out.println("Desktop API Test");
        System.out.println("================");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println();
        
        System.out.println("Desktop Support: " + Desktop.isDesktopSupported());
        
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            System.out.println("\nSupported Actions:");
            
            for (Desktop.Action action : Desktop.Action.values()) {
                System.out.println("  " + action + ": " + desktop.isSupported(action));
            }
            
            // Test setting handlers
            System.out.println("\nTesting Handlers:");
            
            try {
                if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
                    desktop.setAboutHandler(e -> {
                        System.out.println("About handler called!");
                    });
                    System.out.println("  About handler: SET");
                }
            } catch (Exception e) {
                System.out.println("  About handler: FAILED - " + e.getMessage());
            }
            
            try {
                if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
                    desktop.setPreferencesHandler(e -> {
                        System.out.println("Preferences handler called!");
                    });
                    System.out.println("  Preferences handler: SET");
                }
            } catch (Exception e) {
                System.out.println("  Preferences handler: FAILED - " + e.getMessage());
            }
            
            try {
                if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
                    desktop.setQuitHandler((e, response) -> {
                        System.out.println("Quit handler called!");
                        response.performQuit();
                    });
                    System.out.println("  Quit handler: SET");
                }
            } catch (Exception e) {
                System.out.println("  Quit handler: FAILED - " + e.getMessage());
            }
        }
        
        System.out.println("\nTest complete. If handlers were set successfully,");
        System.out.println("the Desktop API should work for macOS integration.");
    }
}