package net.sourceforge.fddtools.ui.fx;

import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.MouseButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import net.sourceforge.fddtools.internationalization.Messages;
import net.sourceforge.fddtools.model.FDDINode;
import com.nebulon.xml.fddi.Program;

/**
 * JavaFX action panel for the tree view with add/delete/edit buttons.
 * This replaces the Swing action button panel at the bottom of the tree view.
 */
public class FDDActionPanelFX extends HBox {

    private FDDActionHandler handler;

    private Button addButton;
    private Button deleteButton;
    private Button editButton;
    
    private ContextMenu programMenu;
    private MenuItem programAddItem;
    private MenuItem projectAddItem;

    public FDDActionPanelFX() {
        super(4);
        setPadding(new Insets(4));
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #888888; -fx-border-width: 1;");
        setPrefHeight(36);
        setMaxHeight(36);
        // Load custom stylesheet for improved look
        try {
            String css = getClass().getResource("/styles/action-panel.css").toExternalForm();
            getStylesheets().add(css);
        } catch (Exception ignore) { }
        createButtons();
        createContextMenu();
    }

    public void setActionHandler(FDDActionHandler handler) {
        this.handler = handler;
        setupHandlers();
    }

    private void createButtons() {
        addButton = createButton(FontAwesomeIcon.PLUS, Messages.getInstance().getMessage(Messages.JBUTTON_ADD_TOOLTIP));
        deleteButton = createButton(FontAwesomeIcon.TRASH, Messages.getInstance().getMessage(Messages.JBUTTON_DELETE_TOOLTIP));
        editButton = createButton(FontAwesomeIcon.EDIT, Messages.getInstance().getMessage(Messages.JBUTTON_EDIT_TOOLTIP));
        getChildren().addAll(addButton, deleteButton, editButton);
        getStyleClass().add("fdd-action-panel");
    }

    private Button createButton(FontAwesomeIcon icon, String tooltipText) {
        Button button = new Button();
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setGlyphSize(18); // unified size
        iconView.getStyleClass().addAll("fdd-action-icon","fdd-icon");
        button.setGraphic(iconView);
        button.setTooltip(new Tooltip(tooltipText));
        button.setFocusTraversable(false);
        button.getStyleClass().addAll("fdd-action-button","fdd-icon-button");
        button.setPrefSize(32,32);
        button.setMinSize(32,32);
        button.setMaxSize(32,32);
        return button;
    }

    private void createContextMenu() {
        programMenu = new ContextMenu();
        programAddItem = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDPROGRAM_CAPTION));
        projectAddItem = new MenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDPROJECT_CAPTION));
        
        programMenu.getItems().addAll(programAddItem, projectAddItem);
        
        programAddItem.setOnAction(e -> {
            if (handler != null) handler.onAddProgram();
        });
        
        projectAddItem.setOnAction(e -> {
            if (handler != null) handler.onAddProject();
        });
    }

    private void setupHandlers() {
        if (handler == null) return;
        
        deleteButton.setOnAction(e -> handler.onDelete());
        editButton.setOnAction(e -> handler.onEdit());
        
        // Complex add button logic matching original Swing implementation exactly
        addButton.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                FDDINode selectedNode = handler.getSelectedNode();
                if (selectedNode instanceof Program) {
                    Program program = (Program) selectedNode;
                    
                    // Enable/disable menu items based on program state (exact Swing logic)
                    if (program.getProgram().size() != 0) {
                        // Already has sub-programs, can't add projects
                        projectAddItem.setDisable(true);
                        programAddItem.setDisable(false);
                    } else if (program.getProject().size() != 0) {
                        // Already has projects, can't add sub-programs
                        programAddItem.setDisable(true);
                        projectAddItem.setDisable(false);
                    } else {
                        // Empty program, both options available
                        projectAddItem.setDisable(false);
                        programAddItem.setDisable(false);
                    }
                    
                    programMenu.show(addButton, e.getScreenX(), e.getScreenY());
                } else {
                    // For all other node types, use simple add logic
                    handler.onAdd();
                }
            }
        });
    }

    public interface FDDActionHandler {
        void onAdd();
        void onDelete();
        void onEdit();
        void onAddProgram();
        void onAddProject();
        FDDINode getSelectedNode();
    }
}
