package net.sourceforge.fddtools.model;

import java.util.List;

/**
 * Swing-independent minimal tree node contract used by JavaFX components.
 *
 * This will allow removal of javax.swing.tree.* from the core model classes
 * while providing the capabilities required by the JavaFX tree, canvas, and
 * dialogs (navigation, child access, and naming).
 *
 * Migration Steps:
 * 1. Introduce this interface (non-breaking); FDDINode will implement it.
 * 2. Refactor JavaFX UI code to depend only on FDDTreeNode instead of
 *    javax.swing.tree.TreeNode / MutableTreeNode.
 * 3. Remove Swing-specific interfaces and imports from FDDINode and subclasses.
 * 4. Delete legacy Swing UI classes that require the old interfaces.
 */
public interface FDDTreeNode {
    /** @return human-readable name (used for tree cell text, etc.). */
    String getName();

    /** @return parent node or null if root. */
    FDDTreeNode getParentNode();

    /** Set parent node; implementation should maintain bidirectional integrity. */
    void setParentNode(FDDTreeNode parent);

    /** @return immutable (or unmodifiable) live view of children. */
    List<? extends FDDTreeNode> getChildren();

    /** Add a single child (implementation should set its parent). */
    void addChild(FDDTreeNode child);

    /** Remove a single child (implementation should clear its parent). */
    void removeChild(FDDTreeNode child);

    /** @return true if node has no children. */
    boolean isLeaf();

    /** Convenience default: depth-first path from root to this node. */
    default List<FDDTreeNode> buildPath() {
        java.util.ArrayList<FDDTreeNode> list = new java.util.ArrayList<>();
        FDDTreeNode current = this;
        while (current != null) {
            list.add(current);
            current = current.getParentNode();
        }
        java.util.Collections.reverse(list);
        return list;
    }
}
