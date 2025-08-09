package net.sourceforge.fddtools.command;

import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.service.LoggingService;
import net.sourceforge.fddtools.model.FDDINode;
import java.util.HashMap;
import java.util.Map;
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
        Map<String,String> ctx = buildContext("execute:" + command.description());
        LoggingService.getInstance().withContext(ctx, () -> {
            stack.execute(command);
            afterMutation();
            if (LOGGER.isDebugEnabled()) LOGGER.debug("Executed command: {}", command.description());
            LoggingService.getInstance().audit("commandExecute", ctx, command::description);
        });
    }

    public void undo() {
        if (stack.canUndo()) {
            Map<String,String> ctx = buildContext("undo:" + stack.peekUndoDescription());
            LoggingService.getInstance().withContext(ctx, () -> {
                stack.undo();
                afterMutation();
                if (LOGGER.isDebugEnabled()) LOGGER.debug("Undid command: {}", ctx.get("action"));
                LoggingService.getInstance().audit("commandUndo", ctx, () -> stack.peekRedoDescription());
            });
        }
    }

    public void redo() {
        if (stack.canRedo()) {
            Map<String,String> ctx = buildContext("redo:" + stack.peekRedoDescription());
            LoggingService.getInstance().withContext(ctx, () -> {
                stack.redo();
                afterMutation();
                if (LOGGER.isDebugEnabled()) LOGGER.debug("Redid command: {}", ctx.get("action"));
                LoggingService.getInstance().audit("commandRedo", ctx, () -> stack.peekUndoDescription());
            });
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

    private Map<String,String> buildContext(String action) {
        Map<String,String> ctx = new HashMap<>();
        ctx.put("action", action);
        String path = ProjectService.getInstance().getAbsolutePath();
        if (path != null) ctx.put("projectPath", path);
        FDDINode sel = ModelState.getInstance().getSelectedNode();
        if (sel != null) ctx.put("selectedNode", sel.getName());
        return ctx;
    }
}
