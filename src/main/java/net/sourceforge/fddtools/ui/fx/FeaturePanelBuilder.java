package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;
import net.sourceforge.fddtools.internationalization.Messages;

import java.time.LocalDate;
import java.util.List;

/** Builder extracting Feature-specific panel construction from FDDElementDialogFX. */
public final class FeaturePanelBuilder {
    private FeaturePanelBuilder() {}

    public static final class Result {
        public final VBox panel;
        public final ComboBox<WorkPackage> workPackageCombo;
        public final Label progressLabel;
        public final GridPane milestoneGrid; // nullable if no milestones
        public final WorkPackage oldWorkPackage;
        private Result(VBox panel, ComboBox<WorkPackage> combo, Label progressLabel, GridPane milestoneGrid, WorkPackage oldWorkPackage) {
            this.panel = panel; this.workPackageCombo = combo; this.progressLabel = progressLabel; this.milestoneGrid = milestoneGrid; this.oldWorkPackage = oldWorkPackage;
        }
    }

    public static Result build(Feature feature) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
    panel.getStyleClass().addAll("panel","panel-bordered","panel-elevated");

        // Feature info
        TitledPane featureInfo = new TitledPane();
        featureInfo.setText(Messages.getInstance().getMessage(Messages.JPANEL_INFO_TITLE));
        featureInfo.setCollapsible(false);
        GridPane featureGrid = new GridPane(); featureGrid.setHgap(10); featureGrid.setVgap(5); featureGrid.setPadding(new Insets(10));

        // Owner (text field supplied by outer dialog; here we just place placeholder label showing existing initials)
        Label ownerLabel = new Label(Messages.getInstance().getMessage(Messages.JLABEL_OWNER_CAPTION));
        TextField ownerField = new TextField(); ownerField.setPrefWidth(50); if (feature.getInitials()!=null) ownerField.setText(feature.getInitials());
        featureGrid.add(ownerLabel, 0, 0); featureGrid.add(ownerField, 1, 0);

        // Work package via helper
        Label wpLabel = new Label(Messages.getInstance().getMessage(Messages.JLABEL_WORKPACKAGE_TITLE));
        ComboBox<WorkPackage> workPackageCombo = new ComboBox<>(); workPackageCombo.setPrefWidth(200);
        Project project = (Project) feature.getParent().getParent().getParent().getParent();
        WorkPackage oldWP = FeatureWorkPackageHelper.initializeCombo(feature, project, workPackageCombo);
        featureGrid.add(wpLabel, 0, 1); featureGrid.add(workPackageCombo, 1, 1);

        featureInfo.setContent(featureGrid);

        // Progress + milestones
        TitledPane progressInfo = new TitledPane();
        progressInfo.setText(Messages.getInstance().getMessage(Messages.JPANEL_PROGRESS_TITLE));
        progressInfo.setCollapsible(false);
        VBox progressContent = new VBox(10); progressContent.setPadding(new Insets(10));
        Label progressLabel = new Label();
        int progress = feature.getProgress()!=null ? feature.getProgress().getCompletion() : 0;
        progressLabel.setText(Messages.getInstance().getMessage(Messages.JLABEL_PERCENTCOMPLETE_CAPTION) + ": " + progress + "%");
    progressLabel.getStyleClass().add("heading-medium");
        progressContent.getChildren().add(progressLabel);

        GridPane milestoneGrid = null;
        Aspect aspect = feature.getAspectForNode();
        if (aspect != null && aspect.getInfo()!=null && aspect.getInfo().getMilestoneInfo()!=null && !aspect.getInfo().getMilestoneInfo().isEmpty()) {
            List<MilestoneInfo> infoList = aspect.getInfo().getMilestoneInfo();
            FeatureMilestoneHelper.alignMilestones(feature, infoList, FeatureMilestoneHelper.todaySupplier());
            milestoneGrid = new GridPane(); milestoneGrid.setHgap(10); milestoneGrid.setVgap(5); milestoneGrid.setPadding(new Insets(5));
            milestoneGrid.add(new Label(Messages.getInstance().getMessage(Messages.JLABEL_MILESTONE)), 0, 0);
            milestoneGrid.add(new Label(Messages.getInstance().getMessage(Messages.JLABEL_MILESTONE_PLANNED)), 1, 0);
            milestoneGrid.add(new Label(Messages.getInstance().getMessage(Messages.JLABEL_MILESTONE_ACTUAL)), 2, 0);
            milestoneGrid.add(new Label(Messages.getInstance().getMessage(Messages.JLABEL_MILESTONE_COMPLETE)), 3, 0);
            for (int i=0;i<infoList.size();i++) {
                MilestoneInfo mi = infoList.get(i);
                Milestone m = feature.getMilestone().get(i);
                milestoneGrid.add(new Label(mi.getName()), 0, i+1);
                DatePicker planned = new DatePicker(); planned.setPrefWidth(120);
                if (m.getPlanned()!=null) planned.setValue(m.getPlanned().toGregorianCalendar().toZonedDateTime().toLocalDate()); else planned.setValue(LocalDate.now());
                milestoneGrid.add(planned,1,i+1);
                DatePicker actual = new DatePicker(); actual.setPrefWidth(120);
                if (m.getActual()!=null) actual.setValue(m.getActual().toGregorianCalendar().toZonedDateTime().toLocalDate());
                milestoneGrid.add(actual,2,i+1);
                CheckBox complete = new CheckBox(); if (m.getStatus()!=null) complete.setSelected(m.getStatus()==StatusEnum.COMPLETE); milestoneGrid.add(complete,3,i+1);
            }
            progressContent.getChildren().add(milestoneGrid);
        }
        progressInfo.setContent(progressContent);

        panel.getChildren().addAll(featureInfo, progressInfo);
        return new Result(panel, workPackageCombo, progressLabel, milestoneGrid, oldWP);
    }
}
