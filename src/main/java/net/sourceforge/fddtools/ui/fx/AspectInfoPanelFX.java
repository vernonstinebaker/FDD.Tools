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
import java.util.logging.Logger;

public class AspectInfoPanelFX extends VBox {
    private static final Logger LOGGER = Logger.getLogger(AspectInfoPanelFX.class.getName());
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
        
    LOGGER.finer(() -> "Constructor called for aspect: " + aspect.getName());
        
        try {
            // Ensure aspect has info object
            if (aspect.getInfo() == null) {
                LOGGER.finest("Creating new AspectInfo");
                aspect.setInfo(new ObjectFactory().createAspectInfo());
                LOGGER.finest("AspectInfo created successfully");
            } else {
                LOGGER.finest("Aspect already has info object");
            }
            
            LOGGER.finest("Starting initializeComponents...");
            initializeComponents();
            LOGGER.finest("initializeComponents completed");
            
            LOGGER.finest("Starting loadData...");
            loadData();
            LOGGER.finest("loadData completed");
            
        } catch (Exception e) {
            LOGGER.warning("ERROR in constructor: " + e.getMessage());
            
            // Create a simple fallback UI instead of throwing
            getChildren().add(new Label("Error loading Aspect details: " + e.getMessage()));
            getChildren().add(new Label("Basic Aspect: " + aspect.getName()));
        }
    }
    
    private void initializeComponents() {
    LOGGER.finest("initializeComponents started");
        
        // Configuration fields
    LOGGER.finest("Creating GridPane...");
        GridPane configGrid = new GridPane();
        configGrid.setHgap(10);
        configGrid.setVgap(10);
        configGrid.setPadding(new Insets(10));
    LOGGER.finest("GridPane created");
        
        // Subject Name
    LOGGER.finest("Creating Subject Name field...");
        Label subjectLabel = new Label("Subject Name:");
        subjectNameField = new TextField();
        subjectNameField.setPrefWidth(200);
        configGrid.add(subjectLabel, 0, 0);
        configGrid.add(subjectNameField, 1, 0);
    LOGGER.finest("Subject Name field created");
        
        // Activity Name
    LOGGER.finest("Creating Activity Name field...");
        Label activityLabel = new Label("Activity Name:");
        activityNameField = new TextField();
        activityNameField.setPrefWidth(200);
        configGrid.add(activityLabel, 0, 1);
        configGrid.add(activityNameField, 1, 1);
    LOGGER.finest("Activity Name field created");
        
        // Feature Name
    LOGGER.finest("Creating Feature Name field...");
        Label featureLabel = new Label("Feature Name:");
        featureNameField = new TextField();
        featureNameField.setPrefWidth(200);
        configGrid.add(featureLabel, 0, 2);
        configGrid.add(featureNameField, 1, 2);
    LOGGER.finest("Feature Name field created");
        
        // Milestone Name
    LOGGER.finest("Creating Milestone Name field...");
        Label milestoneLabel = new Label("Milestone Name:");
        milestoneNameField = new TextField();
        milestoneNameField.setPrefWidth(200);
        configGrid.add(milestoneLabel, 0, 3);
        configGrid.add(milestoneNameField, 1, 3);
    LOGGER.finest("Milestone Name field created");
        
        // Milestone table
    LOGGER.finest("Creating milestone table...");
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
    LOGGER.finest("Milestone table created");
        
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
    LOGGER.finest("Layout completed");
    }
    
    private void loadData() {
    LOGGER.finest("loadData started");
        
        if (aspect.getInfo() != null) {
            LOGGER.finest("Aspect has info object");
            AspectInfo info = aspect.getInfo();
            
            LOGGER.finest("Loading subject name...");
            if (info.getSubjectName() != null) {
                subjectNameField.setText(info.getSubjectName());
            }
            
            LOGGER.finest("Loading activity name...");
            if (info.getActivityName() != null) {
                activityNameField.setText(info.getActivityName());
            }
            
            LOGGER.finest("Loading feature name...");
            if (info.getFeatureName() != null) {
                featureNameField.setText(info.getFeatureName());
            }
            
            LOGGER.finest("Loading milestone name...");
            if (info.getMilestoneName() != null) {
                milestoneNameField.setText(info.getMilestoneName());
            }
            
            // Load milestone data
            LOGGER.finest("Loading milestone data...");
            milestoneData = FXCollections.observableArrayList();
            if (info.getMilestoneInfo() != null) {
                LOGGER.finest(() -> "Found " + info.getMilestoneInfo().size() + " milestones");
                milestoneData.addAll(info.getMilestoneInfo());
            } else {
                LOGGER.finest("No milestone info found");
            }
            milestoneTable.setItems(milestoneData);
            LOGGER.finest("Milestone data loaded");
        } else {
            LOGGER.finest("Aspect has no info object");
        }
        
    LOGGER.finest("Adding listeners...");
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
        
    LOGGER.finest("loadData completed");
    }
    
    private void addMilestone() {
    LOGGER.finest("addMilestone called");
        MilestoneInfo milestone = new ObjectFactory().createMilestoneInfo();
        milestone.setName("New Milestone");
        milestone.setEffort(0);
        
        if (aspect.getInfo() != null) {
            aspect.getInfo().getMilestoneInfo().add(milestone);
            milestoneData.add(milestone);
        }
    }
    
    private void deleteMilestone() {
    LOGGER.finest("deleteMilestone called");
        MilestoneInfo selected = milestoneTable.getSelectionModel().getSelectedItem();
        if (selected != null && aspect.getInfo() != null && aspect.getInfo().getMilestoneInfo() != null) {
            aspect.getInfo().getMilestoneInfo().remove(selected);
            milestoneData.remove(selected);
        }
    }
    
    private void setDefaultMilestones() {
    LOGGER.finest("setDefaultMilestones called");
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