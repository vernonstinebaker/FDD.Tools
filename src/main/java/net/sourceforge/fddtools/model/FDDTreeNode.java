package net.sourceforge.fddtools.model;

import java.util.List;

/**
 * Minimal, Swing-independent tree node contract consumed exclusively by the
 * JavaFX presentation layer (tree, canvas, dialogs). All former dependencies
 * on {@code javax.swing.tree.TreeNode} / {@code MutableTreeNode} have been
 * removed from the domain model.
 * <p>
 * Migration summary (COMPLETE):
 * <ul>
 *   <li>Interface introduced to decouple model from Swing.</li>
 *   <li>JavaFX UI refactored to consume {@link FDDTreeNode} only.</li>
 *   <li>All Swing imports and adapters eliminated from model & UI.</li>
 *   <li>Legacy Swing UI classes removed from the codebase.</li>
 * </ul>
 * Remaining historical references only exist as resource bundle keys (for
 * backward-compatible i18n) and can be renamed in a later, non-functional
 * cleanup if desired.
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

    /** Insert child at specific index (default falls back to append). Implementers override for ordering support. */
    default void insertChildAt(FDDTreeNode child, int index) { addChild(child); }

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
