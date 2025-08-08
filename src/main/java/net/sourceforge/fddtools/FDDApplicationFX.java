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
        // Set macOS specific properties before JavaFX Application starts
        System.setProperty("apple.awt.application.name", "FDD Tools");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "FDD Tools");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        
        // Additional macOS properties for proper application identification
        System.setProperty("java.awt.headless", "false");
        System.setProperty("apple.awt.application.appearance", "system");
        
        // Ensure proper application naming for dock
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false");
        }
    }
    
    private FDDMainWindowFX mainWindow;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Configure the primary stage
            primaryStage.setTitle("FDD Tools");
            
            // Set application icon if available
            try {
                Image icon = new Image(getClass().getResourceAsStream("/FDDTools.icns"));
                primaryStage.getIcons().add(icon);
                LOGGER.info("Successfully loaded application icon");
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
        // Configure macOS-specific properties for JavaFX
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.awt.application.name", "FDD Tools");
            // Note: JavaFX handles menu bar integration automatically on macOS
        }
        
        // Set JavaFX implicit exit to false for better control
        Platform.setImplicitExit(false);
        
        // Launch the JavaFX application
        Application.launch(FDDApplicationFX.class, args);
    }
}
