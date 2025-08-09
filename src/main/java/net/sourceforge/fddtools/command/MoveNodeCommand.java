package net.sourceforge.fddtools.command;

import net.sourceforge.fddtools.model.FDDINode;

/** Reparents a node to a new parent (append ordering). Undo restores original parent. */
public class MoveNodeCommand implements Command {
    private final FDDINode node;
    private final FDDINode originalParent;
    private final FDDINode newParent;
    private boolean executed;

    public MoveNodeCommand(FDDINode node, FDDINode newParent) {
        this.node = node;
        this.originalParent = (FDDINode) node.getParentNode();
        this.newParent = newParent;
    }

    @Override public void execute() {
        if (executed) return;
        if (newParent == null || node == null || newParent == originalParent) return;
        if (originalParent != null) originalParent.removeChild(node);
        newParent.addChild(node);
        executed = true;
    }

    @Override public void undo() {
        if (!executed) return;
        if (newParent != null) newParent.removeChild(node);
        if (originalParent != null) originalParent.addChild(node);
        executed = false;
    }

    @Override public String description() { return "Move " + node.getName() + " to " + (newParent!=null?newParent.getName():"<null>"); }
}
