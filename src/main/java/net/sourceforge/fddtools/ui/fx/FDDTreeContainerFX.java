package net.sourceforge.fddtools.ui.fx;

import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import net.sourceforge.fddtools.model.FDDINode;

/**
 * Container that holds the tree view with its action panel below,
 * similar to how the canvas has its own action bar.
 * This replaces the global status bar approach.
 */
public class FDDTreeContainerFX extends VBox {
    
    private final FDDTreeViewFX treeView;
    private final FDDActionPanelFX actionPanel;
    
    /**
     * Creates a tree container with the tree view and action panel.
     * @param enableProgramBusinessLogic whether to enable program business logic in the tree
     */
    public FDDTreeContainerFX(boolean enableProgramBusinessLogic) {
        super(0); // No spacing between tree and action panel
        
        treeView = new FDDTreeViewFX(enableProgramBusinessLogic);
        actionPanel = new FDDActionPanelFX();
        
        // Tree view takes all available space, action panel stays fixed height
        VBox.setVgrow(treeView, Priority.ALWAYS);
        VBox.setVgrow(actionPanel, Priority.NEVER);
        
        getChildren().addAll(treeView, actionPanel);
        getStyleClass().add("fdd-tree-container");
        
        // Set minimum and preferred widths to match original tree sizing
        setMinWidth(140);
        setPrefWidth(220);
    }
    
    /**
     * Gets the contained tree view.
     * @return the tree view component
     */
    public FDDTreeViewFX getTreeView() {
        return treeView;
    }
    
    /**
     * Gets the contained action panel.
     * @return the action panel component
     */
    public FDDActionPanelFX getActionPanel() {
        return actionPanel;
    }
    
    /**
     * Sets the action handler for the action panel.
     * @param handler the action handler implementation
     */
    public void setActionHandler(FDDActionPanelFX.FDDActionHandler handler) {
        actionPanel.setActionHandler(handler);
    }
    
    /**
     * Convenience method to populate the tree.
     * @param rootNode the root node to populate the tree with
     */
    public void populateTree(FDDINode rootNode) {
        treeView.populateTree(rootNode);
    }
    
    /**
     * Convenience method to set context menu handler.
     * @param handler the context menu handler
     */
    public void setContextMenuHandler(FDDTreeContextMenuHandler handler) {
        treeView.setContextMenuHandler(handler);
    }
    
    /**
     * Convenience method to select a node in the tree.
     * @param node the node to select
     * @param scrollTo whether to scroll to the selected node
     */
    public void selectNode(FDDINode node, boolean scrollTo) {
        treeView.selectNode(node, scrollTo);
    }
}
