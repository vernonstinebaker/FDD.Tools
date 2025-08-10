package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Aspect;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Headless tests for sibling insertion logic using FDDHierarchyRules (no JavaFX). */
public class FDDTreeDragAndDropSiblingTest {
    private final ObjectFactory of = new ObjectFactory();
    private Program program(String name){ Program p = of.createProgram(); p.setName(name); return p; }
    private Project project(String name){ Project p = of.createProject(); p.setName(name); return p; }
    private Aspect aspect(String name){ Aspect a = of.createAspect(); a.setName(name); return a; }

    @Test
    void cannotInsertAroundRootOrSelf() {
        Program root = program("Root");
        Program a = program("A");
        root.add((FDDINode)a);
        assertFalse(FDDHierarchyRules.canInsertSibling(a,a,true));
        assertFalse(FDDHierarchyRules.canInsertSibling(a, root, true));
    }

    @Test
    void acceptsValidSiblingInsertionSameParent() {
        Program root = program("Root");
        Project p1 = project("P1");
        Project p2 = project("P2");
        root.add((FDDINode)p1); root.add((FDDINode)p2);
        assertTrue(FDDHierarchyRules.canInsertSibling(p1,p2,true));
        assertTrue(FDDHierarchyRules.canInsertSibling(p2,p1,true));
    }

    @Test
    void rejectsWhenHierarchyRuleBroken() {
        Program root = program("Root");
        Project pr = project("P");
        Aspect asp = aspect("A1");
        root.add((FDDINode)pr);
        pr.add((FDDINode)asp);
        assertFalse(FDDHierarchyRules.canInsertSibling(asp, pr, true));
    }

    @Test
    void rejectsNoOpSingleChildReorder() {
        Program root = program("Root");
        Project only = project("Only");
        root.add((FDDINode)only);
        assertFalse(FDDHierarchyRules.canInsertSibling(only, only, true));
    }
}
