package net.sourceforge.fddtools.command;

import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Tests ordered insertion (before/after semantics) supported by MoveNodeCommand(newIndex). */
public class MoveNodeCommandOrderingTest {
    private final ObjectFactory of = new ObjectFactory();

    private Activity activity(String n){ Activity a = of.createActivity(); a.setName(n); return a; }
    private Feature feature(String n){ Feature f = of.createFeature(); f.setName(n); return f; }

    @BeforeEach
    void init(){ CommandExecutionService.getInstance().getStack().clear(); }

    @Test
    void moveWithinSameParentEarlierAndUndo() {
        Activity act = activity("Act");
        Feature f1 = feature("F1"); Feature f2 = feature("F2"); Feature f3 = feature("F3");
        act.add((FDDINode) f1); act.add((FDDINode) f2); act.add((FDDINode) f3);
        assertEquals("F1", act.getFeature().get(0).getName());
        assertEquals("F2", act.getFeature().get(1).getName());
        assertEquals("F3", act.getFeature().get(2).getName());

        // Move F3 before F2 (target index 1)
        MoveNodeCommand move = new MoveNodeCommand((FDDINode) f3, (FDDINode) act, 1);
        CommandExecutionService.getInstance().execute(move);
        assertEquals("F1", act.getFeature().get(0).getName());
        assertEquals("F3", act.getFeature().get(1).getName());
        assertEquals("F2", act.getFeature().get(2).getName());

        CommandExecutionService.getInstance().undo();
        assertEquals("F1", act.getFeature().get(0).getName());
        assertEquals("F2", act.getFeature().get(1).getName());
        assertEquals("F3", act.getFeature().get(2).getName());

        CommandExecutionService.getInstance().redo();
        assertEquals("F1", act.getFeature().get(0).getName());
        assertEquals("F3", act.getFeature().get(1).getName());
        assertEquals("F2", act.getFeature().get(2).getName());
    }

    @Test
    void moveToEndUsingOutOfRangeIndexAppends() {
        Activity act = activity("Act");
        Feature f1 = feature("F1"); Feature f2 = feature("F2"); Feature f3 = feature("F3");
        act.add((FDDINode) f1); act.add((FDDINode) f2); act.add((FDDINode) f3);
        // Move F1 to index beyond size -> should append at end
        MoveNodeCommand move = new MoveNodeCommand((FDDINode) f1, (FDDINode) act, 99);
        CommandExecutionService.getInstance().execute(move);
        assertEquals("F2", act.getFeature().get(0).getName());
        assertEquals("F3", act.getFeature().get(1).getName());
        assertEquals("F1", act.getFeature().get(2).getName());
    }
}
