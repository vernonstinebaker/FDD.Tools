package net.sourceforge.fddtools.command;

import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies MoveNodeCommand reparent logic and undo/redo through CommandExecutionService.
 */
public class MoveNodeCommandTest {

    private ObjectFactory of;

    @BeforeEach
    void setup() {
        of = new ObjectFactory();
        CommandExecutionService.getInstance().getStack().clear();
    }

    private Program program(String name) {
        Program p = of.createProgram();
        p.setName(name);
        return p;
    }

    private Project project(String name) {
        Project p = of.createProject();
        p.setName(name);
        return p;
    }

    @Test
    void moveProjectBetweenProgramsUndoRedo() {
        Program a = program("A");
        Program b = program("B");
        Project proj = project("P");
        a.add((FDDINode) proj); // use add to set parent pointer
        assertEquals(1, a.getProject().size());
        assertEquals(0, b.getProject().size());

        MoveNodeCommand move = new MoveNodeCommand((FDDINode) proj, (FDDINode) b);
        CommandExecutionService.getInstance().execute(move);

        assertEquals(0, a.getProject().size(), "Project should be removed from A after move");
        assertEquals(1, b.getProject().size(), "Project should be added to B");

        CommandExecutionService.getInstance().undo();
        assertEquals(1, a.getProject().size(), "Undo should restore project to A");
        assertEquals(0, b.getProject().size());

        CommandExecutionService.getInstance().redo();
        assertEquals(0, a.getProject().size());
        assertEquals(1, b.getProject().size());
    }
}
