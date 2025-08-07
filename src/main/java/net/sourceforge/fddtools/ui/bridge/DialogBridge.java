package net.sourceforge.fddtools.ui.bridge;

import javafx.application.Platform;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.ui.fx.AboutDialogFX;
import net.sourceforge.fddtools.ui.fx.FDDElementDialogFX;
import javax.swing.*;


import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bridge class to show JavaFX dialogs from Swing code.
 * This allows gradual migration of dialogs from Swing to JavaFX.
 */
public class DialogBridge {
    private static final Logger LOGGER = Logger.getLogger(DialogBridge.class.getName());
    
    /**
     * Shows the JavaFX About dialog from Swing code.
     * 
     * @param parent The parent Swing frame (can be null)
     */
    public static void showAboutDialog(JFrame parent) {
        System.out.println("DialogBridge.showAboutDialog() called");
        
        // First, ensure JavaFX is initialized
        SwingFXBridge.initializeJavaFX();
        
        // Check if we're already on the JavaFX thread
        if (Platform.isFxApplicationThread()) {
            System.out.println("Already on JavaFX thread, showing dialog directly");
            try {
                AboutDialogFX aboutDialog = new AboutDialogFX(null);
                if (parent != null && parent.isShowing()) {
                    aboutDialog.setX(parent.getX() + (parent.getWidth() - 550) / 2);
                    aboutDialog.setY(parent.getY() + (parent.getHeight() - 450) / 2);
                }
                aboutDialog.showAndWait();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error showing JavaFX About dialog", e);
            }
            return;
        }
        
        // Use FutureTask to avoid deadlock with Swing EDT
        java.util.concurrent.FutureTask<Void> futureTask = new java.util.concurrent.FutureTask<>(() -> {
            System.out.println("Running on JavaFX thread via FutureTask");
            try {
                AboutDialogFX aboutDialog = new AboutDialogFX(null);
                if (parent != null && parent.isShowing()) {
                    aboutDialog.setX(parent.getX() + (parent.getWidth() - 550) / 2);
                    aboutDialog.setY(parent.getY() + (parent.getHeight() - 450) / 2);
                }
                aboutDialog.showAndWait();
            } catch (Exception e) {
                System.err.println("ERROR in JavaFX About dialog: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        });
        
        System.out.println("Submitting FutureTask to Platform.runLater...");
        Platform.runLater(futureTask);
        
        try {
            System.out.println("Waiting for FutureTask to complete...");
            futureTask.get();
            System.out.println("FutureTask completed");
        } catch (Exception e) {
            System.err.println("Exception waiting for dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }    
    /**
     * Shows the JavaFX FDD Element dialog from Swing code.
     * 
     * @param parent The parent Swing frame (can be null)
     * @param node The FDDINode to edit
     * @return true if the user clicked OK, false if cancelled
     */
    public static boolean showElementDialog(JFrame parent, FDDINode node) {
        System.out.println("DialogBridge.showElementDialog() called for node: " + node.getClass().getSimpleName());
        System.out.println("JavaFX initialized: " + SwingFXBridge.isJavaFXInitialized());
        System.out.println("Platform implicit exit: " + Platform.isImplicitExit());
        
        // First, ensure JavaFX is initialized
        SwingFXBridge.initializeJavaFX();
        
        // Check if we're already on the JavaFX thread
        if (Platform.isFxApplicationThread()) {
            System.out.println("Already on JavaFX thread, showing dialog directly");
            try {
                FDDElementDialogFX elementDialog = new FDDElementDialogFX(null, node);
                if (parent != null && parent.isShowing()) {
                    elementDialog.setX(parent.getX() + (parent.getWidth() - 600) / 2);
                    elementDialog.setY(parent.getY() + (parent.getHeight() - 500) / 2);
                }
                elementDialog.showAndWait();
                return elementDialog.getAccept();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error showing JavaFX Element dialog", e);
                return false;
            }
        }
        
        // Use FutureTask to avoid deadlock with Swing EDT
        java.util.concurrent.FutureTask<Boolean> futureTask = new java.util.concurrent.FutureTask<>(() -> {
            System.out.println("Running on JavaFX thread via FutureTask");
            try {
                FDDElementDialogFX elementDialog = new FDDElementDialogFX(null, node);
                if (parent != null && parent.isShowing()) {
                    elementDialog.setX(parent.getX() + (parent.getWidth() - 600) / 2);
                    elementDialog.setY(parent.getY() + (parent.getHeight() - 500) / 2);
                }
                elementDialog.showAndWait();
                return elementDialog.getAccept();
            } catch (Exception e) {
                System.err.println("ERROR in JavaFX Element dialog: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
        
        System.out.println("Submitting FutureTask to Platform.runLater...");
        Platform.runLater(futureTask);
        
        try {
            System.out.println("Waiting for FutureTask to complete...");
            Boolean result = futureTask.get();
            System.out.println("FutureTask completed with result: " + result);
            return result != null ? result : false;
        } catch (Exception e) {
            System.err.println("Exception waiting for dialog: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }    
    /**
     * Shows a JavaFX alert dialog from Swing code.
     * 
     * @param title Dialog title
     * @param header Header text (can be null)
     * @param content Content text
     * @param alertType Type of alert (ERROR, WARNING, INFORMATION, CONFIRMATION)
     */
    public static void showAlert(String title, String header, String content, 
                                javafx.scene.control.Alert.AlertType alertType) {
        SwingFXBridge.initializeJavaFX();
        
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}