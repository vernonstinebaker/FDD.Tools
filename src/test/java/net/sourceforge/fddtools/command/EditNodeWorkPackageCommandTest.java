package net.sourceforge.fddtools.command;

import net.sourceforge.fddtools.commands.EditNodeCommand;
import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.Project;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Tests work package membership undo/redo within EditNodeCommand snapshots. */
public class EditNodeWorkPackageCommandTest {
    @Test
    void workPackageAssignmentUndoRedo() {
        Project project = new Project();
        project.setName("P");
        Feature feature = new Feature();
        feature.setName("F1");
        // wire parent chain manually
        feature.setParentNode(project);

        // Add two work packages to project 'any' extension list
        WorkPackage wp1 = new WorkPackage();
        wp1.setName("WP1");
        WorkPackage wp2 = new WorkPackage();
        wp2.setName("WP2");
        project.getAny().add(wp1);
        project.getAny().add(wp2);

        // initial snapshot (unassigned)
        var before = EditNodeCommand.capture(feature);
        assertEquals("", before.getWorkPackageName());

        // assign to WP1 by mutating featureSeq list
        wp1.addFeature(feature.getSeq());
        var afterAssign = EditNodeCommand.capture(feature);
        assertEquals("WP1", afterAssign.getWorkPackageName());
        // revert lists to before
        wp1.getFeatureList().clear();

        EditNodeCommand assignCmd = new EditNodeCommand(feature, before, afterAssign);
        assignCmd.execute();
        assertTrue(wp1.getFeatureList().contains(feature.getSeq()));
        assignCmd.undo();
        assertFalse(wp1.getFeatureList().contains(feature.getSeq()));

        // Now reassign WP1 -> WP2
        // Ensure feature currently unassigned
        wp1.getFeatureList().remove(Integer.valueOf(feature.getSeq()));
        var beforeReassign = EditNodeCommand.capture(feature); // unassigned again
        wp2.addFeature(feature.getSeq());
        var afterReassign = EditNodeCommand.capture(feature);
        wp2.getFeatureList().clear();
        EditNodeCommand reassignCmd = new EditNodeCommand(feature, beforeReassign, afterReassign);
        reassignCmd.execute();
        assertFalse(wp1.getFeatureList().contains(feature.getSeq()));
        assertTrue(wp2.getFeatureList().contains(feature.getSeq()));
        reassignCmd.undo();
        assertFalse(wp1.getFeatureList().contains(feature.getSeq()));
        assertFalse(wp2.getFeatureList().contains(feature.getSeq()));
    }
}
