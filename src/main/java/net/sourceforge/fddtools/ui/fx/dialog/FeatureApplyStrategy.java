package net.sourceforge.fddtools.ui.fx.dialog;

import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.MilestoneInfo;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.ui.fx.FeatureMilestoneApplyHelper;
import net.sourceforge.fddtools.ui.fx.FeatureMilestoneHelper;
import net.sourceforge.fddtools.ui.fx.FeatureWorkPackageHelper;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;
import java.util.List;

/** Apply strategy for Feature nodes. */
public class FeatureApplyStrategy implements ElementApplyStrategy {
    private final ComboBox<WorkPackage> workPackageCombo;
    private final WorkPackage oldWorkPackage;
    private final GridPane milestoneGrid;
    private final TextField ownerField;

    public FeatureApplyStrategy(ComboBox<WorkPackage> workPackageCombo, WorkPackage oldWorkPackage,
                                GridPane milestoneGrid, TextField ownerField) {
        this.workPackageCombo = workPackageCombo;
        this.oldWorkPackage = oldWorkPackage;
        this.milestoneGrid = milestoneGrid;
        this.ownerField = ownerField;
    }

    @Override
    public boolean apply(FDDINode node) {
        if(!(node instanceof Feature feature)) return true;
        feature.setInitials(ownerField.getText().trim());
        if (workPackageCombo != null) {
            FeatureWorkPackageHelper.applySelection(feature, oldWorkPackage, workPackageCombo.getValue());
        }
        Aspect aspect = feature.getAspectForNode();
        if (aspect != null && aspect.getInfo() != null && aspect.getInfo().getMilestoneInfo() != null && !aspect.getInfo().getMilestoneInfo().isEmpty()) {
            List<MilestoneInfo> milestoneInfo = aspect.getInfo().getMilestoneInfo();
            FeatureMilestoneHelper.alignMilestones(feature, milestoneInfo, FeatureMilestoneHelper.todaySupplier());
            if (milestoneGrid != null) {
                FeatureMilestoneApplyHelper.applyFromGrid(feature, milestoneInfo, milestoneGrid);
            }
        }
        return true;
    }
}
