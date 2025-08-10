package net.sourceforge.fddtools.command;

import net.sourceforge.fddtools.model.FDDINode;

/** Adds a child to a parent node. */
public class AddChildCommand implements Command {
    private final FDDINode parent;
    private final FDDINode child;
    private boolean executed;

    public AddChildCommand(FDDINode parent, FDDINode child) {
        this.parent = parent;
        this.child = child;
    }

    @Override
    public void execute() {
        if (executed) return;
        parent.add(child);
    net.sourceforge.fddtools.state.ModelEventBus.get().publish(net.sourceforge.fddtools.state.ModelEventBus.EventType.TREE_STRUCTURE_CHANGED, parent);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) return;
        parent.removeChild(child);
    net.sourceforge.fddtools.state.ModelEventBus.get().publish(net.sourceforge.fddtools.state.ModelEventBus.EventType.TREE_STRUCTURE_CHANGED, parent);
        executed = false;
    }

    @Override
    public String description() { return "Add " + child.getClass().getSimpleName() + " to " + parent.getName(); }
}
