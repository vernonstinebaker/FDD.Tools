package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.*;
import net.sourceforge.fddtools.model.FDDINode;

/**
 * Pure logic utility (no JavaFX dependency) for hierarchy validation and
 * sibling insertion rules to enable headless unit testing.
 */
final class FDDHierarchyRules {
    private FDDHierarchyRules() {}

    static boolean hierarchyAccepts(FDDINode parent, FDDINode child, boolean enableProgramBusinessLogic) {
        if (parent instanceof Program) {
            boolean typeAllowed = (child instanceof Program) || (child instanceof Project);
            if (!typeAllowed) return false;
            if (enableProgramBusinessLogic) {
                Program prog = (Program) parent;
                int programChildren = prog.getProgram() == null ? 0 : prog.getProgram().size();
                int projectChildren = prog.getProject() == null ? 0 : prog.getProject().size();
                if (child instanceof Program && projectChildren > 0) return false;
                if (child instanceof Project && programChildren > 0) return false;
            }
            return true;
        }
        if (parent instanceof Project) return (child instanceof Aspect);
        if (parent instanceof Aspect) return (child instanceof Subject);
        if (parent instanceof Subject) return (child instanceof Activity);
        if (parent instanceof Activity) return (child instanceof Feature);
        return false;
    }

    static boolean isDescendant(FDDINode candidateParent, FDDINode potentialChild){
        FDDINode p = (FDDINode) candidateParent.getParentNode();
        while (p != null){
            if (p == potentialChild) return true;
            p = (FDDINode) p.getParentNode();
        }
        return false;
    }

    static boolean isValidReparent(FDDINode child, FDDINode newParent, boolean enableProgramBusinessLogic){
        if (child == null || newParent == null) return false;
        if (child.getParentNode() == null) return false; // cannot move root
        if (isDescendant(newParent, child)) return false; // prevent cycles
        return hierarchyAccepts(newParent, child, enableProgramBusinessLogic);
    }

    static boolean canInsertSibling(FDDINode dragSource, FDDINode reference, boolean enableProgramBusinessLogic){
        if (dragSource==null || reference==null) return false;
        FDDINode parent = (FDDINode) reference.getParentNode(); if (parent==null) return false; // cannot insert around root
        if (dragSource == reference) return false;
        if (dragSource.getParentNode() == parent && parent.getChildren().size()==1) return false; // no-op (single child)
        return hierarchyAccepts(parent, dragSource, enableProgramBusinessLogic) && !isDescendant(reference, dragSource);
    }
}
