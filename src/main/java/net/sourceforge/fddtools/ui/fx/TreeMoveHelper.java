package net.sourceforge.fddtools.ui.fx;

import java.util.List;

/**
 * Pure list reordering helper used by {@link FDDTreeViewFX#updateAfterMove} to enable
 * unit testing of index normalization and append semantics without JavaFX {@code TreeItem}.
 * <p>Algorithm (mirrors prior inline logic):
 * <ul>
 *   <li>If moving within the same parent: clamp/normalize index (negative or >size -> size) and insert at that index.</li>
 *   <li>If moving to a different parent: clamp/normalize index (negative or >size -> append) and insert accordingly.</li>
 *   <li>Removal from old parent list always occurs first when present.</li>
 * </ul>
 */
final class TreeMoveHelper {
    private TreeMoveHelper() {}

    /**
     * Move an item from oldParentChildren to newParentChildren resolving the effective insertion index.
     * @param oldParentChildren list the item currently belongs to (may be same as newParentChildren or null for root)
     * @param newParentChildren target children list (must not be null)
     * @param item item to move
     * @param requestedIndex desired index (-1 or out-of-range => append)
     * @return effective insertion index in the target list after move
     */
    static <T> int move(List<T> oldParentChildren, List<T> newParentChildren, T item, int requestedIndex) {
        if (item == null || newParentChildren == null) return -1;
        if (oldParentChildren != null) oldParentChildren.remove(item); // safe even if not contained
        int size = newParentChildren.size();
        int effectiveIndex = requestedIndex;
        if (effectiveIndex < 0 || effectiveIndex > size) effectiveIndex = size; // append semantics
        newParentChildren.add(effectiveIndex, item);
        return effectiveIndex;
    }
}
