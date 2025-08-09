/**
 * JavaFX Main Window for FDD Tools.
 * This class replaces the Swing-based FDDFrame with a pure JavaFX implementation.
 */
package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.Stage;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.persistence.FDDIXMLFileReader;
import net.sourceforge.fddtools.persistence.FDDIXMLFileWriter;
import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nebulon.xml.fddi.Subject;
import com.nebulon.xml.fddi.Activity;

import net.sourceforge.fddtools.util.ObjectCloner;
import java.awt.Font;
import java.io.File;
import java.util.List;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import net.sourceforge.fddtools.util.RecentFilesService;
import net.sourceforge.fddtools.util.LayoutPreferencesService;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.command.*; // Added command pattern imports
import net.sourceforge.fddtools.commands.EditNodeCommand;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.service.DialogService;
import net.sourceforge.fddtools.service.BusyService;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;

public class FDDMainWindowFX extends BorderPane implements FDDTreeContextMenuHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDMainWindowFX.class);
    
    // Core components
    private final Stage primaryStage;
    // Former options model removed; keep default font settings locally
    private static final Font DEFAULT_AWT_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private FDDINode clipboard;
    private boolean uniqueNodeVersion = false; // Track if clipboard node has unique version numbers
    
    // UI Components
    private MenuBar menuBar;
    private ToolBar toolBar;
    private SplitPane mainSplitPane;
    private SplitPane rightSplitPane; // For canvas and panels
    private TabPane infoPanelContainer; // Container for aspect and work package panels
    private FDDTreeViewFX projectTreeFX;
    private FDDCanvasFX canvasFX;
    private VBox statusBar;
    private Label statusLabel;
    private Label undoStatusLabel;
    private Label redoStatusLabel;
    
    // Menu items that need to be enabled/disabled
    private MenuItem fileSave;
    private MenuItem fileSaveAs;
    private MenuItem editCut;
    private MenuItem editCopy;
    private MenuItem editPaste;
    private MenuItem editDelete;
    private MenuItem editEdit;
    private Menu recentFilesMenu; // dynamic recent files submenu
    private MenuItem editUndo; // Undo menu item
    private MenuItem editRedo; // Redo menu item

    // Command infrastructure
    private final CommandExecutionService commandExec; // centralized command execution

    public FDDMainWindowFX(Stage primaryStage) {
        this.primaryStage = primaryStage;
    this.commandExec = CommandExecutionService.getInstance();
        
    // Options system removed; using default font configuration
        
        // Setup macOS integration FIRST
        setupMacOSIntegration();
        
        // Build the UI
        initializeComponents();
        layoutComponents();
        
        // Create new project
        newProject();
        // Bind menu enablement to model state
    // Menu items now use direct property bindings; no additional listener wiring needed.
        
        LOGGER.info("FDDMainWindowFX initialized successfully");
    }
    
    private void setupMacOSIntegration() {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            try {
                // Set up JavaFX-compatible macOS integration
                boolean success = MacOSHandlerFX.setupMacOSHandlers(this, primaryStage);
                if (success) {
                    LOGGER.info("macOS Desktop API handlers set up successfully");
                } else {
                    LOGGER.warn("Some macOS handlers could not be set");
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to setup macOS integration: {}", e.getMessage());
            }
        }
    }
    
    private void initializeComponents() {
        // Create menu bar
        createMenuBar();
        // Create toolbar
        createToolBar();
        // Create main split pane
        mainSplitPane = new SplitPane();
        // Restore saved divider position or default to 0.25
        double mainDivider = LayoutPreferencesService.getInstance()
            .getMainDividerPosition().orElse(0.25);
    mainSplitPane.setDividerPositions(mainDivider);
    // Listener will be attached after items added (dividers created)
        // Create right split pane for canvas and info panels
        rightSplitPane = new SplitPane();
        rightSplitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        double rightDivider = LayoutPreferencesService.getInstance()
            .getRightDividerPosition().orElse(0.7);
    rightSplitPane.setDividerPositions(rightDivider);
    // Attach listener later once divider exists
        // Create info panel container with tabs
        createInfoPanelContainer();
        // Create status bar
        createStatusBar();
    }
    
    private void layoutComponents() {
        // Set up the main layout
        setTop(new VBox(menuBar, toolBar));
        // Wrap mainSplitPane in a StackPane so BusyService can overlay
        StackPane centerWrapper = new StackPane(mainSplitPane);
        setCenter(centerWrapper);
        BusyService.getInstance().attach(centerWrapper);
        setBottom(statusBar);
        // Ensure split pane has two items so divider exists
        if (mainSplitPane.getItems().size() == 0) {
            mainSplitPane.getItems().addAll(new Label(), rightSplitPane); // placeholder left
        }
        // Attach listeners safely
        if (!mainSplitPane.getDividers().isEmpty()) {
            mainSplitPane.getDividers().get(0).positionProperty().addListener((obs, o, n) ->
                LayoutPreferencesService.getInstance().setMainDividerPosition(n.doubleValue()));
        }
        if (!rightSplitPane.getDividers().isEmpty()) {
            rightSplitPane.getDividers().get(0).positionProperty().addListener((obs, o, n) ->
                LayoutPreferencesService.getInstance().setRightDividerPosition(n.doubleValue()));
        }
    }
    
    private void createMenuBar() {
        menuBar = new MenuBar();
        
        // macOS specific: Use system menu bar - properties already set in FDDApplicationFX static block
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            menuBar.setUseSystemMenuBar(true);
            LOGGER.info("Configured JavaFX MenuBar to use system menu bar on macOS");
        }
        
        // File Menu
        Menu fileMenu = new Menu("File");
        
        MenuItem fileNew = new MenuItem("New");
        fileNew.setAccelerator(KeyCombination.keyCombination("Shortcut+N"));
        fileNew.setOnAction(e -> newProject());
        
        MenuItem fileOpen = new MenuItem("Open...");
        fileOpen.setAccelerator(KeyCombination.keyCombination("Shortcut+O"));
        fileOpen.setOnAction(e -> openProject());
        
        fileSave = new MenuItem("Save");
        fileSave.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
        fileSave.setOnAction(e -> saveProject());
        fileSave.disableProperty().bind(ProjectService.getInstance().hasPathProperty().not().or(ModelState.getInstance().dirtyProperty().not()));
        
        fileSaveAs = new MenuItem("Save As...");
        fileSaveAs.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+S"));
        fileSaveAs.setOnAction(e -> saveProjectAs());
        fileSaveAs.disableProperty().bind(ProjectService.getInstance().hasProjectProperty().not());
        
        // Recent Files submenu (populated dynamically)
        recentFilesMenu = new Menu("Open Recent");
        refreshRecentFilesMenu();
        // Clear Recent item
        MenuItem clearRecent = new MenuItem("Clear Recent");
        clearRecent.setOnAction(e -> {
            RecentFilesService.getInstance().clear();
            refreshRecentFilesMenu();
        });
        recentFilesMenu.getItems().add(new SeparatorMenuItem());
        recentFilesMenu.getItems().add(clearRecent);
        
        MenuItem fileExit = new MenuItem("Exit");
        fileExit.setOnAction(e -> exitApplication());
        
        fileMenu.getItems().addAll(
            fileNew, fileOpen, recentFilesMenu, new SeparatorMenuItem(),
            fileSave, fileSaveAs, new SeparatorMenuItem(),
            fileExit
        );
        
        // Edit Menu
        Menu editMenu = new Menu("Edit");
        
        editCut = new MenuItem("Cut");
        editCut.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
        editCut.setOnAction(e -> cutSelectedNode());
        editCut.disableProperty().bind(ModelState.getInstance().selectedNodeProperty().isNull());
        
        editCopy = new MenuItem("Copy");
        editCopy.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
        editCopy.setOnAction(e -> copySelectedNode());
        editCopy.disableProperty().bind(ModelState.getInstance().selectedNodeProperty().isNull());
        
        editPaste = new MenuItem("Paste");
        editPaste.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
        editPaste.setOnAction(e -> pasteNode());
        editPaste.disableProperty().bind(ModelState.getInstance().clipboardNotEmptyProperty().not());
        
        editDelete = new MenuItem("Delete");
        editDelete.setAccelerator(KeyCombination.keyCombination("Delete"));
        editDelete.setOnAction(e -> deleteSelectedNode());
        editDelete.disableProperty().bind(ModelState.getInstance().selectedNodeProperty().isNull());
        
        editEdit = new MenuItem("Edit...");
        editEdit.setAccelerator(KeyCombination.keyCombination("Shortcut+E"));
        editEdit.setOnAction(e -> editSelectedNode());
        editEdit.disableProperty().bind(ModelState.getInstance().selectedNodeProperty().isNull());
        
        MenuItem editPreferences = new MenuItem("Preferences...");
        editPreferences.setOnAction(e -> showPreferencesDialog());
        
        // Insert undo/redo before assembling menu
        editUndo = new MenuItem("Undo");
        editUndo.setAccelerator(KeyCombination.keyCombination("Shortcut+Z"));
        editUndo.setOnAction(e -> performUndo());
        editUndo.disableProperty().bind(ModelState.getInstance().undoAvailableProperty().not());
        editRedo = new MenuItem("Redo");
        editRedo.setAccelerator(KeyCombination.keyCombination("Shortcut+Shift+Z"));
        editRedo.setOnAction(e -> performRedo());
        editRedo.disableProperty().bind(ModelState.getInstance().redoAvailableProperty().not());
        
        // Replace original addAll call for editMenu
        editMenu.getItems().clear();
        editMenu.getItems().addAll(
            editUndo, editRedo, new SeparatorMenuItem(),
            editCut, editCopy, editPaste, new SeparatorMenuItem(),
            editDelete, editEdit, new SeparatorMenuItem(),
            editPreferences
        );
        
        // View Menu
        Menu viewMenu = new Menu("View");
        
        MenuItem viewRefresh = new MenuItem("Refresh");
        viewRefresh.setAccelerator(KeyCombination.keyCombination("F5"));
        viewRefresh.setOnAction(e -> refreshView());
        
        viewMenu.getItems().add(viewRefresh);
        
        // Help Menu
        Menu helpMenu = new Menu("Help");
        
        MenuItem helpAbout = new MenuItem("About FDD Tools");
        helpAbout.setOnAction(e -> showAboutDialog());
        
        helpMenu.getItems().add(helpAbout);
        
        // Add menus to menu bar
        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);

    // Menu items already have direct property bindings; removed legacy listeners that attempted to set
    // disable state on bound properties (caused RuntimeException on startup & project load).
    }
    
    private void createToolBar() {
        toolBar = new ToolBar();
        toolBar.getStyleClass().add("fdd-toolbar");
        java.util.function.BiFunction<FontAwesomeIcon,String,Button> makeBtn = (icon, tip) -> {
            FontAwesomeIconView view = new FontAwesomeIconView(icon);
            view.setGlyphSize(18);
            view.getStyleClass().addAll("fdd-toolbar-icon","fdd-icon");
            Button b = new Button();
            b.setGraphic(view);
            b.getStyleClass().addAll("fdd-toolbar-button","fdd-icon-button");
            b.setTooltip(new Tooltip(tip));
            b.setFocusTraversable(false);
            b.setPrefSize(32,32);
            b.setMinSize(32,32);
            b.setMaxSize(32,32);
            return b;
        };
        Button newBtn = makeBtn.apply(FontAwesomeIcon.FILE, "New Program (⌘N)");
        newBtn.setOnAction(e -> newProject());
        Button openBtn = makeBtn.apply(FontAwesomeIcon.FOLDER_OPEN, "Open (⌘O)");
        openBtn.setOnAction(e -> openProject());
        Button saveBtn = makeBtn.apply(FontAwesomeIcon.FLOPPY_ALT, "Save (⌘S)");
        saveBtn.setOnAction(e -> saveProject());
        Button cutBtn = makeBtn.apply(FontAwesomeIcon.SCISSORS, "Cut (⌘X)");
        cutBtn.setOnAction(e -> cutSelectedNode());
        Button copyBtn = makeBtn.apply(FontAwesomeIcon.COPY, "Copy (⌘C)");
        copyBtn.setOnAction(e -> copySelectedNode());
        Button pasteBtn = makeBtn.apply(FontAwesomeIcon.CLIPBOARD, "Paste (⌘V)");
        pasteBtn.setOnAction(e -> pasteNode());
        toolBar.getItems().addAll(newBtn, openBtn, saveBtn, new Separator(), cutBtn, copyBtn, pasteBtn);
    }
    
    private void createStatusBar() {
        statusBar = new VBox();
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 2px 5px;");
        
        // Use the existing FDDActionPanelFX instead of simple buttons
        FDDActionPanelFX actionPanel = new FDDActionPanelFX();
        actionPanel.setActionHandler(new FDDActionPanelFX.FDDActionHandler() {
            @Override
            public void onAdd() {
                FDDINode selectedNode = getSelectedNode();
                if (selectedNode != null) {
                    // Add appropriate child based on selected node type
                    if (selectedNode instanceof Program) {
                        // For Program nodes, should show menu (handled by button click logic)
                        addProject(selectedNode); // Default to project
                    } else if (selectedNode instanceof Project) {
                        addAspect(selectedNode);
                    } else if (selectedNode instanceof Aspect) {
                        addSubject(selectedNode);
                    } else if (selectedNode instanceof Subject) {
                        addActivity(selectedNode);
                    } else if (selectedNode instanceof Activity) {
                        addFeature(selectedNode);
                    }
                }
            }
            
            @Override
            public void onDelete() {
                FDDINode selectedNode = getSelectedNode();
                if (selectedNode != null) {
                    deleteNode(selectedNode);
                }
            }
            
            @Override
            public void onEdit() {
                FDDINode selectedNode = getSelectedNode();
                if (selectedNode != null) {
                    editNode(selectedNode);
                }
            }
            
            @Override
            public void onAddProgram() {
                FDDINode selectedNode = getSelectedNode();
                if (selectedNode != null) {
                    addProgram(selectedNode);
                }
            }
            
            @Override
            public void onAddProject() {
                FDDINode selectedNode = getSelectedNode();
                if (selectedNode != null) {
                    addProject(selectedNode);
                }
            }
            
            @Override
            public FDDINode getSelectedNode() {
                return FDDMainWindowFX.this.getSelectedNode();
            }
        });
        
    statusLabel = new Label("Ready");
    statusLabel.setStyle("-fx-font-size: 12px;");
    undoStatusLabel = new Label("");
    undoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
    redoStatusLabel = new Label("");
    redoStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");
    VBox undoRedoBox = new VBox(2, undoStatusLabel, redoStatusLabel);
    undoRedoBox.setPadding(new Insets(2,0,0,4));
    statusBar.getChildren().addAll(actionPanel, statusLabel, undoRedoBox);
    // removed explicit updateUndoRedoStatusBar call
    }
    
    private void createInfoPanelContainer() {
        infoPanelContainer = new TabPane();
        infoPanelContainer.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        infoPanelContainer.setPrefHeight(250);
        infoPanelContainer.setVisible(false); // Hidden by default until a relevant node is selected
        
        // Container starts empty - tabs will be added dynamically based on node selection
    }
    
    private void newProject() {
        // Create new project logic
        Platform.runLater(() -> {
            try {
                // Create root node
                FDDINode rootNode = createNewRootNode();
                rebuildProjectUI(rootNode, true);
                LOGGER.info("New project created");
                
            } catch (Exception e) {
                LOGGER.error("Failed to create new project: {}", e.getMessage(), e);
                showErrorDialog("Failed to create new project", e.getMessage());
            }
        });
    }
    
    private void refreshRecentFilesMenu() {
        if (recentFilesMenu == null) return;
        recentFilesMenu.getItems().clear();
        List<String> recents = RecentFilesService.getInstance().getRecentFiles();
        if (recents.isEmpty()) {
            MenuItem none = new MenuItem("(None)");
            none.setDisable(true);
            recentFilesMenu.getItems().add(none);
        } else {
            for (String path : recents) {
                File f = new File(path);
                String display = f.getName();
                MenuItem item = new MenuItem(display);
                item.setOnAction(e -> openSpecificProject(path));
                recentFilesMenu.getItems().add(item);
            }
        }
        // Append clear option at end
        recentFilesMenu.getItems().add(new SeparatorMenuItem());
        MenuItem clearRecent = new MenuItem("Clear Recent");
        clearRecent.setOnAction(e -> {
            RecentFilesService.getInstance().clear();
            refreshRecentFilesMenu();
        });
        recentFilesMenu.getItems().add(clearRecent);
    }
    
    private void openSpecificProject(String path) {
        File selectedFile = new File(path);
        if (!selectedFile.exists()) {
            showErrorDialog("Open Recent", "File no longer exists: " + path);
            RecentFilesService.getInstance().clear(); // prune all to simplify; could prune single
            refreshRecentFilesMenu();
            return;
        }
        try {
            LOGGER.debug("Attempting to open recent project: {}", path);
            loadProjectFromPath(selectedFile.getAbsolutePath(), true);
            RecentFilesService.getInstance().addRecentFile(path);
            refreshRecentFilesMenu();
        } catch (Exception ex) {
            LOGGER.error("Failed to load project file: {}", ex.getMessage(), ex);
            showErrorDialog("Open Project Failed", "Error loading file: " + ex.getMessage());
        }
    }
    
    private FDDINode createNewRootNode() {
        try {
            ObjectFactory factory = new ObjectFactory();
            Program program = factory.createProgram();
            program.setName("New Program");
            return (FDDINode) program;
        } catch (Exception e) {
            LOGGER.error("Failed to create new root node: {}", e.getMessage(), e);
            return null;
        }
    }
    
    // Legacy manual menu state code removed; bindings handle enablement automatically.
    
    private void updateTitle() {
        Platform.runLater(() -> {
            String title = "FDD Tools";
            ProjectService ps = ProjectService.getInstance();
            String name = ps.getDisplayName();
            if (name != null) {
                title += " - " + name;
                if (ModelState.getInstance().isDirty()) title += " *";
            }
            primaryStage.setTitle(title);
        });
    }
    
    // Action methods
    private void openProject() {
        Platform.runLater(() -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open FDD Project");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("FDD Files", "*.fddi", "*.xml"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
                );
                File selectedFile = fileChooser.showOpenDialog(primaryStage);
                if (selectedFile != null) {
                    Task<FDDINode> loadTask = new Task<>() { @Override protected FDDINode call() throws Exception { return (FDDINode) FDDIXMLFileReader.read(selectedFile.getAbsolutePath()); } };
                    BusyService.getInstance().runAsync("Opening", loadTask, () -> {
                        FDDINode rootNode = loadTask.getValue();
                        if (rootNode != null) {
                            rebuildProjectUI(rootNode, false);
                        } else {
                            showErrorDialog("Open Project Failed", "Failed to parse the selected file.");
                        }
                    }, () -> showErrorDialog("Open Project Failed", "Error loading file."));
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load project file: {}", e.getMessage(), e);
                showErrorDialog("Open Project Failed", "Error loading file: " + e.getMessage());
            }
        });
    }
    
    private void closeCurrentProject() {
        // Clear the split pane contents
        if (mainSplitPane != null) {
            mainSplitPane.getItems().clear();
        }
        if (rightSplitPane != null) {
            rightSplitPane.getItems().clear();
        }
        
        projectTreeFX = null;
        canvasFX = null;
    }
    
    private void saveProject() {
        if (ProjectService.getInstance().getAbsolutePath() != null && !ProjectService.getInstance().getAbsolutePath().equals("New Program")) {
            // Save to existing file using full path
            saveToFile(ProjectService.getInstance().getAbsolutePath());
        } else {
            // No file selected, do Save As
            saveProjectAs();
        }
    }
    
    private void saveProjectAs() {
        Platform.runLater(() -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save FDD Project");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("FDD Files", "*.fddi"),
                    new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
                );
                
                // Set default filename
                if (ProjectService.getInstance().getDisplayName() != null && !ProjectService.getInstance().getDisplayName().equals("New Program")) {
                    fileChooser.setInitialFileName(ProjectService.getInstance().getDisplayName());
                } else {
                    fileChooser.setInitialFileName("New Program.fddi");
                }
                
                File selectedFile = fileChooser.showSaveDialog(primaryStage);
                if (selectedFile != null) {
                    String filePath = selectedFile.getAbsolutePath();
                    if (saveToFile(filePath)) {
                        updateTitle();
                        updateMenuStates();
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to save project: {}", e.getMessage());
                showErrorDialog("Save Project Failed", e.getMessage());
            }
        });
    }
    
    private boolean saveToFile(String fileName) {
        Task<Boolean> saveTask = new Task<>() { @Override protected Boolean call() throws Exception {
            final Object rootNode;
            if (projectTreeFX != null && projectTreeFX.getRoot() != null) rootNode = projectTreeFX.getRoot().getValue(); else return false;
            if (rootNode == null) return false;
            return FDDIXMLFileWriter.write(rootNode, fileName);
        }};
        BusyService.getInstance().runAsync("Saving", saveTask, () -> {
            if (Boolean.TRUE.equals(saveTask.getValue())) {
                try { ProjectService.getInstance().saveAs(fileName); } catch (Exception ex) { LOGGER.warn("saveAs failed: {}", ex.getMessage()); }
                updateTitle();
            } else {
                showErrorDialog("Save Error", "Failed to save the project file.");
            }
        }, () -> showErrorDialog("Save Error", "An error occurred while saving."));
        return true; // operation started
    }
    
    private void cutSelectedNode() {
        FDDINode selected = getSelectedNode();
        if (selected == null) return;

        // Disallow cutting root
        if (projectTreeFX != null && projectTreeFX.getRoot() != null && projectTreeFX.getRoot().getValue() == selected) {
            showErrorDialog("Cut Not Allowed", "Cannot cut the root element.");
            return;
        }

        clipboard = (FDDINode) ObjectCloner.deepClone(selected);
        uniqueNodeVersion = false;
        if (clipboard == null) {
            LOGGER.error("Failed to deep clone for cut: {}", selected.getClass().getSimpleName());
            showErrorDialog("Cut Error", "Unable to copy node to clipboard.");
            return;
        }
        ModelState.getInstance().setClipboardNotEmpty(true);
        FDDINode parent = (FDDINode) selected.getParentNode();
        if (parent != null) {
            commandExec.execute(new DeleteNodeCommand(selected));
            afterModelMutation(parent);
        }
        LOGGER.info("Cut (removed) node via command: " + selected.getClass().getSimpleName());
    }
    
    private void copySelectedNode() {
        FDDINode selected = getSelectedNode();
        if (selected != null) {
            clipboard = (FDDINode) ObjectCloner.deepClone(selected);
            uniqueNodeVersion = false;
            if (clipboard != null) {
                ModelState.getInstance().setClipboardNotEmpty(true);
                editPaste.setDisable(false);
                LOGGER.info("Copied node: " + selected.getClass().getSimpleName());
            } else {
                LOGGER.error("Failed to create deep copy of node: {}", selected.getClass().getSimpleName());
                showErrorDialog("Copy Error", "Failed to copy the selected node.");
            }
        }
    }
    
    private void pasteNode() {
        if (clipboard != null) {
            FDDINode selected = getSelectedNode();
            if (selected != null) {
                try {
                    PasteNodeCommand cmd = new PasteNodeCommand(selected, clipboard, !uniqueNodeVersion);
                    commandExec.execute(cmd);
                    uniqueNodeVersion = false;
                    if (projectTreeFX != null) {
                        projectTreeFX.refresh();
                        if (cmd.getPasted() != null) projectTreeFX.selectNode(cmd.getPasted());
                    }
                    if (canvasFX != null) canvasFX.redraw();
                    markDirty();
                    LOGGER.info("Pasted node via command: " + clipboard.getClass().getSimpleName());
                } catch (Exception e) {
                    LOGGER.error("Failed to paste node: {}", e.getMessage(), e);
                    showErrorDialog("Paste Error", "An error occurred while pasting: " + e.getMessage());
                }
            }
        }
    }
    
    private void deleteSelectedNode() {
        FDDINode selected = getSelectedNode();
        if (selected == null) return;
        if (projectTreeFX != null && projectTreeFX.getRoot() != null && projectTreeFX.getRoot().getValue() == selected) {
            showErrorDialog("Delete Not Allowed", "Cannot delete the root element.");
            return;
        }
        boolean confirmed = DialogService.getInstance().confirm(primaryStage,
                "Delete Node",
                "Delete " + selected.getClass().getSimpleName(),
                "Are you sure you want to delete this node?");
        if (confirmed) {
            FDDINode parent = (FDDINode) selected.getParentNode();
            if (parent != null) {
                commandExec.execute(new DeleteNodeCommand(selected));
                afterModelMutation(parent);
                LOGGER.info("Deleted node via command: " + selected.getClass().getSimpleName());
            }
        }
    }
    
    private void addFDDElementNode(FDDINode parentNode, String requestedType) {
        FDDINode currentNode = parentNode;
        if (currentNode == null) {
            currentNode = getSelectedNode();
        }
        
        final FDDINode selectedNode = currentNode; // Make effectively final for lambda
        
        if (selectedNode == null) {
            LOGGER.error("ERROR: No node selected");
            return;
        }
        
        ObjectFactory of = new ObjectFactory();
        final FDDINode newNode;
        
        // Create appropriate child node based on parent type
        if (selectedNode instanceof Program) {
            if ("Program".equals(requestedType)) {
                newNode = (FDDINode) of.createProgram();
                newNode.setName("New Program");
            } else {
                newNode = (FDDINode) of.createProject();
                newNode.setName("New Project");
            }
        } else if (selectedNode instanceof Project) {
            newNode = (FDDINode) of.createAspect();
            newNode.setName("New Aspect");
        } else if (selectedNode instanceof Aspect) {
            newNode = (FDDINode) of.createSubject();
            newNode.setName("New Subject");
        } else if (selectedNode instanceof Subject) {
            newNode = (FDDINode) of.createActivity();
            newNode.setName("New Activity");
        } else if (selectedNode instanceof Activity) {
            newNode = (FDDINode) of.createFeature();
            newNode.setName("New Feature");
        } else {
            LOGGER.warn("Cannot add child to node type: {}", selectedNode.getClass().getSimpleName());
            return;
        }
        
        // Set parent relationship
    newNode.setParentNode(selectedNode);
        
        LOGGER.info("Creating new " + newNode.getClass().getSimpleName() + " for parent: " + selectedNode.getName());
        
        // Show element dialog for the new node
        Platform.runLater(() -> {
            FDDElementDialogFX dlg = new FDDElementDialogFX(primaryStage, newNode);
            configureDialogCentering(dlg);
            dlg.showAndWait();
            if (dlg.getAccept()) {
                // Add the new node to the parent
                commandExec.execute(new AddChildCommand(selectedNode, newNode));
                afterModelMutation(newNode);
                LOGGER.info("Added new node via command: " + newNode.getClass().getSimpleName());
            } else {
                LOGGER.info("User cancelled adding new " + newNode.getClass().getSimpleName());
            }
        });
    }
    
    private void performUndo() { commandExec.undo(); refreshView(); ProjectService.getInstance().markDirty(); updateUndoRedoState(); }
    private void performRedo() { commandExec.redo(); refreshView(); ProjectService.getInstance().markDirty(); updateUndoRedoState(); }
    private void updateUndoRedoState() {
        ModelState ms = ModelState.getInstance();
    ms.setUndoAvailable(commandExec.getStack().canUndo());
    ms.setRedoAvailable(commandExec.getStack().canRedo());
        if (editUndo != null) {
            editUndo.setText("Undo");
        }
        if (editRedo != null) {
            editRedo.setText("Redo");
        }
    }

    private void markDirty() { ProjectService.getInstance().markDirty(); updateTitle(); updateUndoRedoState(); }
    private void afterModelMutation(FDDINode nodeToSelect) {
        if (projectTreeFX != null) {
            projectTreeFX.refresh();
            projectTreeFX.selectNode(nodeToSelect);
        }
        if (canvasFX != null) {
            canvasFX.setCurrentNode(nodeToSelect);
            canvasFX.redraw();
        }
        markDirty();
    }
    // ===== Restored methods & interface implementations (previously truncated) =====

    // FDDTreeContextMenuHandler implementation
    @Override
    public void onSelectionChanged(FDDINode selectedNode) { onTreeSelectionChanged(selectedNode); }
    @Override
    public void addProgram(FDDINode parentNode) { addFDDElementNode(parentNode, "Program"); }
    @Override
    public void addProject(FDDINode parentNode) { addFDDElementNode(parentNode, "Project"); }
    @Override
    public void addAspect(FDDINode parentNode) { addFDDElementNode(parentNode, "Aspect"); }
    @Override
    public void addSubject(FDDINode parentNode) { addFDDElementNode(parentNode, "Subject"); }
    @Override
    public void addActivity(FDDINode parentNode) { addFDDElementNode(parentNode, "Activity"); }
    @Override
    public void addFeature(FDDINode parentNode) { addFDDElementNode(parentNode, "Feature"); }
    @Override
    public void editNode(FDDINode node) { editSelectedNode(node); }
    @Override
    public void deleteNode(FDDINode node) { deleteSelectedNode(); }

    private void editSelectedNode() { editSelectedNode(getSelectedNode()); }
    private void editSelectedNode(FDDINode node) {
        if (node != null) {
            Platform.runLater(() -> {
                var beforeSnapshot = EditNodeCommand.capture(node);
                FDDElementDialogFX dlg = new FDDElementDialogFX(primaryStage, node);
                configureDialogCentering(dlg);
                dlg.showAndWait();
                if (dlg.getAccept()) {
                    var afterSnapshot = EditNodeCommand.capture(node);
                    boolean changed = !beforeSnapshot.getName().equals(afterSnapshot.getName()) ||
                        (beforeSnapshot.getPrefix() == null ? afterSnapshot.getPrefix() != null : !beforeSnapshot.getPrefix().equals(afterSnapshot.getPrefix())) ||
                        (beforeSnapshot.getOwnerInitials() == null ? afterSnapshot.getOwnerInitials() != null : !beforeSnapshot.getOwnerInitials().equals(afterSnapshot.getOwnerInitials())) ||
                        // Milestone changes
                        (beforeSnapshot.getMilestoneStatuses() != null && afterSnapshot.getMilestoneStatuses() != null &&
                            java.util.Arrays.equals(beforeSnapshot.getMilestoneStatuses(), afterSnapshot.getMilestoneStatuses()) == false) ||
                        // Work package assignment change
                        ((beforeSnapshot.getWorkPackageName() == null ? "" : beforeSnapshot.getWorkPackageName())
                            .equals(afterSnapshot.getWorkPackageName() == null ? "" : afterSnapshot.getWorkPackageName()) == false);
                    if (changed) {
                        // Revert to before state
                        node.setName(beforeSnapshot.getName());
                        if (node instanceof com.nebulon.xml.fddi.Subject subj) subj.setPrefix(beforeSnapshot.getPrefix());
                        if (node instanceof com.nebulon.xml.fddi.Activity act) act.setInitials(beforeSnapshot.getOwnerInitials());
                        else if (node instanceof com.nebulon.xml.fddi.Feature feat) {
                            feat.setInitials(beforeSnapshot.getOwnerInitials());
                            // Revert milestones
                            if (beforeSnapshot.getMilestoneStatuses() != null) {
                                var milestones = feat.getMilestone();
                                for (int i = 0; i < Math.min(milestones.size(), beforeSnapshot.getMilestoneStatuses().length); i++) {
                                    milestones.get(i).setStatus(beforeSnapshot.getMilestoneStatuses()[i]);
                                }
                            }
                            // Revert work package membership if changed
                            if (afterSnapshot.getWorkPackageName() != null || beforeSnapshot.getWorkPackageName() != null) {
                                var proj = getOwningProject(feat);
                                if (proj != null) {
                                    // Remove from all
                                    for (var wp : proj.getWorkPackages()) {
                                        wp.getFeatureList().remove(Integer.valueOf(feat.getSeq()));
                                    }
                                    // Re-add original if not empty
                                    if (beforeSnapshot.getWorkPackageName() != null && !beforeSnapshot.getWorkPackageName().isEmpty()) {
                                        for (var wp : proj.getWorkPackages()) {
                                            if (beforeSnapshot.getWorkPackageName().equals(wp.getName())) {
                                                if (!wp.getFeatureList().contains(feat.getSeq())) wp.addFeature(feat.getSeq());
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        commandExec.execute(new EditNodeCommand(node, beforeSnapshot, afterSnapshot));
                        updateUndoRedoState();
                    }
                    markDirty();
                    if (canvasFX != null) canvasFX.redraw();
                }
            });
        }
    }

    private FDDINode getSelectedNode() { return projectTreeFX != null ? projectTreeFX.getSelectedNode() : null; }

    private com.nebulon.xml.fddi.Project getOwningProject(com.nebulon.xml.fddi.Feature feat) {
        net.sourceforge.fddtools.model.FDDTreeNode current = feat.getParentNode();
        while (current != null) {
            if (current instanceof com.nebulon.xml.fddi.Project p) return p;
            current = current.getParentNode();
        }
        return null;
    }

    private void onTreeSelectionChanged(FDDINode selectedNode) {
        if (canvasFX != null && selectedNode != null) {
            canvasFX.setCurrentNode(selectedNode);
        }
        updateInfoPanels(selectedNode);
    }

    private void updateInfoPanels(FDDINode selectedNode) {
        if (infoPanelContainer.isVisible()) {
            infoPanelContainer.setVisible(false);
            rightSplitPane.getItems().remove(infoPanelContainer);
            rightSplitPane.setDividerPositions(1.0);
        }
    }

    private void refreshView() {
        Platform.runLater(() -> {
            if (projectTreeFX != null) projectTreeFX.refresh();
            if (canvasFX != null) canvasFX.redraw();
        });
    }

    public void showPreferencesDialog() { DialogService.getInstance().showPreferences(primaryStage); }
    private void showAboutDialog() { DialogService.getInstance().showAbout(primaryStage, null); }

    private void exitApplication() { if (canClose()) Platform.exit(); }

    private void showErrorDialog(String title, String message) {
        DialogService.getInstance().showError(primaryStage, title, message);
    }

    public boolean canClose() {
        if (ModelState.getInstance().isDirty()) {
            ButtonType saveButton = new ButtonType("Save");
            ButtonType dontSaveButton = new ButtonType("Don't Save");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType choice = DialogService.getInstance().confirmWithChoices(primaryStage,
                    "Unsaved Changes",
                    "Save changes before closing?",
                    "You have unsaved changes. Do you want to save them?",
                    saveButton, dontSaveButton, cancelButton);
            if (choice == saveButton) { saveProject(); return true; }
            else if (choice == dontSaveButton) { return true; }
            else { return false; }
        }
        return true;
    }

    public void cleanup() { LOGGER.info("FDDMainWindowFX cleanup completed"); }

    private void configureDialogCentering(Stage stage) {
        if (primaryStage != null && stage.getOwner() == null) stage.initOwner(primaryStage);
        stage.setOnShown(e -> centerWindowOverCanvas(stage));
    }

    private void centerWindowOverCanvas(Window win) {
        Node anchor = (canvasFX != null) ? canvasFX : this;
        Bounds b = anchor.localToScreen(anchor.getBoundsInLocal());
        if (b != null) {
            double x = b.getMinX() + (b.getWidth() - win.getWidth()) / 2.0;
            double y = b.getMinY() + (b.getHeight() - win.getHeight()) / 3.0;
            win.setX(x);
            win.setY(y);
            Platform.runLater(() -> {
                Bounds b2 = anchor.localToScreen(anchor.getBoundsInLocal());
                if (b2 != null) {
                    double x2 = b2.getMinX() + (b2.getWidth() - win.getWidth()) / 2.0;
                    double y2 = b2.getMinY() + (b2.getHeight() - win.getHeight()) / 3.0;
                    win.setX(x2);
                    win.setY(y2);
                }
            });
        } else if (primaryStage != null) {
            win.setX(primaryStage.getX() + (primaryStage.getWidth() - win.getWidth()) / 2.0);
            win.setY(primaryStage.getY() + (primaryStage.getHeight() - win.getHeight()) / 2.0);
        }
    }
    // ===== Helper methods extracted to unify project load/new logic =====
    private void rebuildProjectUI(FDDINode rootNode, boolean isNew) {
        if (rootNode == null) return;
        closeCurrentProject();
        projectTreeFX = new FDDTreeViewFX(false, true);
        projectTreeFX.setContextMenuHandler(this);
        projectTreeFX.populateTree(rootNode);
        if (projectTreeFX.getRoot() != null) {
            projectTreeFX.getSelectionModel().select(projectTreeFX.getRoot());
        }
        projectTreeFX.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                ModelState.getInstance().setSelectedNode(newSel.getValue());
                onTreeSelectionChanged(newSel.getValue());
            }
        });
        Font awtFont = DEFAULT_AWT_FONT;
        javafx.scene.text.Font fxFont = javafx.scene.text.Font.font(awtFont.getName(), awtFont.getSize());
        canvasFX = new FDDCanvasFX(rootNode, fxFont);
        rightSplitPane.getItems().clear();
        rightSplitPane.getItems().add(canvasFX);
        if (infoPanelContainer.isVisible()) {
            rightSplitPane.getItems().add(infoPanelContainer);
            rightSplitPane.setDividerPositions(0.7);
        } else {
            rightSplitPane.setDividerPositions(1.0);
        }
        mainSplitPane.getItems().clear();
        mainSplitPane.getItems().addAll(projectTreeFX, rightSplitPane);
        double pos = LayoutPreferencesService.getInstance().getMainDividerPosition().orElse(0.25);
        mainSplitPane.setDividerPositions(pos);
        updateTitle();
        updateUndoRedoState();
        if (isNew) {
            ProjectService.getInstance().newProject(rootNode.getName());
        }
    }

    private void loadProjectFromPath(String absolutePath, boolean fromRecent) {
        if (absolutePath == null) return;
        LOGGER.debug("Loading project from path: {} (recent={})", absolutePath, fromRecent);
        try {
            FDDINode rootNode = (FDDINode) FDDIXMLFileReader.read(absolutePath);
            if (rootNode == null) {
                showErrorDialog("Open Project Failed", "Failed to parse the selected file.");
                return;
            }
            ProjectService.getInstance().open(absolutePath);
            rebuildProjectUI(rootNode, false);
            LOGGER.info("Project loaded: {}", absolutePath);
        } catch (Exception e) {
            LOGGER.error("Load project failed: {}", e.getMessage(), e);
            showErrorDialog("Open Project Failed", e.getMessage());
        }
    }

    // Package-private getters for tests
    FDDTreeViewFX getProjectTree() { return projectTreeFX; }
    FDDCanvasFX getCanvas() { return canvasFX; }
}
