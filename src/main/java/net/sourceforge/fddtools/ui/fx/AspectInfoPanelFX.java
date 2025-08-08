/**
 * JavaFX version of AspectInfoPanel.
 * Displays and allows editing of Aspect configuration information.
 */
package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.AspectInfo;
import com.nebulon.xml.fddi.MilestoneInfo;
import com.nebulon.xml.fddi.ObjectFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.layout.*;

public class AspectInfoPanelFX extends VBox {
    private Aspect aspect;
    private TextField subjectNameField;
    private TextField activityNameField;
    private TextField featureNameField;
    private TextField milestoneNameField;
    private TableView<MilestoneInfo> milestoneTable;
    private ObservableList<MilestoneInfo> milestoneData;
    
    public AspectInfoPanelFX(Aspect aspect) {
        super(10);
        this.aspect = aspect;
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        System.out.println("AspectInfoPanelFX: Constructor called for aspect: " + aspect.getName());
        
        try {
            // Ensure aspect has info object
            if (aspect.getInfo() == null) {
                System.out.println("AspectInfoPanelFX: Creating new AspectInfo");
                aspect.setInfo(new ObjectFactory().createAspectInfo());
                System.out.println("AspectInfoPanelFX: AspectInfo created successfully");
            } else {
                System.out.println("AspectInfoPanelFX: Aspect already has info object");
            }
            
            System.out.println("AspectInfoPanelFX: Starting initializeComponents...");
            initializeComponents();
            System.out.println("AspectInfoPanelFX: initializeComponents completed");
            
            System.out.println("AspectInfoPanelFX: Starting loadData...");
            loadData();
            System.out.println("AspectInfoPanelFX: loadData completed");
            
        } catch (Exception e) {
            System.err.println("AspectInfoPanelFX: ERROR in constructor: " + e.getMessage());
            e.printStackTrace();
            
            // Create a simple fallback UI instead of throwing
            getChildren().add(new Label("Error loading Aspect details: " + e.getMessage()));
            getChildren().add(new Label("Basic Aspect: " + aspect.getName()));
        }
    }
    
