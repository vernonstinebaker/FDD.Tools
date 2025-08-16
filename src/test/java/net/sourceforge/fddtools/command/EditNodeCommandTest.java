package net.sourceforge.fddtools.command;

import net.sourceforge.fddtools.command.EditNodeCommand;
import net.sourceforge.fddtools.model.FDDINode;
import com.nebulon.xml.fddi.Subject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EditNodeCommandTest {
    @Test
    void testEditSubjectNameAndPrefix() {
        Subject subj = new Subject();
        subj.setName("Old Name");
        subj.setPrefix("OLD");
        FDDINode node = (FDDINode) subj; // Subject implements FDDINode
        var before = EditNodeCommand.capture(node);
        subj.setName("New Name");
        subj.setPrefix("NEW");
        var after = EditNodeCommand.capture(node);
        // revert to before state
        subj.setName(before.getName());
        subj.setPrefix(before.getPrefix());
        EditNodeCommand cmd = new EditNodeCommand(node, before, after);
        cmd.execute();
        assertEquals("New Name", subj.getName());
        assertEquals("NEW", subj.getPrefix());
        cmd.undo();
        assertEquals("Old Name", subj.getName());
        assertEquals("OLD", subj.getPrefix());
    }
}
