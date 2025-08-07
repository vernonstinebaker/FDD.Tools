package net.sourceforge.fddtools.ui.fx;

import net.sourceforge.fddtools.model.FDDINode;

/**
 * Interface for handling context menu actions and selection changes in the JavaFX tree.
 */
public interface FDDTreeContextMenuHandler {
    
    /**
     * Called when the tree selection changes.
     */
    void onSelectionChanged(FDDINode selectedNode);
    
    /**
     * Add a new program under the given node.
     */
    void addProgram(FDDINode parentNode);
    
    /**
     * Add a new project under the given node.
     */
    void addProject(FDDINode parentNode);
    
    /**
     * Add a new aspect under the given node.
     */
    void addAspect(FDDINode parentNode);
    
    /**
     * Add a new subject under the given node.
     */
    void addSubject(FDDINode parentNode);
    
    /**
     * Add a new activity under the given node.
     */
    void addActivity(FDDINode parentNode);
    
    /**
     * Add a new feature under the given node.
     */
    void addFeature(FDDINode parentNode);
    
    /**
     * Edit the given node.
     */
    void editNode(FDDINode node);
    
    /**
     * Delete the given node.
     */
    void deleteNode(FDDINode node);
}