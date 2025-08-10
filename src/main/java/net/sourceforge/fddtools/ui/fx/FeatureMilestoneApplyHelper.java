package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.MilestoneInfo;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.GridPane;

import javax.xml.datatype.DatatypeFactory;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Extracts JavaFX control scraping for milestone updates out of FDDElementDialogFX.
 * Converts the current state of a milestone grid into MilestoneUpdate objects and applies them.
 */
public final class FeatureMilestoneApplyHelper {
    private FeatureMilestoneApplyHelper() {}

    /** Scrape milestone GridPane controls (expected layout produced by FeaturePanelBuilder) and apply updates. */
    public static void applyFromGrid(Feature feature, List<MilestoneInfo> infoList, GridPane milestoneGrid) {
        if (feature == null || infoList == null || milestoneGrid == null) return;
        java.util.ArrayList<FeatureMilestoneHelper.MilestoneUpdate> updates = new java.util.ArrayList<>();
        for (int i = 0; i < infoList.size(); i++) {
            FeatureMilestoneHelper.MilestoneUpdate u = new FeatureMilestoneHelper.MilestoneUpdate();
            DatePicker plannedPicker = null; DatePicker actualPicker = null; CheckBox completeCheck = null;
            for (javafx.scene.Node node : milestoneGrid.getChildren()) {
                Integer row = GridPane.getRowIndex(node); Integer col = GridPane.getColumnIndex(node);
                if (row!=null && row == i+1) {
                    if (col==1 && node instanceof DatePicker dp) plannedPicker = dp;
                    else if (col==2 && node instanceof DatePicker dp2) actualPicker = dp2;
                    else if (col==3 && node instanceof CheckBox cb) completeCheck = cb;
                }
            }
            if (plannedPicker != null && plannedPicker.getValue()!=null) {
                GregorianCalendar cal = new GregorianCalendar();
                cal.set(plannedPicker.getValue().getYear(), plannedPicker.getValue().getMonthValue()-1, plannedPicker.getValue().getDayOfMonth());
                try { u.planned = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal); } catch (Exception ignore) {}
            }
            if (actualPicker != null && actualPicker.getValue()!=null) {
                GregorianCalendar cal = new GregorianCalendar();
                cal.set(actualPicker.getValue().getYear(), actualPicker.getValue().getMonthValue()-1, actualPicker.getValue().getDayOfMonth());
                try { u.actual = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal); } catch (Exception ignore) {}
            }
            if (completeCheck != null) u.complete = completeCheck.isSelected();
            updates.add(u);
        }
        FeatureMilestoneHelper.applyUpdates(feature, updates);
    }
}
