package net.sourceforge.fddtools.command;

import java.util.logging.Logger;
import net.sourceforge.fddtools.state.ModelState;

/**
 * Centralizes execution of Commands so undo/redo availability and dirty state
 * are updated uniformly and future listeners (e.g., status bar) can hook in.
 */
public final class CommandExecutionService {
    private static final Logger LOGGER = Logger.getLogger(CommandExecutionService.class.getName());
    private static final CommandExecutionService INSTANCE = new CommandExecutionService();
    public static CommandExecutionService getInstance() { return INSTANCE; }

    private final CommandStack stack = new CommandStack();

    private CommandExecutionService() {}

    public CommandStack getStack() { return stack; }

    public void execute(Command command) {
        if (command == null) return;
        stack.execute(command);
        afterMutation();
        try {
            LOGGER.fine("Executed command: " + command.description());
        } catch (Exception ignored) { }
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
    }
}
