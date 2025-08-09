package net.sourceforge.fddtools.command;

import net.sourceforge.fddtools.commands.EditNodeCommand;
import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.Milestone;
import com.nebulon.xml.fddi.StatusEnum;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Tests milestone status inclusion in EditNodeCommand snapshot. */
public class EditNodeMilestonesCommandTest {
    @Test
    void milestoneStatusUndoRedo() {
        Feature feature = new Feature();
        feature.setName("F1");
        // create two milestones with different statuses
        Milestone m1 = new Milestone();
        m1.setStatus(StatusEnum.NOTSTARTED);
        Milestone m2 = new Milestone();
        m2.setStatus(StatusEnum.NOTSTARTED);
        feature.getMilestone().add(m1);
        feature.getMilestone().add(m2);

        var before = EditNodeCommand.capture(feature);
        // mutate
        m1.setStatus(StatusEnum.COMPLETE);
    m2.setStatus(StatusEnum.UNDERWAY);
        var after = EditNodeCommand.capture(feature);
        // revert to before so command applies after snapshot
        m1.setStatus(before.getMilestoneStatuses()[0]);
        m2.setStatus(before.getMilestoneStatuses()[1]);

        EditNodeCommand cmd = new EditNodeCommand(feature, before, after);
        cmd.execute();
        assertEquals(StatusEnum.COMPLETE, feature.getMilestone().get(0).getStatus());
    assertEquals(StatusEnum.UNDERWAY, feature.getMilestone().get(1).getStatus());
        cmd.undo();
        assertEquals(StatusEnum.NOTSTARTED, feature.getMilestone().get(0).getStatus());
        assertEquals(StatusEnum.NOTSTARTED, feature.getMilestone().get(1).getStatus());
    }
}
