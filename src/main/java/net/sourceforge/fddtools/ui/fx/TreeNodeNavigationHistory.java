package net.sourceforge.fddtools.ui.fx;

import net.sourceforge.fddtools.model.FDDINode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Navigation history system for tree node selection, providing browser-like
 * forward/back navigation capabilities.
 */
public class TreeNodeNavigationHistory {
    
    private final List<FDDINode> history = new ArrayList<>();
    private int currentIndex = -1;
    private boolean suppressRecording = false;
    
    private NavigationListener listener;
    
    public interface NavigationListener {
        /**
         * Called when navigation buttons should be enabled/disabled
         * @param canGoBack true if back navigation is possible
         * @param canGoForward true if forward navigation is possible
         */
        void onNavigationStateChanged(boolean canGoBack, boolean canGoForward);
        
        /**
         * Called when programmatic navigation occurs (back/forward)
         * @param node the node to navigate to
         */
        void onNavigateTo(FDDINode node);
    }
    
    public TreeNodeNavigationHistory() {
        // Default constructor
    }
    
    public void setNavigationListener(NavigationListener listener) {
        this.listener = listener;
    }
    
    /**
     * Records a node selection in the navigation history.
     * @param node the node that was selected
     */
    public void recordSelection(FDDINode node) {
        if (suppressRecording || node == null) {
            return;
        }
        
        // Don't record duplicate consecutive selections
        if (!history.isEmpty() && Objects.equals(getCurrentNode(), node)) {
            return;
        }
        
        // If we're not at the end of history, remove everything after current position
        if (currentIndex < history.size() - 1) {
            history.subList(currentIndex + 1, history.size()).clear();
        }
        
        // Add the new node
        history.add(node);
        currentIndex = history.size() - 1;
        
        // Limit history size to prevent memory growth
        if (history.size() > 100) {
            history.remove(0);
            currentIndex--;
        }
        
        notifyNavigationStateChanged();
    }
    
    /**
     * Navigate back to the previous node in history.
     * @return true if navigation occurred, false if at beginning
     */
    public boolean goBack() {
        if (!canGoBack()) {
            return false;
        }
        
        currentIndex--;
        FDDINode targetNode = history.get(currentIndex);
        
        suppressRecording = true;
        try {
            if (listener != null) {
                listener.onNavigateTo(targetNode);
            }
        } finally {
            suppressRecording = false;
        }
        
        notifyNavigationStateChanged();
        return true;
    }
    
    /**
     * Navigate forward to the next node in history.
     * @return true if navigation occurred, false if at end
     */
    public boolean goForward() {
        if (!canGoForward()) {
            return false;
        }
        
        currentIndex++;
        FDDINode targetNode = history.get(currentIndex);
        
        suppressRecording = true;
        try {
            if (listener != null) {
                listener.onNavigateTo(targetNode);
            }
        } finally {
            suppressRecording = false;
        }
        
        notifyNavigationStateChanged();
        return true;
    }
    
    /**
     * @return true if back navigation is possible
     */
    public boolean canGoBack() {
        return currentIndex > 0;
    }
    
    /**
     * @return true if forward navigation is possible
     */
    public boolean canGoForward() {
        return currentIndex >= 0 && currentIndex < history.size() - 1;
    }
    
    /**
     * @return the currently selected node in history, or null if none
     */
    public FDDINode getCurrentNode() {
        if (currentIndex >= 0 && currentIndex < history.size()) {
            return history.get(currentIndex);
        }
        return null;
    }
    
    /**
     * Clears all navigation history (e.g., when opening a new project).
     */
    public void clear() {
        history.clear();
        currentIndex = -1;
        notifyNavigationStateChanged();
    }
    
    /**
     * @return the number of items in the navigation history
     */
    public int size() {
        return history.size();
    }
    
    /**
     * @return true if the history is empty
     */
    public boolean isEmpty() {
        return history.isEmpty();
    }
    
    private void notifyNavigationStateChanged() {
        if (listener != null) {
            listener.onNavigationStateChanged(canGoBack(), canGoForward());
        }
    }
}
