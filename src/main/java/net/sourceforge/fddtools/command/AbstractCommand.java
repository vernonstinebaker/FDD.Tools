package net.sourceforge.fddtools.command;

import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.state.ModelEventBus;

/**
 * Abstract base class for commands that provides common event publishing functionality.
 * Eliminates redundant ModelEventBus calls across command implementations.
 */
public abstract class AbstractCommand implements Command {
    
    /**
     * Publishes a TREE_STRUCTURE_CHANGED event for the given node.
     * Use this when a command modifies the tree structure (add/remove/move nodes).
     */
    protected final void publishTreeStructureChanged(FDDINode node) {
        ModelEventBus.get().publish(ModelEventBus.EventType.TREE_STRUCTURE_CHANGED, node);
    }
    
    /**
     * Publishes a NODE_UPDATED event for the given node.
     * Use this when a command modifies node properties without affecting tree structure.
     */
    protected final void publishNodeUpdated(FDDINode node) {
        ModelEventBus.get().publish(ModelEventBus.EventType.NODE_UPDATED, node);
    }
}
