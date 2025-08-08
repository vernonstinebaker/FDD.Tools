/**
 * Enhanced DialogBridge with JavaFX Stage support.
 * This extends the existing DialogBridge to support pure JavaFX applications.
 */
package net.sourceforge.fddtools.ui.bridge;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.ui.fx.AboutDialogFX;
import net.sourceforge.fddtools.ui.fx.FDDElementDialogFX;

import javax.swing.JFrame;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class DialogBridgeFX {
    private static final Logger LOGGER = Logger.getLogger(DialogBridgeFX.class.getName());
    
    /**
     * Show About dialog with JavaFX Stage as parent.
     */
    public static void showAboutDialog(Stage parent) {
        Platform.runLater(() -> {
            try {
                AboutDialogFX aboutDialog = new AboutDialogFX(parent);
                aboutDialog.showAndWait();
            } catch (Exception e) {
                LOGGER.severe("Failed to show About dialog: " + e.getMessage());
            }
        });
    }
    
    /**
     * Show About dialog with any Window as parent.
     */
    public static void showAboutDialog(Window parent) {
        Platform.runLater(() -> {
            try {
                Stage parentStage = (parent instanceof Stage) ? (Stage) parent : null;
                AboutDialogFX aboutDialog = new AboutDialogFX(parentStage);
                aboutDialog.showAndWait();
            } catch (Exception e) {
                LOGGER.severe("Failed to show About dialog: " + e.getMessage());
            }
        });
    }
    
    /**
     * Show Element edit dialog with JavaFX Stage as parent.
     */
    public static void showElementDialog(Stage parent, FDDINode node, Consumer<Boolean> onCompletion) {
        Platform.runLater(() -> {
            try {
                FDDElementDialogFX elementDialog = new FDDElementDialogFX(parent, node);
                
                elementDialog.showAndWait();
                
                // Check if dialog was accepted (OK button pressed)
                if (onCompletion != null) {
                    // For now, assume accepted - TODO: implement proper result handling
                    onCompletion.accept(true);
                }
            } catch (Exception e) {
                LOGGER.severe("Failed to show Element dialog: " + e.getMessage());
                if (onCompletion != null) {
                    onCompletion.accept(false);
                }
            }
        });
    }
    
    /**
     * Show Element edit dialog with any Window as parent.
     */
    public static void showElementDialog(Window parent, FDDINode node, Consumer<Boolean> onCompletion) {
        Platform.runLater(() -> {
            try {
                Stage parentStage = (parent instanceof Stage) ? (Stage) parent : null;
                FDDElementDialogFX elementDialog = new FDDElementDialogFX(parentStage, node);
                
                elementDialog.showAndWait();
                
                // Check if dialog was accepted (OK button pressed)
                if (onCompletion != null) {
                    // For now, assume accepted - TODO: implement proper result handling
                    onCompletion.accept(true);
                }
            } catch (Exception e) {
                LOGGER.severe("Failed to show Element dialog: " + e.getMessage());
                if (onCompletion != null) {
                    onCompletion.accept(false);
                }
            }
        });
    }
    
    /**
     * Unified dialog method that works with both Swing JFrame and JavaFX Stage.
     * This provides backward compatibility while supporting the new JavaFX application.
     */
    public static void showAboutDialogUnified(Object parent) {
        if (parent instanceof JFrame) {
            DialogBridge.showAboutDialog((JFrame) parent);
        } else if (parent instanceof Stage) {
            showAboutDialog((Stage) parent);
        } else if (parent instanceof Window) {
            showAboutDialog((Window) parent);
        } else {
            LOGGER.warning("Unsupported parent type for About dialog: " + 
                         (parent != null ? parent.getClass().getName() : "null"));
        }
    }
    
    /**
     * Unified element dialog method that works with both Swing JFrame and JavaFX Stage.
     */
    public static void showElementDialogUnified(Object parent, FDDINode node, Consumer<Boolean> onCompletion) {
        if (parent instanceof JFrame) {
            DialogBridge.showElementDialog((JFrame) parent, node, onCompletion);
        } else if (parent instanceof Stage) {
            showElementDialog((Stage) parent, node, onCompletion);
        } else if (parent instanceof Window) {
            showElementDialog((Window) parent, node, onCompletion);
        } else {
            LOGGER.warning("Unsupported parent type for Element dialog: " + 
                         (parent != null ? parent.getClass().getName() : "null"));
            if (onCompletion != null) {
                onCompletion.accept(false);
            }
        }
    }
}
