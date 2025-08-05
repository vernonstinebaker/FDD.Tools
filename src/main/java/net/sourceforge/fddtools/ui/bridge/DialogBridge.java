package net.sourceforge.fddtools.ui.bridge;

import javafx.application.Platform;
import net.sourceforge.fddtools.ui.fx.AboutDialogFX;
import javax.swing.*;


import java.util.concurrent.CountDownLatch;
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
    public static void showAboutDialog(JFrame parent) {        System.out.println("DialogBridge.showAboutDialog() called");
                // First, ensure JavaFX is initialized
        SwingFXBridge.initializeJavaFX();
        
        // Use a CountDownLatch to wait for the dialog to close
        CountDownLatch latch = new CountDownLatch(1);
        
        // We need to ensure this runs on the JavaFX thread
        Platform.runLater(() -> {            System.out.println("Running on JavaFX thread");            try {
                // Create and show the About dialog
                AboutDialogFX aboutDialog = new AboutDialogFX(null);                System.out.println("AboutDialogFX created");                
                // If we have a parent frame, position relative to it
                if (parent != null && parent.isShowing()) {
                    // Position the dialog relative to the parent frame
                    aboutDialog.setX(parent.getX() + (parent.getWidth() - 550) / 2);
                    aboutDialog.setY(parent.getY() + (parent.getHeight() - 450) / 2);
                }
                                System.out.println("Showing AboutDialogFX...");                // Show the dialog and wait for it to close
                aboutDialog.showAndWait();                System.out.println("AboutDialogFX closed");                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error showing JavaFX About dialog", e);
                e.printStackTrace();
                // Fallback to showing error in Swing
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(parent, 
                        "Error showing About dialog: " + e.getMessage() + "\n" + e.getClass().getName(),
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                });
            } finally {
                latch.countDown();
            }
        });
        
        // Wait for the dialog to close
        try {            System.out.println("Waiting for dialog to close...");            latch.await();            System.out.println("Dialog closed, returning");        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Interrupted while waiting for About dialog", e);
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