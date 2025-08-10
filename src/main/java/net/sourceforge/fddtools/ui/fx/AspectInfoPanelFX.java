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
import net.sourceforge.fddtools.util.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AspectInfoPanelFX extends VBox {
    private static final Logger LOGGER = LoggerFactory.getLogger(AspectInfoPanelFX.class);
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
        
    if (LOGGER.isTraceEnabled()) LOGGER.trace("Constructor called for aspect: {}", aspect.getName());
        
        try {
            // Ensure aspect has info object
            if (aspect.getInfo() == null) {
                LOGGER.trace("Creating new AspectInfo");
                aspect.setInfo(new ObjectFactory().createAspectInfo());
                LOGGER.trace("AspectInfo created successfully");
            } else {
                LOGGER.trace("Aspect already has info object");
            }
            
            LOGGER.trace("Starting initializeComponents...");
            initializeComponents();
            LOGGER.trace("initializeComponents completed");
            
            LOGGER.trace("Starting loadData...");
            loadData();
            LOGGER.trace("loadData completed");
            
        } catch (Exception e) {
            LOGGER.warn("ERROR in constructor: {}", e.getMessage());
            
            // Create a simple fallback UI instead of throwing
            getChildren().add(new Label(I18n.get("AspectInfo.ErrorLoading") + " " + e.getMessage()));
            getChildren().add(new Label(I18n.get("AspectInfo.BasicAspect") + " " + aspect.getName()));
        }
    }
    private void initializeComponents() {
    LOGGER.trace("initializeComponents started");
        
        // Configuration fields
    LOGGER.trace("Creating GridPane...");
        GridPane configGrid = new GridPane();
        configGrid.setHgap(10);
        configGrid.setVgap(10);
        configGrid.setPadding(new Insets(10));
    LOGGER.trace("GridPane created");
        
        // Subject Name
    LOGGER.trace("Creating Subject Name field...");
        Label subjectLabel = new Label(I18n.get("AspectInfo.SubjectName.Label"));
        subjectNameField = new TextField();
        subjectNameField.setPrefWidth(200);
        configGrid.add(subjectLabel, 0, 0);
        configGrid.add(subjectNameField, 1, 0);
    LOGGER.trace("Subject Name field created");
        
        // Activity Name
    LOGGER.trace("Creating Activity Name field...");
        Label activityLabel = new Label(I18n.get("AspectInfo.ActivityName.Label"));
        activityNameField = new TextField();
        activityNameField.setPrefWidth(200);
        configGrid.add(activityLabel, 0, 1);
        configGrid.add(activityNameField, 1, 1);
    LOGGER.trace("Activity Name field created");
        
        // Feature Name
    LOGGER.trace("Creating Feature Name field...");
        Label featureLabel = new Label(I18n.get("AspectInfo.FeatureName.Label"));
        featureNameField = new TextField();
        featureNameField.setPrefWidth(200);
        configGrid.add(featureLabel, 0, 2);
        configGrid.add(featureNameField, 1, 2);
    LOGGER.trace("Feature Name field created");
        
        // Milestone Name
    LOGGER.trace("Creating Milestone Name field...");
        Label milestoneLabel = new Label(I18n.get("AspectInfo.MilestoneName.Label"));
        milestoneNameField = new TextField();
        milestoneNameField.setPrefWidth(200);
        configGrid.add(milestoneLabel, 0, 3);
        configGrid.add(milestoneNameField, 1, 3);
    LOGGER.trace("Milestone Name field created");
        
        // Milestone table
    LOGGER.trace("Creating milestone table...");
        milestoneTable = new TableView<>();
        milestoneTable.setPrefHeight(150);
        
        TableColumn<MilestoneInfo, String> nameColumn = new TableColumn<>(I18n.get("AspectInfo.Table.MilestoneName"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(150);
        
        TableColumn<MilestoneInfo, Integer> effortColumn = new TableColumn<>(I18n.get("AspectInfo.Table.MilestoneEffort"));
        effortColumn.setCellValueFactory(new PropertyValueFactory<>("effort"));
        effortColumn.setPrefWidth(80);
        
        milestoneTable.getColumns().add(nameColumn);
        milestoneTable.getColumns().add(effortColumn);
    LOGGER.trace("Milestone table created");
        
        // Context menu for milestone management (right-click)
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem addItem = new MenuItem(I18n.get("AspectInfo.Context.AddMilestone"));
        addItem.setOnAction(e -> addMilestone());
        
        MenuItem deleteItem = new MenuItem(I18n.get("AspectInfo.Context.DeleteMilestone"));
        deleteItem.setOnAction(e -> deleteMilestone());
        
        MenuItem defaultItem = new MenuItem(I18n.get("AspectInfo.Context.DefaultMilestones"));
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
            new Label(I18n.get("AspectInfo.Section.Configuration")),
            configGrid,
            new Label(I18n.get("AspectInfo.Section.Milestones")),
            milestoneTable
        );
        
        getChildren().add(configBox);
    LOGGER.trace("Layout completed");
    }
    
    private void loadData() {
    LOGGER.trace("loadData started");
        
        if (aspect.getInfo() != null) {
            LOGGER.trace("Aspect has info object");
            AspectInfo info = aspect.getInfo();
            
            LOGGER.trace("Loading subject name...");
            if (info.getSubjectName() != null) {
                subjectNameField.setText(info.getSubjectName());
            }
            
            LOGGER.trace("Loading activity name...");
            if (info.getActivityName() != null) {
                activityNameField.setText(info.getActivityName());
            }
            
            LOGGER.trace("Loading feature name...");
            if (info.getFeatureName() != null) {
                featureNameField.setText(info.getFeatureName());
            }
            
            LOGGER.trace("Loading milestone name...");
            if (info.getMilestoneName() != null) {
                milestoneNameField.setText(info.getMilestoneName());
            }
            
            // Load milestone data
            LOGGER.trace("Loading milestone data...");
            milestoneData = FXCollections.observableArrayList();
            if (info.getMilestoneInfo() != null) {
                if (LOGGER.isTraceEnabled()) LOGGER.trace("Found {} milestones", info.getMilestoneInfo().size());
                milestoneData.addAll(info.getMilestoneInfo());
            } else {
                LOGGER.trace("No milestone info found");
            }
            milestoneTable.setItems(milestoneData);
            LOGGER.trace("Milestone data loaded");
        } else {
            LOGGER.trace("Aspect has no info object");
        }
        
    LOGGER.trace("Adding listeners...");
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
        
    LOGGER.trace("loadData completed");
    }
    
    private void addMilestone() {
    LOGGER.trace("addMilestone called");
        MilestoneInfo milestone = new ObjectFactory().createMilestoneInfo();
        milestone.setName("New Milestone");
        milestone.setEffort(0);
        
        if (aspect.getInfo() != null) {
            aspect.getInfo().getMilestoneInfo().add(milestone);
            milestoneData.add(milestone);
        }
    }
    
    private void deleteMilestone() {
    LOGGER.trace("deleteMilestone called");
        MilestoneInfo selected = milestoneTable.getSelectionModel().getSelectedItem();
        if (selected != null && aspect.getInfo() != null && aspect.getInfo().getMilestoneInfo() != null) {
            aspect.getInfo().getMilestoneInfo().remove(selected);
            milestoneData.remove(selected);
        }
    }
    
    private void setDefaultMilestones() {
    LOGGER.trace("setDefaultMilestones called");
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