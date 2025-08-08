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
     * Show About dialog and invoke centering callback when shown.
     */
    public static void showAboutDialog(Stage parent, java.util.function.Consumer<Stage> centeringCallback) {
        Platform.runLater(() -> {
            try {
                AboutDialogFX aboutDialog = new AboutDialogFX(parent);
                if (centeringCallback != null) {
                    aboutDialog.setOnShown(e -> centeringCallback.accept(aboutDialog));
                }
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
     * Show Element dialog with optional centering callback.
     */
    public static void showElementDialog(Stage parent, FDDINode node, Consumer<Boolean> onCompletion, java.util.function.Consumer<Stage> centeringCallback) {
        Platform.runLater(() -> {
            try {
                FDDElementDialogFX elementDialog = new FDDElementDialogFX(parent, node);
                if (centeringCallback != null) {
                    elementDialog.setOnShown(e -> centeringCallback.accept(elementDialog));
                }
                elementDialog.showAndWait();
                if (onCompletion != null) {
                    onCompletion.accept(true);
                }
            } catch (Exception e) {
                LOGGER.severe("Failed to show Element dialog: " + e.getMessage());
                if (onCompletion != null) onCompletion.accept(false);
            }
        });
    }
                
                
    
    /**
     * Unified dialog method that works with both Swing JFrame and JavaFX Stage.
     * This provides backward compatibility while supporting the new JavaFX application.
     */
    // Legacy Swing bridge removed; JavaFX-only implementations retained
}
