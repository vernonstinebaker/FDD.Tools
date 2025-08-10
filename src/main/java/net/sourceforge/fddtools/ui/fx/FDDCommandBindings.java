package net.sourceforge.fddtools.ui.fx;

import net.sourceforge.fddtools.command.CommandExecutionService;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.service.ProjectService;
import javafx.scene.control.MenuItem;

/** Helper encapsulating undo/redo execution and UI state updates. */
public class FDDCommandBindings {
    private final CommandExecutionService exec;
    private final Runnable refreshView;
    private final Runnable updateTitle;
    private FDDStatusBarFX statusBar;
    private MenuItem undoItem; private MenuItem redoItem;

    public FDDCommandBindings(CommandExecutionService exec, Runnable refreshView, Runnable updateTitle){
        this.exec = exec; this.refreshView = refreshView; this.updateTitle = updateTitle;
    }
    public void setStatusBar(FDDStatusBarFX bar){ this.statusBar = bar; }
    public void setMenuItems(MenuItem undo, MenuItem redo){ this.undoItem=undo; this.redoItem=redo; }

    public void performUndo(){ exec.undo(); postMutation(); }
    public void performRedo(){ exec.redo(); postMutation(); }

    private void postMutation(){
        if (refreshView!=null) refreshView.run();
        ProjectService.getInstance().markDirty();
        updateUndoRedoState();
    }

    public void updateUndoRedoState(){
        ModelState ms = ModelState.getInstance();
        ms.setUndoAvailable(exec.getStack().canUndo());
        ms.setRedoAvailable(exec.getStack().canRedo());
        if (undoItem!=null) undoItem.setText("Undo");
        if (redoItem!=null) redoItem.setText("Redo");
        if (statusBar!=null) statusBar.updateUndoRedo(exec.getStack());
        if (updateTitle!=null) updateTitle.run();
    }
}
