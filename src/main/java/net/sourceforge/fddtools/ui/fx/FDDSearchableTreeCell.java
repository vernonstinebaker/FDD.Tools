package net.sourceforge.fddtools.ui.fx;

import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.search.FDDTreeSearchController;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

/**
 * Custom tree cell that supports search highlighting.
 * Adds CSS classes based on search match state.
 */
public class FDDSearchableTreeCell extends TreeCell<FDDINode> {
    
    private FDDTreeSearchController searchController;
    
    public FDDSearchableTreeCell() {
        super();
    }
    
    /**
     * Sets the search controller for highlighting support.
     * @param searchController the search controller to use
     */
    public void setSearchController(FDDTreeSearchController searchController) {
        this.searchController = searchController;
    }
    
    @Override
    protected void updateItem(FDDINode item, boolean empty) {
        super.updateItem(item, empty);
        
        // Clear previous search-related style classes
        getStyleClass().removeAll("search-highlight", "search-current-match");
        
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.getName());
            setGraphic(null);
            
            // Apply search highlighting if applicable
            if (searchController != null) {
                TreeItem<FDDINode> treeItem = getTreeItem();
                if (treeItem != null && searchController.isHighlighted(treeItem)) {
                    getStyleClass().add("search-highlight");
                    
                    // Check if this is the current match
                    int currentIndex = searchController.getCurrentMatchIndex();
                    if (currentIndex >= 0 && currentIndex < searchController.getCurrentMatches().size()) {
                        TreeItem<FDDINode> currentMatch = searchController.getCurrentMatches()
                            .get(currentIndex).getTreeItem();
                        if (treeItem == currentMatch) {
                            getStyleClass().add("search-current-match");
                        }
                    }
                }
            }
        }
    }
}
