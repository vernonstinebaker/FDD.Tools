package net.sourceforge.fddtools.command;

import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.state.ModelState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies CommandExecutionService updates ModelState (undo/redo flags & descriptions, dirty flag).
 */
public class CommandExecutionServiceTest {

    private FDDINode newProgram(String name) {
        ObjectFactory of = new ObjectFactory();
        Program p = of.createProgram();
        p.setName(name);
        return (FDDINode) p;
    }

    @BeforeEach
    void resetState() {
        CommandExecutionService.getInstance().getStack().clear();
        ModelState ms = ModelState.getInstance();
        ms.setDirty(false);
        ms.setUndoAvailable(false);
        ms.setRedoAvailable(false);
        ms.setNextUndoDescription("");
        ms.setNextRedoDescription("");
    }

    @Test
    void executeUndoRedoLifecycle() {
        var root = newProgram("Root");
        var child = newProgram("Child");
        var svc = CommandExecutionService.getInstance();

        svc.execute(new AddChildCommand(root, child));
        assertEquals(1, root.getChildren().size());
        assertTrue(ModelState.getInstance().isDirty());
        assertTrue(ModelState.getInstance().undoAvailableProperty().get());
        assertFalse(ModelState.getInstance().redoAvailableProperty().get());
        assertTrue(ModelState.getInstance().getNextUndoDescription().startsWith("Add "));
        assertEquals("", ModelState.getInstance().getNextRedoDescription());

        svc.undo();
        assertEquals(0, root.getChildren().size());
    assertFalse(ModelState.getInstance().undoAvailableProperty().get(), "Undo should now be unavailable");
        assertTrue(ModelState.getInstance().redoAvailableProperty().get());
        assertTrue(ModelState.getInstance().getNextRedoDescription().startsWith("Add "));
    assertEquals("", ModelState.getInstance().getNextUndoDescription(), "Next undo description should reset to empty after last undo");

        svc.redo();
        assertEquals(1, root.getChildren().size());
        assertTrue(ModelState.getInstance().undoAvailableProperty().get());
        assertTrue(ModelState.getInstance().getNextUndoDescription().startsWith("Add "));
    }
}
