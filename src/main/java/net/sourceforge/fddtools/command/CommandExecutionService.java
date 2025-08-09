package net.sourceforge.fddtools.command;

import net.sourceforge.fddtools.state.ModelState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralizes execution of Commands so undo/redo availability and dirty state
 * are updated uniformly and future listeners (e.g., status bar) can hook in.
 */
public final class CommandExecutionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutionService.class);
    private static final CommandExecutionService INSTANCE = new CommandExecutionService();
    public static CommandExecutionService getInstance() { return INSTANCE; }

    private final CommandStack stack = new CommandStack();

    private CommandExecutionService() {}

    public CommandStack getStack() { return stack; }

    public void execute(Command command) {
    if (command == null) return;
    stack.execute(command);
    afterMutation();
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Executed command: {}", command.description());
    }

    public void undo() {
        if (stack.canUndo()) {
            stack.undo();
            afterMutation();
        }
    }

    public void redo() {
        if (stack.canRedo()) {
            stack.redo();
            afterMutation();
        }
    }

    private void afterMutation() {
        ModelState ms = ModelState.getInstance();
        ms.setUndoAvailable(stack.canUndo());
        ms.setRedoAvailable(stack.canRedo());
        ms.setDirty(true); // any command mutation marks model dirty
    ms.setNextUndoDescription(stack.canUndo() ? stack.peekUndoDescription() : "");
    ms.setNextRedoDescription(stack.canRedo() ? stack.peekRedoDescription() : "");
    }
}
