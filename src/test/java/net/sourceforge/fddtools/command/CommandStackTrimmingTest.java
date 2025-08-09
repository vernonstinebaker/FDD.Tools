package net.sourceforge.fddtools.command;

import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Ensures CommandStack trims to default max size (100). */
public class CommandStackTrimmingTest {

    private FDDINode newProgram(String name) {
        ObjectFactory of = new ObjectFactory();
        Program p = of.createProgram();
        p.setName(name);
        return (FDDINode) p;
    }

    @Test
    void stackTrimsBeyondMax() {
        CommandStack stack = new CommandStack();
        FDDINode root = newProgram("Root");
        for (int i=0;i<105;i++) {
            FDDINode child = newProgram("C"+i);
            stack.execute(new AddChildCommand(root, child));
        }
        assertEquals(100, stack.undoSize());
    }
}
