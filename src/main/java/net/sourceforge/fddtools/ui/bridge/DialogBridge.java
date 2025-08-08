package net.sourceforge.fddtools.ui.bridge;

import javafx.application.Platform;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.ui.fx.AboutDialogFX;
import net.sourceforge.fddtools.ui.fx.FDDElementDialogFX;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.function.Consumer;
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
        
        // We need to ensure this runs on the JavaFX thread
        Platform.runLater(() -> {
            System.out.println("Running on JavaFX thread");
            try {
                // Create and show the About dialog
                AboutDialogFX aboutDialog = new AboutDialogFX(null);
                System.out.println("AboutDialogFX created");
                // Center relative to primary screen (Stage centering handled by layout once owner set if needed)
                aboutDialog.centerOnScreen();
                
                System.out.println("Showing AboutDialogFX...");
                // Show the dialog and wait for it to close
                aboutDialog.showAndWait();
                System.out.println("AboutDialogFX closed");
                
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
            }
        });
    }    
    /**
     * Shows the JavaFX Element dialog asynchronously and executes a callback on completion.
     * This method does not block the calling thread.
     *
     * @param parent The parent Swing frame
     * @param node The FDDINode to be edited
     * @param onCompletion A Consumer callback that receives the result (true for OK, false for Cancel)
     */
    public static void showElementDialog(JFrame parent, FDDINode node, Consumer<Boolean> onCompletion) {
        System.out.println("DialogBridge.showElementDialog() called for node: " + node.getClass().getSimpleName());
        
        // First, ensure JavaFX is initialized
        SwingFXBridge.initializeJavaFX();
        
        Platform.runLater(() -> {
            System.out.println("Running on JavaFX thread");
            boolean result = false;
            try {
                FDDElementDialogFX elementDialog = new FDDElementDialogFX(null, node);
                elementDialog.centerOnScreen();
                elementDialog.showAndWait();
                result = elementDialog.getAccept();
            } catch (Exception e) {
                System.err.println("ERROR in JavaFX Element dialog: " + e.getMessage());
                e.printStackTrace();
            } finally {
                final boolean finalResult = result;
                SwingUtilities.invokeLater(() -> onCompletion.accept(finalResult));
            }
        });
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