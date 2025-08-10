package net.sourceforge.fddtools.command;

import net.sourceforge.fddtools.model.FDDINode;

/** Reparents a node to a new parent (append ordering). Undo restores original parent. */
public class MoveNodeCommand implements Command {
    private final FDDINode node;
    private final FDDINode originalParent;
    private final FDDINode newParent;
    private final Integer newIndex; // null => append
    private boolean executed;
    private int originalIndex = -1;

    public MoveNodeCommand(FDDINode node, FDDINode newParent) { this(node, newParent, null); }

    public MoveNodeCommand(FDDINode node, FDDINode newParent, Integer newIndex) {
        this.node = node;
        this.originalParent = (FDDINode) node.getParentNode();
        this.newParent = newParent;
        this.newIndex = newIndex;
    }

    @Override public void execute() {
        if (executed) return;
        if (newParent == null || node == null || newParent == originalParent && newIndex == null) return;
        // capture original index within its parent list (if same parent + move by index)
        if (originalParent != null) {
            var children = originalParent.getChildren();
            originalIndex = children.indexOf(node);
        }
        if (originalParent != null) originalParent.removeChild(node);
        if (newIndex != null && newParent != null) {
            newParent.insertChildAt(node, newIndex);
        } else {
            if (newParent != null) newParent.addChild(node);
        }
        executed = true;
    }

    @Override public void undo() {
        if (!executed) return;
        if (newParent != null) newParent.removeChild(node);
        if (originalParent != null) {
            if (originalIndex >= 0) {
                originalParent.insertChildAt(node, originalIndex);
            } else {
                originalParent.addChild(node);
            }
        }
        executed = false;
    }

    @Override public String description() {
        return "Move " + node.getName() + " to " + (newParent!=null?newParent.getName():"<null>") + (newIndex!=null? ("@"+newIndex):"");
    }
}
