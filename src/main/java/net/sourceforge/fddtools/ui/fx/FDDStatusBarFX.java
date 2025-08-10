package net.sourceforge.fddtools.ui.fx;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import net.sourceforge.fddtools.command.CommandStack;

/**
 * Encapsulated status bar with action panel and undo/redo status labels.
 */
public class FDDStatusBarFX extends VBox {
    private final FDDActionPanelFX actionPanel = new FDDActionPanelFX();
    private final Label statusLabel = new Label("Ready");
    private final Label undoStatusLabel = new Label("");
    private final Label redoStatusLabel = new Label("");

    public FDDStatusBarFX() {
        setStyle("-fx-background-color: #f0f0f0; -fx-padding: 2px 5px;");
        statusLabel.setStyle("-fx-font-size: 12px;");
        undoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
        redoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
        VBox undoRedoBox = new VBox(2, undoStatusLabel, redoStatusLabel);
        undoRedoBox.setPadding(new Insets(2,0,0,4));
        getChildren().addAll(actionPanel, statusLabel, undoRedoBox);
    }

    public void setActionHandler(FDDActionPanelFX.FDDActionHandler handler) {
        actionPanel.setActionHandler(handler);
    }

    public void setStatus(String text) { statusLabel.setText(text); }

    public void updateUndoRedo(CommandStack stack) {
        String undoDesc = stack.peekUndoDescription();
        String redoDesc = stack.peekRedoDescription();
        undoStatusLabel.setText(undoDesc == null ? "" : "Undo: " + undoDesc);
        redoStatusLabel.setText(redoDesc == null ? "" : "Redo: " + redoDesc);
    }
}
