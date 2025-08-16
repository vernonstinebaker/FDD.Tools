package net.sourceforge.fddtools.command;

import net.sourceforge.fddtools.model.FDDINode;

/**
 * Generalized edit command capturing before/after snapshots of mutable fields.
 * Currently tracks: name, prefix (Subject), initials (Activity/Feature owner).
 * Can be extended to additional fields (dates, milestones, etc.).
 */
public class EditNodeCommand implements Command {
    private final FDDINode node;
    private final Snapshot before;
    private final Snapshot after;

    public EditNodeCommand(FDDINode node, Snapshot before, Snapshot after) {
        this.node = node;
        this.before = before;
        this.after = after;
    }

    @Override
    public void execute() { apply(after); }

    @Override
    public void undo() { apply(before); }

    @Override
    public String description() { return "Edit " + node.getClass().getSimpleName(); }

    private void apply(Snapshot s) {
        node.setName(s.name);
        if (node instanceof com.nebulon.xml.fddi.Subject subj) {
            subj.setPrefix(s.prefix);
        }
        if (node instanceof com.nebulon.xml.fddi.Activity act) {
            act.setInitials(s.ownerInitials);
        } else if (node instanceof com.nebulon.xml.fddi.Feature feat) {
            feat.setInitials(s.ownerInitials);
            if (s.milestoneStatuses != null) {
                var milestones = feat.getMilestone();
                for (int i = 0; i < Math.min(milestones.size(), s.milestoneStatuses.length); i++) {
                    milestones.get(i).setStatus(s.milestoneStatuses[i]);
                }
            }
            // Work package reassignment (remove from original, add to target)
            if (s.workPackageName != null) {
                var project = findOwningProject(feat);
                if (project != null) {
                    var wps = project.getWorkPackages();
                    for (var wp : wps) {
                        wp.getFeatureList().remove(Integer.valueOf(feat.getSeq()));
                    }
                    if (!s.workPackageName.isEmpty()) {
                        for (var wp : wps) {
                            if (s.workPackageName.equals(wp.getName())) {
                                if (!wp.getFeatureList().contains(feat.getSeq())) wp.addFeature(feat.getSeq());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /** Builder to capture before/after state */
    public static Snapshot capture(FDDINode node) {
        Snapshot s = new Snapshot();
        s.name = node.getName();
        if (node instanceof com.nebulon.xml.fddi.Subject subj) {
            s.prefix = subj.getPrefix();
        }
        if (node instanceof com.nebulon.xml.fddi.Activity act) {
            s.ownerInitials = act.getInitials();
        } else if (node instanceof com.nebulon.xml.fddi.Feature feat) {
            s.ownerInitials = feat.getInitials();
            var milestones = feat.getMilestone();
            if (!milestones.isEmpty()) {
                s.milestoneStatuses = milestones.stream().map(m -> m.getStatus()).toArray(com.nebulon.xml.fddi.StatusEnum[]::new);
            }
            // Determine current work package by feature seq membership
            var proj = findOwningProject(feat);
            if (proj != null) {
                for (var wp : proj.getWorkPackages()) {
                    if (wp.getFeatureList().contains(feat.getSeq())) {
                        s.workPackageName = wp.getName();
                        break;
                    }
                }
                if (s.workPackageName == null) s.workPackageName = ""; // empty for unassigned
            }
        }
        return s;
    }

    public static class Snapshot {
    String name;
    String prefix;
    String ownerInitials;
    com.nebulon.xml.fddi.StatusEnum[] milestoneStatuses;
    String workPackageName; // empty = unassigned

    public String getName() { return name; }
    public String getPrefix() { return prefix; }
    public String getOwnerInitials() { return ownerInitials; }
    public com.nebulon.xml.fddi.StatusEnum[] getMilestoneStatuses() { return milestoneStatuses; }
    public String getWorkPackageName() { return workPackageName; }
    }

    private static com.nebulon.xml.fddi.Project findOwningProject(com.nebulon.xml.fddi.Feature feat) {
        net.sourceforge.fddtools.model.FDDTreeNode current = feat.getParentNode();
        while (current != null) {
            if (current instanceof com.nebulon.xml.fddi.Project p) return p;
            current = current.getParentNode();
        }
        return null;
    }
}
