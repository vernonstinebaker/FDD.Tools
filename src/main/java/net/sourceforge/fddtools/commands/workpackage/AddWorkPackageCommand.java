package net.sourceforge.fddtools.commands.workpackage;

import net.sourceforge.fddtools.command.Command;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;
import com.nebulon.xml.fddi.Project;

/** Adds a new WorkPackage to a Project (stored in project.any list). */
public class AddWorkPackageCommand implements Command {
    private final Project project;
    private final WorkPackage workPackage;
    private boolean executed;

    public AddWorkPackageCommand(Project project, WorkPackage wp) {
        this.project = project;
        this.workPackage = wp;
    }

    @Override public void execute() { project.getAny().add(workPackage); executed = true; }
    @Override public void undo() { if (executed) project.getAny().remove(workPackage); }
    @Override public String description() { return "Add Work Package '" + workPackage.getName() + "'"; }
}
