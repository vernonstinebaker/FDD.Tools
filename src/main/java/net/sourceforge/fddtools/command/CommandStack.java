package net.sourceforge.fddtools.command;

import java.util.ArrayDeque;
import java.util.Deque;

/** Simple undo/redo stack manager. */
public class CommandStack {
    private final Deque<Command> undo = new ArrayDeque<>();
    private final Deque<Command> redo = new ArrayDeque<>();
    private int maxSize = 100;

    public void execute(Command cmd) {
        cmd.execute();
        undo.push(cmd);
        redo.clear();
        trim();
    }

    public boolean canUndo() { return !undo.isEmpty(); }
    public boolean canRedo() { return !redo.isEmpty(); }

    public void undo() {
        if (undo.isEmpty()) return;
        Command cmd = undo.pop();
        cmd.undo();
        redo.push(cmd);
    }

    public void redo() {
        if (redo.isEmpty()) return;
        Command cmd = redo.pop();
        cmd.execute();
        undo.push(cmd);
    }

    /** Description of the command that would be undone next, or null. */
    public String peekUndoDescription() { return undo.isEmpty() ? null : undo.peek().description(); }
    /** Description of the command that would be redone next, or null. */
    public String peekRedoDescription() { return redo.isEmpty() ? null : redo.peek().description(); }

    private void trim() {
        while (undo.size() > maxSize) undo.removeLast();
    }

    public void clear() { undo.clear(); redo.clear(); }
}
