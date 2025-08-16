package net.sourceforge.fddtools.command.workpackage;

import net.sourceforge.fddtools.command.Command;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;
import com.nebulon.xml.fddi.Project;

/** Deletes an existing WorkPackage from a Project. */
public class DeleteWorkPackageCommand implements Command {
    private final Project project;
    private final WorkPackage workPackage;
    private int priorIndex = -1;

    public DeleteWorkPackageCommand(Project project, WorkPackage wp) {
        this.project = project;
        this.workPackage = wp;
    }

    @Override public void execute() { priorIndex = project.getAny().indexOf(workPackage); project.getAny().remove(workPackage); }
    @Override public void undo() { if (priorIndex >= 0) project.getAny().add(priorIndex, workPackage); }
    @Override public String description() { return "Delete Work Package '" + workPackage.getName() + "'"; }
}
