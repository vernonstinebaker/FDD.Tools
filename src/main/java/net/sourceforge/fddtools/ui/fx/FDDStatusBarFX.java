package net.sourceforge.fddtools.ui.fx;

import javafx.scene.layout.VBox;
import net.sourceforge.fddtools.command.CommandStack;

/**
 * Encapsulated status bar with action panel and undo/redo status labels.
 */
public class FDDStatusBarFX extends VBox {
    private final FDDActionPanelFX actionPanel = new FDDActionPanelFX();

    public FDDStatusBarFX() {
    // Minimal styling & footprint: just the action panel (no status / undo labels)
    setStyle("-fx-padding: 2 4 2 4; -fx-background-color: transparent;");
    getChildren().add(actionPanel);
    }

    public void setActionHandler(FDDActionPanelFX.FDDActionHandler handler) {
        actionPanel.setActionHandler(handler);
    }

    // Undo/redo status display removed; retain method for call sites (no-op now)
    public void updateUndoRedo(CommandStack stack) { /* intentionally no-op */ }
}
