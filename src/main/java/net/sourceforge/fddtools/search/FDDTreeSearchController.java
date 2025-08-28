package net.sourceforge.fddtools.search;

import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.ui.fx.FDDTreeViewFX;
import javafx.scene.control.TreeItem;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Controls search operations and result navigation for the FDD tree.
 * Manages search state, highlighting, and navigation between matches.
 */
public class FDDTreeSearchController {
    
    private final FDDTreeViewFX treeView;
    private final FDDTreeSearchEngine searchEngine;
    
    // Search state
    private List<FDDTreeSearchEngine.SearchMatch> currentMatches = new ArrayList<>();
    private int currentMatchIndex = -1;
    private String currentQuery = "";
    
    // Visual state
    private final Set<TreeItem<FDDINode>> highlightedItems = new HashSet<>();
    private final Set<TreeItem<FDDINode>> expandedForSearch = new HashSet<>();
    
    /**
     * Listener interface for search events.
     */
    public interface SearchListener {
        void onSearchResults(String query, List<FDDTreeSearchEngine.SearchMatch> matches);
        void onCurrentMatchChanged(int matchIndex, int totalMatches);
        void onSearchCleared();
    }
    
    private SearchListener searchListener;
    
    public FDDTreeSearchController(FDDTreeViewFX treeView) {
        this.treeView = treeView;
        this.searchEngine = new FDDTreeSearchEngine();
    }
    
    public void setSearchListener(SearchListener listener) {
        this.searchListener = listener;
    }
    
    /**
     * Performs a new search with the given query.
     * @param query the search query string
     */
    public void search(String query) {
        clearCurrentSearch();
        
        if (query == null || query.trim().isEmpty()) {
            currentQuery = "";
            currentMatches.clear();
            currentMatchIndex = -1;
            notifySearchCleared();
            return;
        }
        
        currentQuery = query.trim();
        TreeItem<FDDINode> root = treeView.getRoot();
        
        if (root != null) {
            currentMatches = searchEngine.search(root, currentQuery);
            currentMatchIndex = currentMatches.isEmpty() ? -1 : 0;
            
            // Apply highlighting and expand tree paths
            highlightMatches();
            expandPathsToMatches();
            
            // Navigate to first match if any
            if (!currentMatches.isEmpty()) {
                navigateToCurrentMatch(false); // false = don't steal focus while typing
            }
            
            notifySearchResults();
        }
    }
    
    /**
     * Navigates to the next search match.
     * @return true if navigation occurred, false if no more matches
     */
    public boolean navigateToNext() {
        if (currentMatches.isEmpty()) return false;
        
        currentMatchIndex = (currentMatchIndex + 1) % currentMatches.size();
        navigateToCurrentMatch(true); // true = request focus for explicit navigation
        notifyCurrentMatchChanged();
        return true;
    }
    
    /**
     * Navigates to the previous search match.
     * @return true if navigation occurred, false if no matches
     */
    public boolean navigateToPrevious() {
        if (currentMatches.isEmpty()) return false;
        
        currentMatchIndex = currentMatchIndex <= 0 ? 
            currentMatches.size() - 1 : currentMatchIndex - 1;
        navigateToCurrentMatch(true); // true = request focus for explicit navigation
        notifyCurrentMatchChanged();
        return true;
    }
    
    /**
     * Clears the current search and removes all highlighting.
     */
    public void clearSearch() {
        clearCurrentSearch();
        currentQuery = "";
        currentMatches.clear();
        currentMatchIndex = -1;
        notifySearchCleared();
    }
    
    /**
     * Gets the current search query.
     */
    public String getCurrentQuery() {
        return currentQuery;
    }
    
    /**
     * Gets the current matches.
     */
    public List<FDDTreeSearchEngine.SearchMatch> getCurrentMatches() {
        return new ArrayList<>(currentMatches);
    }
    
    /**
     * Gets the current match index.
     */
    public int getCurrentMatchIndex() {
        return currentMatchIndex;
    }
    
    /**
     * Checks if an item is currently highlighted by search.
     */
    public boolean isHighlighted(TreeItem<FDDINode> item) {
        return highlightedItems.contains(item);
    }
    
    // Private methods
    
    private void clearCurrentSearch() {
        // Remove highlighting
        highlightedItems.clear();
        
        // Collapse items that were expanded for search
        for (TreeItem<FDDINode> item : expandedForSearch) {
            if (item != null) {
                item.setExpanded(false);
            }
        }
        expandedForSearch.clear();
        
        // Refresh tree view to update styling
        Platform.runLater(() -> {
            treeView.refresh();
        });
    }
    
    private void highlightMatches() {
        highlightedItems.clear();
        for (FDDTreeSearchEngine.SearchMatch match : currentMatches) {
            if (match.getTreeItem() != null) {
                highlightedItems.add(match.getTreeItem());
            }
        }
        
        // Refresh tree view to show highlighting
        Platform.runLater(() -> {
            treeView.refresh();
        });
    }
    
    private void expandPathsToMatches() {
        for (FDDTreeSearchEngine.SearchMatch match : currentMatches) {
            expandPathToItem(match.getTreeItem());
        }
    }
    
    private void expandPathToItem(TreeItem<FDDINode> item) {
        if (item == null) return;
        
        TreeItem<FDDINode> current = item.getParent();
        while (current != null) {
            if (!current.isExpanded()) {
                current.setExpanded(true);
                expandedForSearch.add(current);
            }
            current = current.getParent();
        }
    }
    
    private void navigateToCurrentMatch(boolean requestFocus) {
        if (currentMatchIndex >= 0 && currentMatchIndex < currentMatches.size()) {
            FDDTreeSearchEngine.SearchMatch match = currentMatches.get(currentMatchIndex);
            TreeItem<FDDINode> item = match.getTreeItem();
            
            if (item != null) {
                System.out.println("DEBUG: Navigating to match " + (currentMatchIndex + 1) + " of " + currentMatches.size());
                System.out.println("DEBUG: Target node: " + (item.getValue() != null ? item.getValue().getName() : "null"));
                
                Platform.runLater(() -> {
                    // Select and scroll to the item
                    System.out.println("DEBUG: Before selection - currently selected: " + 
                        (treeView.getSelectionModel().getSelectedItem() != null && 
                         treeView.getSelectionModel().getSelectedItem().getValue() != null ?
                         treeView.getSelectionModel().getSelectedItem().getValue().getName() : "null"));
                    
                    // Use the proper selectNode method instead of direct selection model access
                    treeView.selectNode(item.getValue());
                    
                    System.out.println("DEBUG: After selection - currently selected: " + 
                        (treeView.getSelectionModel().getSelectedItem() != null && 
                         treeView.getSelectionModel().getSelectedItem().getValue() != null ?
                         treeView.getSelectionModel().getSelectedItem().getValue().getName() : "null"));
                    
                    // Add explicit refresh to ensure visual update
                    treeView.refresh();
                    
                    // Only request focus for explicit navigation (F3, arrow buttons)
                    // Don't steal focus while user is typing in search field
                    if (requestFocus) {
                        treeView.requestFocus();
                    }
                });
            }
        }
    }
    
    // Notification methods
    
    private void notifySearchResults() {
        if (searchListener != null) {
            searchListener.onSearchResults(currentQuery, currentMatches);
        }
        notifyCurrentMatchChanged();
    }
    
    private void notifyCurrentMatchChanged() {
        if (searchListener != null) {
            searchListener.onCurrentMatchChanged(currentMatchIndex, currentMatches.size());
        }
    }
    
    private void notifySearchCleared() {
        if (searchListener != null) {
            searchListener.onSearchCleared();
        }
    }
}
