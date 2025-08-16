package net.sourceforge.fddtools.command;

import com.nebulon.xml.fddi.*;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.command.EditNodeCommand;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Tests for PasteNodeCommand and generalized EditNodeCommand rename with undo/redo on CommandStack. */
public class PasteAndRenameCommandTest {

    private ObjectFactory of = new ObjectFactory();

    private FDDINode newProgram(String name) {
        Program p = of.createProgram();
        p.setName(name);
        return (FDDINode) p;
    }

    @Test
    void pasteAndUndoRedo() {
        FDDINode root = newProgram("Root");
        FDDINode source = newProgram("Template");
        CommandStack stack = new CommandStack();
        PasteNodeCommand paste = new PasteNodeCommand(root, source, false);
        stack.execute(paste);
        assertEquals(1, root.getChildren().size(), "Child added after paste");
        FDDINode pasted = paste.getPasted();
        assertNotNull(pasted);
        stack.undo();
        assertEquals(0, root.getChildren().size(), "Child removed after undo");
        stack.redo();
        assertEquals(1, root.getChildren().size(), "Child re-added after redo");
    }

    @Test
    void renameUndoRedo() {
        FDDINode root = newProgram("Root");
        CommandStack stack = new CommandStack();
        var before = EditNodeCommand.capture(root);
        root.setName("RootRenamed");
        var after = EditNodeCommand.capture(root);
        // revert before executing command
        root.setName(before.getName());
        EditNodeCommand rename = new EditNodeCommand(root, before, after);
        stack.execute(rename);
        assertEquals("RootRenamed", root.getName());
        stack.undo();
        assertEquals("Root", root.getName());
        stack.redo();
        assertEquals("RootRenamed", root.getName());
    }
}
