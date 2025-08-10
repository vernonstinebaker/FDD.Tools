package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.Project;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import net.sourceforge.fddtools.command.CommandExecutionService;
import net.sourceforge.fddtools.commands.workpackage.AddWorkPackageCommand;
import net.sourceforge.fddtools.commands.workpackage.DeleteWorkPackageCommand;
import net.sourceforge.fddtools.commands.workpackage.RenameWorkPackageCommand;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;
import net.sourceforge.fddtools.internationalization.Messages;
import net.sourceforge.fddtools.util.I18n;

/**
 * JavaFX version of WorkPackagePanel that matches Swing interface behavior.
 * Provides an editable table for work packages without Add/Delete buttons.
 */
public class WorkPackagePanelFX extends VBox {
    private final Project project;
    private final TableView<WorkPackage> workPackageTable;
    private final ObservableList<WorkPackage> workPackages;

    private final TableColumn<WorkPackage, String> nameColumn;
    private final TableColumn<WorkPackage, String> featuresColumn;

    public WorkPackagePanelFX(Project project) {
        super(10);
        this.project = project;
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        // Initialize components
        workPackageTable = new TableView<>();
        workPackageTable.setPrefHeight(200);

        // Initialize data
        workPackages = FXCollections.observableArrayList();
        loadWorkPackages();

        // Create columns
        nameColumn = new TableColumn<>(Messages.getInstance().getMessage(Messages.JLABEL_NAME_CAPTION));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(300);
        
        // Make name column editable like Swing
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            WorkPackage wp = event.getRowValue();
            String newName = event.getNewValue();
            if (newName != null && !newName.trim().isEmpty() && !newName.equals(wp.getName())) {
                CommandExecutionService.getInstance().execute(new RenameWorkPackageCommand(wp, newName.trim()));
                workPackageTable.refresh();
            }
        });

        // Add context menu for Add/Delete operations
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem addItem = new MenuItem(I18n.get("WorkPackage.Context.Add"));
        addItem.setOnAction(e -> addWorkPackage());
        
        MenuItem deleteItem = new MenuItem(I18n.get("WorkPackage.Context.Delete"));
        deleteItem.setOnAction(e -> deleteSelectedWorkPackage());
        
        contextMenu.getItems().addAll(addItem, deleteItem);
        
        workPackageTable.setContextMenu(contextMenu);
        
        // Enable editing on double-click
        workPackageTable.setEditable(true);



        featuresColumn = new TableColumn<>(I18n.get("WorkPackage.Column.Features"));
        featuresColumn.setCellValueFactory(cellData -> {
            WorkPackage wp = cellData.getValue();
            int count = wp.getFeatureList().size();
            return new SimpleStringProperty(String.valueOf(count));
        });
        featuresColumn.setPrefWidth(100);

        // Add columns to table
        workPackageTable.getColumns().add(nameColumn);
        workPackageTable.getColumns().add(featuresColumn);
        workPackageTable.setItems(workPackages);
        workPackageTable.setEditable(true);

        // Layout
        VBox.setVgrow(workPackageTable, Priority.ALWAYS);
        getChildren().addAll(
            new Label(I18n.get("WorkPackage.Panel.Title")) {{
                setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            }},
            workPackageTable
        );
    }

    private void loadWorkPackages() {
        workPackages.clear();
        if (project != null && project.getWorkPackages() != null) {
            workPackages.addAll(project.getWorkPackages());
        }
    }

    private void addWorkPackage() {
        WorkPackage newWorkPackage = new WorkPackage();
        newWorkPackage.setName(I18n.get("WorkPackage.New.Default"));
        CommandExecutionService.getInstance().execute(new AddWorkPackageCommand(project, newWorkPackage));
        workPackages.add(newWorkPackage);
    }

    private void deleteSelectedWorkPackage() {
        WorkPackage selected = workPackageTable.getSelectionModel().getSelectedItem();
        if (selected != null && 
            !"Unassigned".equals(selected.getName()) &&
            !Messages.getInstance().getMessage(Messages.UNASSIGNED_WORKPACKAGE_NAME).equals(selected.getName())) {
            CommandExecutionService.getInstance().execute(new DeleteWorkPackageCommand(project, selected));
            workPackages.remove(selected);
        }
    }
    
    /**
     * Get the project associated with this panel.
     */
    public Project getProject() {
        return project;
    }
}