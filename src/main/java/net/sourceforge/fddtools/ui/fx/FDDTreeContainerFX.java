package net.sourceforge.fddtools.ui.fx;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.search.FDDTreeSearchController;

/**
 * Container that holds the tree view with search toolbar and action panel,
 * similar to how the canvas has its own action bar.
 * This replaces the global status bar approach and adds integrated search.
 */
public class FDDTreeContainerFX extends VBox {
    
    private final HBox searchToolbar;
    private final FDDTreeSearchUI searchUI;
    private final FDDTreeViewFX treeView;
    private final FDDActionPanelFX actionPanel;
    private final FDDTreeSearchController searchController;
    
    /**
     * Creates a tree container with search, tree view, and action panel.
     * @param enableProgramBusinessLogic whether to enable program business logic in the tree
     */
    public FDDTreeContainerFX(boolean enableProgramBusinessLogic) {
        super(0); // No spacing between components
        
        // Initialize components
        treeView = new FDDTreeViewFX(enableProgramBusinessLogic);
        actionPanel = new FDDActionPanelFX();
        searchUI = new FDDTreeSearchUI();
        searchController = new FDDTreeSearchController(treeView);
        
        // Create search toolbar
        searchToolbar = new HBox();
        searchToolbar.getStyleClass().add("fdd-toolbar");
        searchToolbar.setPadding(new Insets(4));
        searchToolbar.getChildren().add(searchUI);
        
        // Connect search UI to controller
        searchUI.setSearchController(searchController);
        
        // Connect search controller to tree view for highlighting
        treeView.setSearchController(searchController);
        
        // Set growth priorities
        VBox.setVgrow(searchToolbar, Priority.NEVER);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        VBox.setVgrow(actionPanel, Priority.NEVER);
        
        // Add all components: search toolbar, tree view, action panel
        getChildren().addAll(searchToolbar, treeView, actionPanel);
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
    
    /**
     * Gets the search controller for advanced search operations.
     * @return the search controller
     */
    public FDDTreeSearchController getSearchController() {
        return searchController;
    }
    
    /**
     * Gets the search UI component.
     * @return the search UI component
     */
    public FDDTreeSearchUI getSearchUI() {
        return searchUI;
    }
    
    /**
     * Focuses the search field for immediate text entry.
     */
    public void focusSearch() {
        searchUI.getSearchField().requestFocus();
    }
    
    /**
     * Clears the current search and removes highlighting.
     */
    public void clearSearch() {
        searchController.clearSearch();
    }
}
