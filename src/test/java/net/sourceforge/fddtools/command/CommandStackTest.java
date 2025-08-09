package net.sourceforge.fddtools.command;

import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommandStackTest {

    private FDDINode newProgram(String name) {
        ObjectFactory of = new ObjectFactory();
        Program p = of.createProgram();
        p.setName(name);
        return (FDDINode) p;
    }

    @Test
    void addAndUndo() {
        FDDINode root = newProgram("Root");
        FDDINode child = newProgram("Child");
        CommandStack stack = new CommandStack();
        stack.execute(new AddChildCommand(root, child));
        assertEquals(1, root.getChildren().size());
        stack.undo();
        assertEquals(0, root.getChildren().size());
        stack.redo();
        assertEquals(1, root.getChildren().size());
    }
}
