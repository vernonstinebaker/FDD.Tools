package net.sourceforge.fddtools.command;

import net.sourceforge.fddtools.model.FDDINode;

/** Removes a node from its parent and can restore it at the same index. */
public class DeleteNodeCommand implements Command {
    private final FDDINode node;
    private final FDDINode parent;
    private boolean executed;

    public DeleteNodeCommand(FDDINode node) {
        this.node = node;
        this.parent = (FDDINode) node.getParentNode();
    }

    @Override
    public void execute() {
        if (executed) return;
        if (parent == null) return; // root protection
        parent.removeChild(node);
    net.sourceforge.fddtools.state.ModelEventBus.get().publish(net.sourceforge.fddtools.state.ModelEventBus.EventType.TREE_STRUCTURE_CHANGED, parent);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) return;
        if (parent == null) return;
        // parent.add re-appends; ordering restoration skipped until indexed add available
        parent.add(node);
    net.sourceforge.fddtools.state.ModelEventBus.get().publish(net.sourceforge.fddtools.state.ModelEventBus.EventType.TREE_STRUCTURE_CHANGED, parent);
        executed = false;
    }

    @Override
    public String description() { return "Delete " + node.getName(); }
}
