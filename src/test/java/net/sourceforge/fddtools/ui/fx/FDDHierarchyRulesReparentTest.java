package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Tests for isValidReparent logic including cycle prevention and program exclusivity. */
public class FDDHierarchyRulesReparentTest {
    private final ObjectFactory of = new ObjectFactory();
    private Program program(String n){ Program p = of.createProgram(); p.setName(n); return p; }
    private Project project(String n){ Project p = of.createProject(); p.setName(n); return p; }

    @Test
    void preventCycle() {
        Program root = program("Root");
        Program childProg = program("Child");
        root.add((FDDINode)childProg);
        // Attempt to reparent root under its descendant should fail
        assertFalse(FDDHierarchyRules.isValidReparent((FDDINode)root, (FDDINode)childProg, true));
    }

    @Test
    void programExclusivity() {
        Program parent = program("P");
        Program subProgram = program("Sub");
        Project project = project("Prj");
        parent.add((FDDINode)subProgram);
        // adding a project when a program child exists (business logic enabled) not allowed
        assertFalse(FDDHierarchyRules.hierarchyAccepts((FDDINode)parent, (FDDINode)project, true));
    }

    @Test
    void programExclusivityDisabledAllowsBoth() {
        Program parent = program("P");
        Program subProgram = program("Sub");
        Project project = project("Prj");
        parent.add((FDDINode)subProgram);
        assertTrue(FDDHierarchyRules.hierarchyAccepts((FDDINode)parent, (FDDINode)project, false));
    }
}
