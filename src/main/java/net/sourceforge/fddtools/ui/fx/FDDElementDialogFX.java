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

import java.time.LocalDate;

import java.util.Date;
import java.util.List;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * JavaFX version of the FDD Element Dialog.
 * This dialog is used for creating and editing FDD elements (Program, Project, Aspect, Subject, Activity, Feature).
 */
public class FDDElementDialogFX extends Stage {
    
    private TextField nameTextField;
    private TextField ownerTextField;
    private TextField prefixTextField;
    private DatePicker calendarDatePicker;
    private VBox genericInfoPanel;
    private Region progressPanel;
    private WorkPackage oldWorkPackage;
    private boolean accept = false;
    private FDDINode node;
    
    // Feature-specific controls
    private ComboBox<WorkPackage> workPackageCombo;
    private Label progressLabel;
    private GridPane milestoneGrid;
    
    public FDDElementDialogFX(Stage owner, FDDINode inNode) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.DECORATED);
        setTitle(Messages.getInstance().getMessage(Messages.FDD_ELEMENT_TITLE));
        setResizable(false);
        
        this.node = inNode;
        
        // Initialize controls
        nameTextField = new TextField();
        nameTextField.setPrefWidth(300);
        if (inNode.getName() != null) {
            nameTextField.setText(inNode.getName());
        }
        
        ownerTextField = new TextField();
        ownerTextField.setPrefWidth(50);
        
        prefixTextField = new TextField();
        prefixTextField.setPrefWidth(50);
        
        calendarDatePicker = new DatePicker();
        calendarDatePicker.setPrefWidth(150);
        
        // Build the dialog content
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f0f0;");
        
        // Generic info panel (always shown)
        genericInfoPanel = buildGenericInfoPanel();
        
        // Type-specific panel
        try {
            System.out.println("FDDElementDialogFX: Creating panel for node type: " + inNode.getClass().getSimpleName());
            if (inNode instanceof Feature) {
                System.out.println("FDDElementDialogFX: Creating Feature panel...");
                progressPanel = buildFeaturePanel();
                System.out.println("FDDElementDialogFX: Created Feature panel successfully");
            } else if (inNode instanceof Aspect) {
                Aspect aspect = (Aspect) inNode;
                System.out.println("FDDElementDialogFX: Creating Aspect panel for: " + aspect.getName());
                System.out.println("FDDElementDialogFX: Aspect has info = " + (aspect.getInfo() != null));
                
                // Use proper Aspect panel
                try {
                    System.out.println("FDDElementDialogFX: Using AspectInfoPanelFX...");
                    progressPanel = new AspectInfoPanelFX(aspect);
                    System.out.println("FDDElementDialogFX: AspectInfoPanelFX created successfully");
                } catch (Exception e) {
                    System.err.println("FDDElementDialogFX: ERROR creating AspectInfoPanelFX: " + e.getMessage());
                    e.printStackTrace();
                    progressPanel = buildGenericProgressPanel();
                }
            } else if (inNode instanceof Project) {
                Project project = (Project) inNode;
                System.out.println("FDDElementDialogFX: Creating Project panel for: " + project.getName());
                
                try {
                    System.out.println("FDDElementDialogFX: Using WorkPackagePanelFX with editable table...");
                    progressPanel = new WorkPackagePanelFX(project);
                    System.out.println("FDDElementDialogFX: WorkPackagePanelFX created successfully");
                } catch (Exception e) {
                    System.err.println("FDDElementDialogFX: ERROR creating WorkPackagePanelFX: " + e.getMessage());
                    e.printStackTrace();
                    progressPanel = buildGenericProgressPanel();
                }
            } else {
                System.out.println("FDDElementDialogFX: Creating generic panel...");
                progressPanel = buildGenericProgressPanel();
                System.out.println("FDDElementDialogFX: Created generic panel successfully");
            }
        } catch (Exception e) {
            System.err.println("FDDElementDialogFX: ERROR creating type-specific panel: " + e.getMessage());
            e.printStackTrace();
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
    
    private VBox buildGenericInfoPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        
        // Subject prefix field
        if (node instanceof Subject) {
            Label prefixLabel = new Label(Messages.getInstance().getMessage(Messages.JLABEL_PREFIX_TITLE));
            grid.add(prefixLabel, 0, 0);
            grid.add(prefixTextField, 1, 0);
            if (((Subject) node).getPrefix() != null) {
                prefixTextField.setText(((Subject) node).getPrefix());
            }
            
            Label nameLabel = new Label(Messages.getInstance().getMessage(Messages.JLABEL_NAME_CAPTION));
            grid.add(nameLabel, 0, 1);
            grid.add(nameTextField, 1, 1);
        } else {
            Label nameLabel = new Label(Messages.getInstance().getMessage(Messages.JLABEL_NAME_CAPTION));
            grid.add(nameLabel, 0, 0);
            grid.add(nameTextField, 1, 0);
            
            // Owner field for Activity and Feature
            if (node instanceof Activity || node instanceof Feature) {
                Label ownerLabel = new Label(Messages.getInstance().getMessage(Messages.JLABEL_OWNER_CAPTION));
                grid.add(ownerLabel, 0, 1);
                grid.add(ownerTextField, 1, 1);
                
                if (node instanceof Activity && ((Activity) node).getInitials() != null) {
                    ownerTextField.setText(((Activity) node).getInitials());
                } else if (node instanceof Feature && ((Feature) node).getInitials() != null) {
                    ownerTextField.setText(((Feature) node).getInitials());
                }
            }
        }
        
        panel.getChildren().add(grid);
        return panel;
    }
    
    private VBox buildFeaturePanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        Feature feature = (Feature) node;
        
        // Feature info section
        TitledPane featureInfo = new TitledPane();
        featureInfo.setText(Messages.getInstance().getMessage(Messages.JPANEL_INFO_TITLE));
        featureInfo.setCollapsible(false);
        
        GridPane featureGrid = new GridPane();
        featureGrid.setHgap(10);
        featureGrid.setVgap(5);
        featureGrid.setPadding(new Insets(10));
        
        // Owner
        Label ownerLabel = new Label(Messages.getInstance().getMessage(Messages.JLABEL_OWNER_CAPTION));
        featureGrid.add(ownerLabel, 0, 0);
        featureGrid.add(ownerTextField, 1, 0);
        if (feature.getInitials() != null) {
            ownerTextField.setText(feature.getInitials());
        }
        
        // Work Package
        Label workPackageLabel = new Label(Messages.getInstance().getMessage(Messages.JLABEL_WORKPACKAGE_TITLE));
        workPackageCombo = new ComboBox<>();
        workPackageCombo.setPrefWidth(200);
        
        // Get project and populate work packages
        Project project = (Project) node.getParent().getParent().getParent().getParent();
        List<WorkPackage> workPackages = project.getWorkPackages();
        
        // Add unassigned option
        WorkPackage unassignedWorkPackage = new WorkPackage();
        unassignedWorkPackage.setName(Messages.getInstance().getMessage(Messages.UNASSIGNED_WORKPACKAGE_NAME));
        workPackageCombo.getItems().add(unassignedWorkPackage);
        workPackageCombo.getItems().addAll(workPackages);
        
        // Find current work package
        workPackageCombo.setValue(unassignedWorkPackage);
        for (WorkPackage wp : workPackages) {
            if (wp.getFeatureList().contains(feature.getSeq())) {
                workPackageCombo.setValue(wp);
                oldWorkPackage = wp;
                break;
            }
        }
        
        featureGrid.add(workPackageLabel, 0, 1);
        featureGrid.add(workPackageCombo, 1, 1);
        
        featureInfo.setContent(featureGrid);
        
        // Progress section with milestones
        TitledPane progressInfo = new TitledPane();
        progressInfo.setText(Messages.getInstance().getMessage(Messages.JPANEL_PROGRESS_TITLE));
        progressInfo.setCollapsible(false);
        
        VBox progressContent = new VBox(10);
        progressContent.setPadding(new Insets(10));
        
        // Progress display
        progressLabel = new Label();
        updateProgressLabel(feature);
        progressLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        progressContent.getChildren().add(progressLabel);
        
        // Milestone management
        Aspect aspect = feature.getAspectForNode();
        if (aspect != null && aspect.getInfo() != null && 
            aspect.getInfo().getMilestoneInfo() != null && 
            !aspect.getInfo().getMilestoneInfo().isEmpty()) {
            
            List<MilestoneInfo> milestoneInfo = aspect.getInfo().getMilestoneInfo();
            
            // Ensure existing milestones have planned dates
            for (Milestone milestone : feature.getMilestone()) {
                if (milestone.getPlanned() == null) {
                    try {
                        GregorianCalendar cal = new GregorianCalendar();
                        XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance()
                            .newXMLGregorianCalendar(cal);
                        milestone.setPlanned(xmlDate);
                        if (milestone.getStatus() == null) {
                            milestone.setStatus(StatusEnum.NOTSTARTED);
                        }
                    } catch (Exception e) {
                        System.err.println("Error initializing milestone: " + e.getMessage());
                    }
                }
            }
            ObjectFactory of = new ObjectFactory();
            try {
                // Ensure feature has exactly the right number of milestones
                while (feature.getMilestone().size() > milestoneInfo.size()) {
                    feature.getMilestone().remove(feature.getMilestone().size() - 1);
                }
                
                while (feature.getMilestone().size() < milestoneInfo.size()) {
                    Milestone newMilestone = of.createMilestone();
                    // Set default planned date to today for new milestones
                    GregorianCalendar cal = new GregorianCalendar();
                    try {
                        XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance()
                            .newXMLGregorianCalendar(cal);
                        newMilestone.setPlanned(xmlDate);
                    } catch (DatatypeConfigurationException e) {
                        System.err.println("Error creating XML date: " + e.getMessage());
                        e.printStackTrace();
                    }
                    newMilestone.setStatus(StatusEnum.NOTSTARTED);
                    feature.getMilestone().add(newMilestone);
                }
                
                // Ensure all milestones have required fields
                for (Milestone milestone : feature.getMilestone()) {
                    if (milestone.getPlanned() == null) {
                        GregorianCalendar cal = new GregorianCalendar();
                        XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance()
                            .newXMLGregorianCalendar(cal);
                        milestone.setPlanned(xmlDate);
                    }
                    if (milestone.getStatus() == null) {
                        milestone.setStatus(StatusEnum.NOTSTARTED);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error managing milestones: " + e.getMessage());
                e.printStackTrace();
            }


            
            // Create milestone grid and store reference
            milestoneGrid = new GridPane();
            milestoneGrid.setHgap(10);
            milestoneGrid.setVgap(5);
            milestoneGrid.setPadding(new Insets(5));
            
            // Headers
            milestoneGrid.add(new Label(Messages.getInstance().getMessage(Messages.JLABEL_MILESTONE)), 0, 0);
            milestoneGrid.add(new Label(Messages.getInstance().getMessage(Messages.JLABEL_MILESTONE_PLANNED)), 1, 0);
            milestoneGrid.add(new Label(Messages.getInstance().getMessage(Messages.JLABEL_MILESTONE_ACTUAL)), 2, 0);
            milestoneGrid.add(new Label(Messages.getInstance().getMessage(Messages.JLABEL_MILESTONE_COMPLETE)), 3, 0);
            
            // Milestone rows
            for (int i = 0; i < milestoneInfo.size(); i++) {
                MilestoneInfo info = milestoneInfo.get(i);
                Milestone milestone = feature.getMilestone().get(i);
                
                // Milestone name
                milestoneGrid.add(new Label(info.getName()), 0, i + 1);
                
                // Planned date
                DatePicker plannedPicker = new DatePicker();
                plannedPicker.setPrefWidth(120);
                if (milestone.getPlanned() != null) {
                    plannedPicker.setValue(milestone.getPlanned().toGregorianCalendar().toZonedDateTime().toLocalDate());
                } else {
                    plannedPicker.setValue(LocalDate.now());
                }
                plannedPicker.setUserData("planned_" + i);
                milestoneGrid.add(plannedPicker, 1, i + 1);
                
                // Actual date
                DatePicker actualPicker = new DatePicker();
                actualPicker.setPrefWidth(120);
                if (milestone.getActual() != null) {
                    actualPicker.setValue(milestone.getActual().toGregorianCalendar().toZonedDateTime().toLocalDate());
                }
                actualPicker.setUserData("actual_" + i);
                milestoneGrid.add(actualPicker, 2, i + 1);
                
                // Complete checkbox
                CheckBox completeCheck = new CheckBox();
                completeCheck.setUserData("complete_" + i);
                if (milestone.getStatus() != null) {
                    completeCheck.setSelected(milestone.getStatus() == StatusEnum.COMPLETE);
                }
                milestoneGrid.add(completeCheck, 3, i + 1);
            }
            
            progressContent.getChildren().add(milestoneGrid);
        }
        
        progressInfo.setContent(progressContent);
        
        panel.getChildren().addAll(featureInfo, progressInfo);
        
        return panel;
    }
    
    private void updateProgressLabel(Feature feature) {
        int progress = 0;
        if (feature.getProgress() != null) {
            progress = feature.getProgress().getCompletion();
        }
        progressLabel.setText(Messages.getInstance().getMessage(Messages.JLABEL_PERCENTCOMPLETE_CAPTION) + ": " + progress + "%");
    }
    
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
            
            // Update work package
            if (workPackageCombo != null) {
                WorkPackage workPackage = workPackageCombo.getValue();
                if (workPackage != null && workPackage != oldWorkPackage) {
                    Integer featureSeq = feature.getSeq();
                    if (oldWorkPackage != null) {
                        oldWorkPackage.getFeatureList().remove(featureSeq);
                    }
                    if (!workPackage.getName().equals(Messages.getInstance().getMessage(Messages.UNASSIGNED_WORKPACKAGE_NAME))) {
                        workPackage.addFeature(featureSeq);
                    }
                }
            }
            
            // Update milestones
            Aspect aspect = feature.getAspectForNode();
            if (aspect != null && aspect.getInfo() != null && 
                aspect.getInfo().getMilestoneInfo() != null && 
                !aspect.getInfo().getMilestoneInfo().isEmpty()) {
                
                List<MilestoneInfo> milestoneInfo = aspect.getInfo().getMilestoneInfo();
                
                // Ensure we have the right number of milestones
                while (feature.getMilestone().size() > milestoneInfo.size()) {
                    feature.getMilestone().remove(feature.getMilestone().size() - 1);
                }
                while (feature.getMilestone().size() < milestoneInfo.size()) {
                    Milestone newMilestone = new ObjectFactory().createMilestone();
                    GregorianCalendar cal = new GregorianCalendar();
                    try {
                        XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance()
                            .newXMLGregorianCalendar(cal);
                        newMilestone.setPlanned(xmlDate);
                    } catch (DatatypeConfigurationException e) {
                        System.err.println("Error creating XML date: " + e.getMessage());
                        e.printStackTrace();
                    }
                    newMilestone.setStatus(StatusEnum.NOTSTARTED);
                    feature.getMilestone().add(newMilestone);
                }
                


                // Update milestones using the stored grid reference
                if (milestoneGrid != null) {
                    for (int i = 0; i < milestoneInfo.size() && i < feature.getMilestone().size(); i++) {
                        Milestone milestone = feature.getMilestone().get(i);
                        
                        try {
                            // Get controls by row and column
                            DatePicker plannedPicker = null;
                            DatePicker actualPicker = null;
                            CheckBox completeCheck = null;
                            
                            for (javafx.scene.Node node : milestoneGrid.getChildren()) {
                                Integer row = GridPane.getRowIndex(node);
                                Integer col = GridPane.getColumnIndex(node);
                                if (row != null && row == i + 1) {
                                    if (col == 1 && node instanceof DatePicker) {
                                        plannedPicker = (DatePicker) node;
                                    } else if (col == 2 && node instanceof DatePicker) {
                                        actualPicker = (DatePicker) node;
                                    } else if (col == 3 && node instanceof CheckBox) {
                                        completeCheck = (CheckBox) node;
                                    }
                                }
                            }
                            
                            if (plannedPicker != null) {
                                // Update planned date
                                if (plannedPicker.getValue() != null) {
                                    GregorianCalendar cal = new GregorianCalendar();
                                    cal.set(plannedPicker.getValue().getYear(), 
                                           plannedPicker.getValue().getMonthValue() - 1, 
                                           plannedPicker.getValue().getDayOfMonth());
                                    XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance()
                                        .newXMLGregorianCalendar(cal);
                                    milestone.setPlanned(xmlDate);
                                }
                            }
                            
                            if (actualPicker != null) {
                                // Update actual date
                                if (actualPicker.getValue() != null) {
                                    GregorianCalendar cal = new GregorianCalendar();
                                    cal.set(actualPicker.getValue().getYear(), 
                                           actualPicker.getValue().getMonthValue() - 1, 
                                           actualPicker.getValue().getDayOfMonth());
                                    XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance()
                                        .newXMLGregorianCalendar(cal);
                                    milestone.setActual(xmlDate);
                                } else {
                                    milestone.setActual(null);
                                }
                            }
                            
                            if (completeCheck != null) {
                                // Update status
                                milestone.setStatus(completeCheck.isSelected() ? 
                                    StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
                            }
                            
                        } catch (Exception e) {
                            System.err.println("Error updating milestone " + i + ": " + e.getMessage());
                                     }
            }
            
            // Recalculate progress and target date
            feature.calculateProgress();
            feature.calculateTargetDate();                        }
                    }
            
        } else if (node instanceof Activity) {
            // Update Activity owner
            ((Activity) node).setInitials(ownerTextField.getText().trim());
            
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