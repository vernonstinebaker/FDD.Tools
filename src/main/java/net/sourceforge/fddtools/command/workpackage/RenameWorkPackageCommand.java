package net.sourceforge.fddtools.command.workpackage;

import net.sourceforge.fddtools.command.Command;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;

/** Renames a WorkPackage. */
public class RenameWorkPackageCommand implements Command {
    private final WorkPackage workPackage;
    private final String newName;
    private String oldName;

    public RenameWorkPackageCommand(WorkPackage wp, String newName) { this.workPackage = wp; this.newName = newName; }

    @Override public void execute() { oldName = workPackage.getName(); workPackage.setName(newName); }
    @Override public void undo() { workPackage.setName(oldName); }
    @Override public String description() { return "Rename Work Package to '" + newName + "'"; }
}
