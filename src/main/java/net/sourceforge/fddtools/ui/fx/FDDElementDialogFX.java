package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;
import net.sourceforge.fddtools.internationalization.Messages;
import net.sourceforge.fddtools.model.FDDINode;


import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX version of the FDD Element Dialog.
 * This dialog is used for creating and editing FDD elements (Program, Project, Aspect, Subject, Activity, Feature).
 */
public class FDDElementDialogFX extends Stage {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDElementDialogFX.class);
    
    private TextField nameTextField;
    private TextField ownerTextField; // only for Activity / Feature
    private TextField prefixTextField; // only for Subject
    private DatePicker calendarDatePicker;
    private VBox genericInfoPanel;
    private Region progressPanel;
    private WorkPackage oldWorkPackage;
    private boolean accept = false;
    private FDDINode node;
    
    // Feature-specific controls
    private ComboBox<WorkPackage> workPackageCombo;
    // progressLabel now managed inside FeaturePanelBuilder result
    private GridPane milestoneGrid;
    
    public FDDElementDialogFX(Stage owner, FDDINode inNode) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.DECORATED);
        setTitle(Messages.getInstance().getMessage(Messages.FDD_ELEMENT_TITLE));
        setResizable(false);
        
        this.node = inNode;
        
    // Initialize controls now handled by GenericInfoPanelBuilder
        
        calendarDatePicker = new DatePicker();
        calendarDatePicker.setPrefWidth(150);
        
        // Build the dialog content
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f0f0;");
        
        // Generic info panel (always shown)
    GenericInfoPanelBuilder.Result generic = GenericInfoPanelBuilder.build(inNode);
    genericInfoPanel = generic.panel;
    nameTextField = generic.nameField;
    ownerTextField = generic.ownerField;
    prefixTextField = generic.prefixField;
        
        // Type-specific panel
        try {
            if (LOGGER.isTraceEnabled()) LOGGER.trace("Creating panel for node type: {}", inNode.getClass().getSimpleName());
            if (inNode instanceof Feature) {
                LOGGER.trace("Creating Feature panel (builder)...");
                FeaturePanelBuilder.Result featureRes = FeaturePanelBuilder.build((Feature) inNode);
                progressPanel = featureRes.panel;
                workPackageCombo = featureRes.workPackageCombo;
                milestoneGrid = featureRes.milestoneGrid;
                oldWorkPackage = featureRes.oldWorkPackage;
                LOGGER.trace("Created Feature panel via builder successfully");
            } else if (inNode instanceof Aspect) {
                Aspect aspect = (Aspect) inNode;
                if (LOGGER.isTraceEnabled()) LOGGER.trace("Creating Aspect panel for: {}", aspect.getName());
                if (LOGGER.isTraceEnabled()) LOGGER.trace("Aspect has info = {}", (aspect.getInfo() != null));
                
                // Use proper Aspect panel
                try {
                    LOGGER.trace("Using AspectInfoPanelFX...");
                    progressPanel = new AspectInfoPanelFX(aspect);
                    LOGGER.trace("AspectInfoPanelFX created successfully");
                } catch (Exception e) {
                    LOGGER.warn("ERROR creating AspectInfoPanelFX: {}", e.getMessage());
                    progressPanel = buildGenericProgressPanel();
                }
            } else if (inNode instanceof Project) {
                Project project = (Project) inNode;
                if (LOGGER.isTraceEnabled()) LOGGER.trace("Creating Project panel for: {}", project.getName());
                
                try {
                    LOGGER.trace("Using WorkPackagePanelFX with editable table...");
                    progressPanel = new WorkPackagePanelFX(project);
                    LOGGER.trace("WorkPackagePanelFX created successfully");
                } catch (Exception e) {
                    LOGGER.warn("ERROR creating WorkPackagePanelFX: {}", e.getMessage());
                    progressPanel = buildGenericProgressPanel();
                }
            } else {
                LOGGER.trace("Creating generic panel...");
                progressPanel = buildGenericProgressPanel();
                LOGGER.trace("Created generic panel successfully");
            }
        } catch (Exception e) {
            LOGGER.warn("ERROR creating type-specific panel: {}", e.getMessage());
            progressPanel = buildGenericProgressPanel();
        }
        
        // Button panel
        HBox buttonPanel = buildButtonPanel();
        
        root.setTop(genericInfoPanel);
        root.setCenter(progressPanel);
        root.setBottom(buttonPanel);
        
        Scene scene = new Scene(root);
        setScene(scene);
        sizeToScene();
    }
    
    public boolean getAccept() {
        return accept;
    }
    
    // generic info panel building is now handled by GenericInfoPanelBuilder
    
    // feature panel building now handled by FeaturePanelBuilder
    
    private VBox buildGenericProgressPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        
        // Target date (read-only)
        Label targetDateLabel = new Label(Messages.getInstance().getMessage(Messages.JLABEL_TARGETDATE_CAPTION));
        grid.add(targetDateLabel, 0, 0);
        
        Label targetDateValue = new Label();
        if (node.getTargetDate() != null) {
            Date targetDate = node.getTargetDate();
            targetDateValue.setText(targetDate.toString());
        } else {
            targetDateValue.setText("TBD");
        }
        grid.add(targetDateValue, 1, 0);
        
        // Percent complete (read-only)
        Label percentLabel = new Label(Messages.getInstance().getMessage(Messages.JLABEL_PERCENTCOMPLETE_CAPTION));
        grid.add(percentLabel, 0, 1);
        
        Label percentValue = new Label();
        if (node.getProgress() != null) {
            percentValue.setText(node.getProgress().getCompletion() + "%");
        } else {
            percentValue.setText("0%");
        }
        grid.add(percentValue, 1, 1);
        
        panel.getChildren().add(grid);
        return panel;
    }
    
    private HBox buildButtonPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER_RIGHT);
        panel.setStyle("-fx-background-color: #f0f0f0;");

        Button okButton = new Button(Messages.getInstance().getMessage(Messages.JBUTTON_OK_CAPTION));
        okButton.setDefaultButton(true);
        okButton.setPrefWidth(80);
        okButton.setOnAction(e -> handleOK());

        Button cancelButton = new Button(Messages.getInstance().getMessage(Messages.JBUTTON_CANCEL_CAPTION));
        cancelButton.setCancelButton(true);
        cancelButton.setPrefWidth(80);
        cancelButton.setOnAction(e -> handleCancel());

        panel.getChildren().addAll(okButton, cancelButton);
        return panel;
    }
    
    private void handleOK() {
        accept = true;
        node.setName(nameTextField.getText().trim());
        
        if (node instanceof Subject) {
            // Update Subject prefix
            ((Subject) node).setPrefix(prefixTextField.getText().trim());
            
        } else if (node instanceof Activity) {
            // Update Activity owner
            ((Activity) node).setInitials(ownerTextField.getText().trim());
            
        } else if (node instanceof Feature) {
            Feature feature = (Feature) node;
            
            // Update owner
            feature.setInitials(ownerTextField.getText().trim());
            
            // Update work package via helper
            if (workPackageCombo != null) {
                FeatureWorkPackageHelper.applySelection(feature, oldWorkPackage, workPackageCombo.getValue());
            }
            
            // Update milestones
            Aspect aspect = feature.getAspectForNode();
            if (aspect != null && aspect.getInfo() != null && 
                aspect.getInfo().getMilestoneInfo() != null && 
                !aspect.getInfo().getMilestoneInfo().isEmpty()) {
                
                List<MilestoneInfo> milestoneInfo = aspect.getInfo().getMilestoneInfo();
                
                // Ensure we have the right number of milestones
                FeatureMilestoneHelper.alignMilestones(feature, milestoneInfo, FeatureMilestoneHelper.todaySupplier());
                


                // Update milestones using the stored grid reference
                if (milestoneGrid != null) {
                    FeatureMilestoneApplyHelper.applyFromGrid(feature, milestoneInfo, milestoneGrid);
                }
            }
            
        } else if (node instanceof Project) {
            // Project has its own WorkPackagePanelFX
            
        } else if (node instanceof Aspect) {
            // Aspect has its own AspectInfoPanelFX
            
        } else {
            // For other node types (Program, etc.), no additional fields
            // Target date is handled by generic progress panel if applicable
        }
        
        close();
    }
    
    private void handleCancel() {
        accept = false;
        close();
    }
}