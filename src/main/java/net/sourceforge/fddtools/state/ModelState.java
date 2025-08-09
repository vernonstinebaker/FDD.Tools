package net.sourceforge.fddtools.state;

import javafx.beans.property.*;
import net.sourceforge.fddtools.model.FDDINode;

/**
 * Central observable application model state slice used for UI bindings.
 * This is an initial lightweight facade; will expand as services are extracted.
 */
public final class ModelState {
    private static final ModelState INSTANCE = new ModelState();
    public static ModelState getInstance() { return INSTANCE; }

    private final ObjectProperty<FDDINode> selectedNode = new SimpleObjectProperty<>();
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private final BooleanProperty clipboardNotEmpty = new SimpleBooleanProperty(false);
    private final BooleanProperty undoAvailable = new SimpleBooleanProperty(false);
    private final BooleanProperty redoAvailable = new SimpleBooleanProperty(false);

    private ModelState() {}

    public ObjectProperty<FDDINode> selectedNodeProperty() { return selectedNode; }
    public FDDINode getSelectedNode() { return selectedNode.get(); }
    public void setSelectedNode(FDDINode n) { selectedNode.set(n); }

    public BooleanProperty dirtyProperty() { return dirty; }
    public boolean isDirty() { return dirty.get(); }
    public void setDirty(boolean v) { dirty.set(v); }

    public BooleanProperty clipboardNotEmptyProperty() { return clipboardNotEmpty; }
    public boolean isClipboardNotEmpty() { return clipboardNotEmpty.get(); }
    public void setClipboardNotEmpty(boolean v) { clipboardNotEmpty.set(v); }

    public BooleanProperty undoAvailableProperty() { return undoAvailable; }
    public void setUndoAvailable(boolean v) { undoAvailable.set(v); }
    public BooleanProperty redoAvailableProperty() { return redoAvailable; }
    public void setRedoAvailable(boolean v) { redoAvailable.set(v); }
}
