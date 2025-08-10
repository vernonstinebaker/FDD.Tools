package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.Project;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;
import net.sourceforge.fddtools.internationalization.Messages;

import java.util.List;

/**
 * Extracted helper for assigning/unassigning a Feature to a WorkPackage.
 * Keeps UI dialog lean and enables isolated unit testing.
 */
public final class FeatureWorkPackageHelper {
    private FeatureWorkPackageHelper() {}

    /** Populate combo list and return previously assigned WorkPackage (or null). */
    public static WorkPackage initializeCombo(Feature feature, Project project, javafx.scene.control.ComboBox<WorkPackage> combo) {
        List<WorkPackage> workPackages = project.getWorkPackages();
        WorkPackage unassigned = new WorkPackage();
        unassigned.setName(Messages.getInstance().getMessage(Messages.UNASSIGNED_WORKPACKAGE_NAME));
        combo.getItems().add(unassigned);
        combo.getItems().addAll(workPackages);
        combo.setValue(unassigned);
        WorkPackage old = null;
        for (WorkPackage wp : workPackages) {
            if (wp.getFeatureList().contains(feature.getSeq())) {
                combo.setValue(wp); old = wp; break;
            }
        }
        return old;
    }

    /** Apply change if user selected different work package. */
    public static void applySelection(Feature feature, WorkPackage previous, WorkPackage selected) {
        if (selected == null || selected == previous) return;
        Integer featureSeq = feature.getSeq();
        if (previous != null) previous.getFeatureList().remove(featureSeq);
        if (!selected.getName().equals(Messages.getInstance().getMessage(Messages.UNASSIGNED_WORKPACKAGE_NAME))) {
            selected.addFeature(featureSeq);
        }
    }
}
