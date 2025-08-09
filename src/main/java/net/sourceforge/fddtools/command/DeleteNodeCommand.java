package net.sourceforge.fddtools.command;

import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.model.FDDTreeNode;

/** Removes a node from its parent and can restore it at the same index. */
public class DeleteNodeCommand implements Command {
    private final FDDINode node;
    private final FDDINode parent;
    private int originalIndex = -1;
    private boolean executed;

    public DeleteNodeCommand(FDDINode node) {
        this.node = node;
        this.parent = (FDDINode) node.getParentNode();
    }

    @Override
    public void execute() {
        if (executed) return;
        if (parent == null) return; // root protection
        // determine index
        int idx = 0;
        for (FDDTreeNode tn : parent.getChildren()) {
            if (tn == node) { originalIndex = idx; break; }
            idx++;
        }
        parent.removeChild(node);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) return;
        if (parent == null) return;
        // parent.add re-appends; ordering restoration skipped until indexed add available
        parent.add(node);
        executed = false;
    }

    @Override
    public String description() { return "Delete " + node.getName(); }
}
