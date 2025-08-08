/**
 * JavaFX Application entry point for FDD Tools.
 * This replaces the traditional Swing-based Main.java approach with a modern JavaFX Application.
 */
package net.sourceforge.fddtools;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.sourceforge.fddtools.ui.fx.FDDMainWindowFX;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FDDApplicationFX extends Application {
    private static final Logger LOGGER = Logger.getLogger(FDDApplicationFX.class.getName());
    
    static {
        // CRITICAL: Set ALL macOS properties FIRST, before any JavaFX or AWT initialization
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            // Application name properties - MUST be set before AWT/JavaFX initialization
            System.setProperty("apple.awt.application.name", "FDD Tools");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "FDD Tools");
            
            // Additional application name properties for broader compatibility
            System.setProperty("apple.awt.application.title", "FDD Tools");
            System.setProperty("com.apple.eio.FileManager.enableExtensionPopup", "false");
            
            // Menu bar properties - for both AWT and JavaFX compatibility
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.macos.useScreenMenuBar", "true");
            
            // JavaFX-specific macOS properties
            System.setProperty("javafx.macosx.embedded", "false");
            System.setProperty("glass.accessible.force", "false");
            
            // Force AWT application name to be used by JavaFX
            System.setProperty("java.awt.application.name", "FDD Tools");
            
            // Additional macOS integration properties
            System.setProperty("apple.awt.enableTemplateImages", "true");
            System.setProperty("apple.awt.application.appearance", "system");
        }
        
        // Ensure AWT is not headless for Desktop API integration
        System.setProperty("java.awt.headless", "false");
        
        System.out.println("DEBUG: macOS properties set in static block:");
        System.out.println("  apple.awt.application.name = " + System.getProperty("apple.awt.application.name"));
        System.out.println("  java.awt.application.name = " + System.getProperty("java.awt.application.name"));
        System.out.println("  apple.laf.useScreenMenuBar = " + System.getProperty("apple.laf.useScreenMenuBar"));
    }
    
    private FDDMainWindowFX mainWindow;
    
    @Override
    public void init() throws Exception {
        // Called before start() - good place for early initialization
        super.init();
        
        LOGGER.info("FDD Tools JavaFX application initialized");
        
        // Verify macOS properties are set correctly
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            LOGGER.info("macOS properties verification:");
            LOGGER.info("  apple.awt.application.name = " + System.getProperty("apple.awt.application.name"));
            LOGGER.info("  apple.laf.useScreenMenuBar = " + System.getProperty("apple.laf.useScreenMenuBar"));
        }
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Configure the primary stage
            primaryStage.setTitle("FDD Tools");
            
            // Try to set application name for macOS through different methods
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                try {
                    // Method 1: Force application name through runtime
                    Runtime.getRuntime().exec(new String[] {"osascript", "-e", 
                        "tell application \"System Events\" to set name of application process \"java\" to \"FDD Tools\""});
                } catch (Exception e) {
                    LOGGER.log(Level.INFO, "Could not set application name via osascript: " + e.getMessage());
                }
                
                try {
                    // Method 2: Try to use reflection to set the application name
                    Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
                    Object application = applicationClass.getMethod("getApplication").invoke(null);
                    // Note: This may not work in modern Java, but worth trying
                } catch (Exception e) {
                    LOGGER.log(Level.INFO, "Could not access Apple EAWT Application class: " + e.getMessage());
                }
            }
            
            // Set application icon if available
            try {
                // Add multiple icon sizes for better macOS integration
                primaryStage.getIcons().addAll(
                    new Image(getClass().getResourceAsStream("/FDDTools-16.png")),
                    new Image(getClass().getResourceAsStream("/FDDTools-32.png")),
                    new Image(getClass().getResourceAsStream("/FDDTools-64.png")),
                    new Image(getClass().getResourceAsStream("/FDDTools-128.png")),
                    new Image(getClass().getResourceAsStream("/FDDTools.png"))
                );
                LOGGER.info("Successfully loaded application icon");
                
                // Also try to set the dock icon immediately for macOS
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    try {
                        java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                        if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                            // Try to load the icon as AWT Image for dock
                            java.awt.Image dockIcon = java.awt.Toolkit.getDefaultToolkit().getImage(
                                getClass().getResource("/FDDTools-128.png"));
                            if (dockIcon != null) {
                                taskbar.setIconImage(dockIcon);
                                LOGGER.info("Successfully set dock icon in FDDApplicationFX");
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.INFO, "Could not set dock icon in FDDApplicationFX: " + e.getMessage());
                    }
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not load application icon: " + e.getMessage());
                // Try alternative formats
                try {
                    Image fallbackIcon = new Image(getClass().getResourceAsStream("/net/sourceforge/fddtools/ui/images/document-properties.png"));
                    primaryStage.getIcons().add(fallbackIcon);
                    LOGGER.info("Loaded fallback application icon");
                } catch (Exception e2) {
                    LOGGER.log(Level.WARNING, "Could not load fallback icon either", e2);
                }
            }
            
            // Create the main window
            mainWindow = new FDDMainWindowFX(primaryStage);
            
            // Create the scene
            Scene scene = new Scene(mainWindow, 1400, 900);
            
            // Add CSS styling if available
            try {
                String css = getClass().getResource("/styles/fdd-canvas.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not load CSS stylesheet", e);
            }
            
            // Configure the stage
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            // Center on screen
            primaryStage.centerOnScreen();
            
            // Show the application
            primaryStage.show();
            
            // Handle application close
            primaryStage.setOnCloseRequest(event -> {
                if (mainWindow.canClose()) {
                    Platform.exit();
                } else {
                    event.consume(); // Cancel close if unsaved changes
                }
            });
            
            LOGGER.info("FDD Tools JavaFX application started successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start FDD Tools application", e);
            Platform.exit();
        }
    }
    
    @Override
    public void stop() throws Exception {
        if (mainWindow != null) {
            mainWindow.cleanup();
        }
        super.stop();
    }
    
    /**
     * Entry point for the application.
     * This replaces the traditional Swing main method.
     */
    public static void main(String[] args) {
        // macOS properties are already set in static block
        
        // Set JavaFX implicit exit to false for better control
        Platform.setImplicitExit(false);
        
        // Launch the JavaFX application
        Application.launch(FDDApplicationFX.class, args);
    }
}
