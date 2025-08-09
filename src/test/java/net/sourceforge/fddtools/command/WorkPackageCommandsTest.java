package net.sourceforge.fddtools.command;

import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Project;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;
import net.sourceforge.fddtools.commands.workpackage.AddWorkPackageCommand;
import net.sourceforge.fddtools.commands.workpackage.DeleteWorkPackageCommand;
import net.sourceforge.fddtools.commands.workpackage.RenameWorkPackageCommand;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WorkPackageCommandsTest {

    @Test
    void addDeleteAndRenameWorkPackageUndoRedo() {
        ObjectFactory f = new ObjectFactory();
        Project project = f.createProject();
        project.setName("P1");
        WorkPackage wp = new WorkPackage();
        wp.setName("WP1");

        CommandStack stack = new CommandStack();
        stack.execute(new AddWorkPackageCommand(project, wp));
        assertTrue(project.getAny().contains(wp));
        assertEquals("WP1", wp.getName());

        stack.execute(new RenameWorkPackageCommand(wp, "Renamed"));
        assertEquals("Renamed", wp.getName());

        stack.undo(); // undo rename
        assertEquals("WP1", wp.getName());
        stack.undo(); // undo add
        assertFalse(project.getAny().contains(wp));

        stack.redo(); // redo add
        assertTrue(project.getAny().contains(wp));
        stack.redo(); // redo rename
        assertEquals("Renamed", wp.getName());

        // Delete command
        stack.execute(new DeleteWorkPackageCommand(project, wp));
        assertFalse(project.getAny().contains(wp));
        stack.undo(); // undo delete
        assertTrue(project.getAny().contains(wp));
        stack.undo(); // undo rename
        assertEquals("WP1", wp.getName());
    }
}
