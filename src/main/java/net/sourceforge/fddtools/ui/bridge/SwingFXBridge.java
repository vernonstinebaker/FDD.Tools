package net.sourceforge.fddtools.ui.bridge;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for bridging Swing and JavaFX components during incremental migration.
 * This allows embedding JavaFX content in Swing containers and vice versa.
 */
public class SwingFXBridge {
    private static final Logger LOGGER = Logger.getLogger(SwingFXBridge.class.getName());
    private static boolean javaFXInitialized = false;
    private static final Object INIT_LOCK = new Object();
    
    /**
     * Ensures JavaFX runtime is initialized. Safe to call multiple times.
     */
    public static void initializeJavaFX() {
        synchronized (INIT_LOCK) {
            if (!javaFXInitialized) {
                try {
                    // This will initialize the JavaFX runtime
                    new JFXPanel();
                    javaFXInitialized = true;
                    LOGGER.info("JavaFX runtime initialized successfully");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to initialize JavaFX runtime", e);
                    throw new RuntimeException("JavaFX initialization failed", e);
                }
            }
        }
    }
    
    /**
     * Creates a JFXPanel that can be embedded in Swing containers.
     * The scene will be set on the JavaFX Application Thread.
     * 
     * @param sceneCreator A runnable that creates and sets the JavaFX scene
     * @return JFXPanel ready to be added to Swing containers
     */
    public static JFXPanel createJFXPanel(Runnable sceneCreator) {
        initializeJavaFX();
        
        JFXPanel jfxPanel = new JFXPanel();
        
        Platform.runLater(() -> {
            try {
                sceneCreator.run();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error creating JavaFX scene", e);
            }
        });
        
        return jfxPanel;
    }
    
    /**
     * Embeds a Swing component in a JavaFX SwingNode.
     * 
     * @param swingComponent The Swing component to embed
     * @return SwingNode containing the Swing component
     */
    public static SwingNode embedSwingComponent(JComponent swingComponent) {
        SwingNode swingNode = new SwingNode();
        
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(swingComponent);
        });
        
        return swingNode;
    }
    
    /**
     * Runs code on the JavaFX Application Thread and waits for completion.
     * Useful for synchronous operations during migration.
     * 
     * @param runnable The code to run on JavaFX thread
     */
    public static void runAndWait(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    runnable.run();
                } finally {
                    latch.countDown();
                }
            });
            
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Interrupted while waiting for JavaFX operation", e);
            }
        }
    }
    
    /**
     * Safely updates Swing components from JavaFX thread.
     * 
     * @param runnable The Swing update code
     */
    public static void updateSwingFromFX(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
    
    /**
     * Safely updates JavaFX components from Swing thread.
     * 
     * @param runnable The JavaFX update code
     */
    public static void updateFXFromSwing(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
}