    private void initializeComponents() {
        System.out.println("AspectInfoPanelFX: initializeComponents started");
        
        // Configuration fields
        System.out.println("AspectInfoPanelFX: Creating GridPane...");
        GridPane configGrid = new GridPane();
        configGrid.setHgap(10);
        configGrid.setVgap(10);
        configGrid.setPadding(new Insets(10));
        System.out.println("AspectInfoPanelFX: GridPane created");
        
        // Subject Name
        System.out.println("AspectInfoPanelFX: Creating Subject Name field...");
        Label subjectLabel = new Label("Subject Name:");
        subjectNameField = new TextField();
        subjectNameField.setPrefWidth(200);
        configGrid.add(subjectLabel, 0, 0);
        configGrid.add(subjectNameField, 1, 0);
        System.out.println("AspectInfoPanelFX: Subject Name field created");
        
        // Activity Name
        System.out.println("AspectInfoPanelFX: Creating Activity Name field...");
        Label activityLabel = new Label("Activity Name:");
        activityNameField = new TextField();
        activityNameField.setPrefWidth(200);
        configGrid.add(activityLabel, 0, 1);
        configGrid.add(activityNameField, 1, 1);
        System.out.println("AspectInfoPanelFX: Activity Name field created");
        
        // Feature Name
        System.out.println("AspectInfoPanelFX: Creating Feature Name field...");
        Label featureLabel = new Label("Feature Name:");
        featureNameField = new TextField();
        featureNameField.setPrefWidth(200);
        configGrid.add(featureLabel, 0, 2);
        configGrid.add(featureNameField, 1, 2);
        System.out.println("AspectInfoPanelFX: Feature Name field created");
        
        // Milestone Name
        System.out.println("AspectInfoPanelFX: Creating Milestone Name field...");
        Label milestoneLabel = new Label("Milestone Name:");
        milestoneNameField = new TextField();
        milestoneNameField.setPrefWidth(200);
        configGrid.add(milestoneLabel, 0, 3);
        configGrid.add(milestoneNameField, 1, 3);
        System.out.println("AspectInfoPanelFX: Milestone Name field created");
        
        // Milestone table
        System.out.println("AspectInfoPanelFX: Creating milestone table...");
        milestoneTable = new TableView<>();
        milestoneTable.setPrefHeight(150);
        
        TableColumn<MilestoneInfo, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(150);
        
        TableColumn<MilestoneInfo, Integer> effortColumn = new TableColumn<>("Effort");
        effortColumn.setCellValueFactory(new PropertyValueFactory<>("effort"));
        effortColumn.setPrefWidth(80);
        
        milestoneTable.getColumns().add(nameColumn);
        milestoneTable.getColumns().add(effortColumn);
        System.out.println("AspectInfoPanelFX: Milestone table created");
        
        // Context menu for milestone management (right-click)
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem addItem = new MenuItem("Add Milestone");
        addItem.setOnAction(e -> addMilestone());
        
        MenuItem deleteItem = new MenuItem("Delete Milestone");
        deleteItem.setOnAction(e -> deleteMilestone());
        
        MenuItem defaultItem = new MenuItem("Set Default Milestones");
        defaultItem.setOnAction(e -> setDefaultMilestones());
        
        contextMenu.getItems().addAll(addItem, deleteItem, new SeparatorMenuItem(), defaultItem);
        milestoneTable.setContextMenu(contextMenu);
        
        // Make milestone name column editable
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            MilestoneInfo milestone = event.getRowValue();
            String newName = event.getNewValue();
            if (newName != null && !newName.trim().isEmpty()) {
                milestone.setName(newName.trim());
            }
        });
        
        // Make effort column editable
        effortColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        effortColumn.setOnEditCommit(event -> {
            MilestoneInfo milestone = event.getRowValue();
            milestone.setEffort(event.getNewValue());
        });
        
        milestoneTable.setEditable(true);
        
        // Layout
        VBox configBox = new VBox(10);
        configBox.setPadding(new Insets(10));
        configBox.getChildren().addAll(
            new Label("Aspect Configuration"),
            configGrid,
            new Label("Milestones"),
            milestoneTable
        );
        
        getChildren().add(configBox);
        System.out.println("AspectInfoPanelFX: Layout completed");
    }
    
    private void loadData() {
        System.out.println("AspectInfoPanelFX: loadData started");
        
        if (aspect.getInfo() != null) {
            System.out.println("AspectInfoPanelFX: Aspect has info object");
            AspectInfo info = aspect.getInfo();
            
            System.out.println("AspectInfoPanelFX: Loading subject name...");
            if (info.getSubjectName() != null) {
                subjectNameField.setText(info.getSubjectName());
            }
            
            System.out.println("AspectInfoPanelFX: Loading activity name...");
            if (info.getActivityName() != null) {
                activityNameField.setText(info.getActivityName());
            }
            
            System.out.println("AspectInfoPanelFX: Loading feature name...");
            if (info.getFeatureName() != null) {
                featureNameField.setText(info.getFeatureName());
            }
            
            System.out.println("AspectInfoPanelFX: Loading milestone name...");
            if (info.getMilestoneName() != null) {
                milestoneNameField.setText(info.getMilestoneName());
            }
            
            // Load milestone data
            System.out.println("AspectInfoPanelFX: Loading milestone data...");
            milestoneData = FXCollections.observableArrayList();
            if (info.getMilestoneInfo() != null) {
                System.out.println("AspectInfoPanelFX: Found " + info.getMilestoneInfo().size() + " milestones");
                milestoneData.addAll(info.getMilestoneInfo());
            } else {
                System.out.println("AspectInfoPanelFX: No milestone info found");
            }
            milestoneTable.setItems(milestoneData);
            System.out.println("AspectInfoPanelFX: Milestone data loaded");
        } else {
            System.out.println("AspectInfoPanelFX: Aspect has no info object");
        }
        
        System.out.println("AspectInfoPanelFX: Adding listeners...");
        // Add listeners for text fields
        subjectNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (aspect.getInfo() != null && newVal != null) {
                aspect.getInfo().setSubjectName(newVal.trim());
            }
        });
        
        activityNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (aspect.getInfo() != null && newVal != null) {
                aspect.getInfo().setActivityName(newVal.trim());
            }
        });
        
        featureNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (aspect.getInfo() != null && newVal != null) {
                aspect.getInfo().setFeatureName(newVal.trim());
            }
        });
        
        milestoneNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (aspect.getInfo() != null && newVal != null) {
                aspect.getInfo().setMilestoneName(newVal.trim());
            }
        });
        
        System.out.println("AspectInfoPanelFX: loadData completed");
    }
    
    private void addMilestone() {
        System.out.println("AspectInfoPanelFX: addMilestone called");
        MilestoneInfo milestone = new ObjectFactory().createMilestoneInfo();
        milestone.setName("New Milestone");
        milestone.setEffort(0);
        
        if (aspect.getInfo() != null) {
            aspect.getInfo().getMilestoneInfo().add(milestone);
            milestoneData.add(milestone);
        }
    }
    
    private void deleteMilestone() {
        System.out.println("AspectInfoPanelFX: deleteMilestone called");
        MilestoneInfo selected = milestoneTable.getSelectionModel().getSelectedItem();
        if (selected != null && aspect.getInfo() != null && aspect.getInfo().getMilestoneInfo() != null) {
            aspect.getInfo().getMilestoneInfo().remove(selected);
            milestoneData.remove(selected);
        }
    }
    
    private void setDefaultMilestones() {
        System.out.println("AspectInfoPanelFX: setDefaultMilestones called");
        aspect.setStandardMilestones();
        
        // Refresh the UI
        if (aspect.getInfo() != null && aspect.getInfo().getMilestoneInfo() != null) {
            milestoneData.clear();
            milestoneData.addAll(aspect.getInfo().getMilestoneInfo());
        }
    }
    
    /**
     * Get the aspect associated with this panel.
     */
    public Aspect getAspect() {
        return aspect;
    }
}