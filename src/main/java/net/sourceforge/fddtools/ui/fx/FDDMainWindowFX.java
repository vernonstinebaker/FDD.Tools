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
import javafx.scene.layout.StackPane;
import net.sourceforge.fddtools.util.UnsavedChangesHandler;

public class FDDMainWindowFX extends BorderPane implements FDDTreeContextMenuHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDMainWindowFX.class);
    
    // Core components
    private final Stage primaryStage;
    // Former options model removed; keep default font settings locally
    private static final javafx.scene.text.Font DEFAULT_FONT = javafx.scene.text.Font.font("System", 12);
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
            mainSplitPane.getDividers().get(0).positionProperty().addListener((obs,o,n)->
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
        fileNew.setOnAction(e -> requestNewProject());
        
        MenuItem fileOpen = new MenuItem("Open...");
        fileOpen.setAccelerator(KeyCombination.keyCombination("Shortcut+O"));
        fileOpen.setOnAction(e -> requestOpenProject());
        
    fileSave = new MenuItem("Save");
    fileSave.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
    fileSave.setOnAction(e -> saveProject());
    // Enable Save whenever a project exists AND it is dirty (even if not yet saved / no path)
    // Allow Save when a project exists (even if not dirty yet) so user can immediately choose a path.
    fileSave.disableProperty().bind(ProjectService.getInstance().hasProjectProperty().not());
        
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
    // Use unified requestOpenProject() path so ProjectService absolutePath is set and unsaved-change prompts occur.
    // Legacy openProject() bypassed ProjectService.openWithRoot causing Save (⌘S) to wrongly trigger Save As.
    openBtn.setOnAction(e -> requestOpenProject());
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
    
    // Wrapper that handles unsaved state before creating new project
    private void requestNewProject(){
        // Mirror quit semantics but without exiting
        if (!canClose()) return; // user cancelled or save failed
        createFreshProject();
    }

    private void requestOpenProject(){
        if (!canClose()) return; // preserve unsaved state if cancelled
        openProjectInternal();
    }

    private void handleUnsavedChanges(Runnable proceed){
        UnsavedChangesHandler.handle(
                ModelState.getInstance().isDirty(),
                () -> {
                    ButtonType saveButton = new ButtonType("Save");
                    ButtonType dontSaveButton = new ButtonType("Don't Save");
                    ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    ButtonType choice = DialogService.getInstance().confirmWithChoices(primaryStage,
                            "Unsaved Changes",
                            "Save changes before continuing?",
                            "You have unsaved changes.",
                            saveButton, dontSaveButton, cancelButton);
                    if (choice == saveButton) return UnsavedChangesHandler.Decision.SAVE;
                    if (choice == dontSaveButton) return UnsavedChangesHandler.Decision.DONT_SAVE;
                    return UnsavedChangesHandler.Decision.CANCEL;
                },
                this::saveCurrentProjectBlocking,
                proceed);
    }

    private boolean saveCurrentProjectBlocking(){
        try {
            ProjectService ps = ProjectService.getInstance();
            if (ps.getRoot()==null) return true;
            String pathBefore = ps.getAbsolutePath();
            LOGGER.debug("Blocking save invoked (pathBefore={}, displayName={}, dirty={})", pathBefore, ps.getDisplayName(), ModelState.getInstance().isDirty());
            if (pathBefore!=null){
                boolean ok = ps.save();
                LOGGER.debug("Blocking save existing path result={} pathAfter={} dirtyNow={}", ok, ps.getAbsolutePath(), ModelState.getInstance().isDirty());
                if (ok){
                    net.sourceforge.fddtools.util.PreferencesService.getInstance().setLastProjectPath(ps.getAbsolutePath());
                    net.sourceforge.fddtools.util.PreferencesService.getInstance().flushNow();
                    updateTitle();
                }
                return ok;
            } else {
                FileChooser fc = new FileChooser();
                fc.setTitle("Save FDD Project");
                fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("FDD Files","*.fddi"), new FileChooser.ExtensionFilter("XML Files","*.xml"));
                String dn = buildDefaultSaveFileName(ps.getDisplayName());
                fc.setInitialFileName(dn);
                File f = fc.showSaveDialog(primaryStage);
                if (f==null) { LOGGER.debug("Blocking save cancelled by user"); return false; }
                String target = ensureFddiOrXmlExtension(f.getAbsolutePath());
                boolean ok = ps.saveAs(target);
                LOGGER.debug("Blocking saveAs result={} newPath={} dirtyNow={}", ok, ps.getAbsolutePath(), ModelState.getInstance().isDirty());
                if (ok){
                    net.sourceforge.fddtools.util.PreferencesService.getInstance().setLastProjectPath(ps.getAbsolutePath());
                    net.sourceforge.fddtools.util.PreferencesService.getInstance().flushNow();
                    updateTitle();
                }
                return ok;
            }
        } catch (Exception ex){
            LOGGER.error("Blocking save failed: {}", ex.getMessage(), ex);
            showErrorDialog("Save Failed", ex.getMessage());
            return false;
        }
    }

    private void createFreshProject(){
        FDDINode rootNode = createNewRootNode();
        rebuildProjectUI(rootNode, true);
        LOGGER.info("New project created");
    }

    private void newProject() { // legacy callers (toolbar) delegate
        requestNewProject();
    }

    private void openProjectInternal(){
        // Actual open dialog (was openProject())
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open FDD Project");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("FDD Files", "*.fddi", "*.xml"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                loadProjectFromPath(selectedFile.getAbsolutePath(), true);
                RecentFilesService.getInstance().addRecentFile(selectedFile.getAbsolutePath());
                refreshRecentFilesMenu();
                net.sourceforge.fddtools.util.PreferencesService.getInstance().setLastProjectPath(selectedFile.getAbsolutePath());
                net.sourceforge.fddtools.util.PreferencesService.getInstance().flushNow();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load project file: {}", e.getMessage(), e);
            showErrorDialog("Open Project Failed", "Error loading file: " + e.getMessage());
        }
    }

    private void openSpecificProject(String path) {
        handleUnsavedChanges(() -> {
            File selectedFile = new File(path);
            if (!selectedFile.exists()) {
                showErrorDialog("Open Recent", "File no longer exists: " + path);
                RecentFilesService.getInstance().clear();
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
    
    // Action method (legacy openProject removed; unified open path via requestOpenProject -> openProjectInternal)
    
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
        // If there is already a path, perform a normal save, otherwise forward to Save As
        String currentPath = ProjectService.getInstance().getAbsolutePath();
        if (currentPath != null) {
            // Perform silent save to existing path (no chooser)
            saveToFile(currentPath);
        } else {
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
                // Set default filename (sanitized to avoid double extensions)
                fileChooser.setInitialFileName(buildDefaultSaveFileName(ProjectService.getInstance().getDisplayName()));
                
                File selectedFile = fileChooser.showSaveDialog(primaryStage);
                if (selectedFile != null) {
                    String filePath = ensureFddiOrXmlExtension(selectedFile.getAbsolutePath());
                    if (saveToFile(filePath)) { updateTitle(); }
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to save project: {}", e.getMessage());
                showErrorDialog("Save Project Failed", e.getMessage());
            }
        });
    }
    
    private boolean saveToFile(String fileName) {
    // Always rely on ProjectService root (authoritative) to ensure saved content matches edits
    Object rootNode = ProjectService.getInstance().getRoot();
    if (rootNode == null) return false;

        // Decide whether this is a Save (existing path) or Save As (new path)
        ProjectService ps = ProjectService.getInstance();
        String currentPath = ps.getAbsolutePath();
        boolean isSaveAs = currentPath == null || !currentPath.equals(fileName);
    String normalized = ensureFddiOrXmlExtension(stripDuplicateFddi(fileName));

        // --- Diagnostic structure logging (helps detect accidental minimal root overwrites) ---
        try {
            int directChildren = computeDirectChildCount(rootNode);
            int totalNodes = computeTotalNodeCount(rootNode);
            LOGGER.info("Pre-save structure: op={} path='{}' identity={} type={} directChildren={} totalNodes={} dirty={} currentRegisteredPath='{}'", (isSaveAs?"SaveAs":"Save"), normalized, System.identityHashCode(rootNode), rootNode.getClass().getSimpleName(), directChildren, totalNodes, net.sourceforge.fddtools.state.ModelState.getInstance().isDirty(), currentPath);
            if (!isSaveAs && directChildren == 0 && fileAppearsPreviouslyPopulated(currentPath)) {
                LOGGER.warn("Guard: Attempting to overwrite existing file with zero-child root. Operation will continue, but this is suspicious.");
            }
        } catch (Exception diagEx) {
            LOGGER.debug("Structure diagnostics failed: {}", diagEx.getMessage());
        }

        javafx.concurrent.Task<Boolean> saveTask = FDDIXMLFileWriter.createWriteTask(rootNode, normalized);
    LOGGER.debug("Initiating {} operation (requested='{}', normalized='{}', isSaveAs={}, currentPath='{}')", (isSaveAs?"SaveAs":"Save"), fileName, normalized, isSaveAs, currentPath);
        BusyService.getInstance().runAsync(isSaveAs ? "Saving As" : "Saving", saveTask, true, true, () -> {
            if (Boolean.TRUE.equals(saveTask.getValue())) {
                try {
                    if (isSaveAs) {
                        ps.saveAs(normalized);
                        RecentFilesService.getInstance().addRecentFile(normalized);
                        refreshRecentFilesMenu();
                    } else {
                        ps.save();
                    }
                    // Post-save structural confirmation (log only)
                    try {
                        Object afterRoot = ProjectService.getInstance().getRoot();
                        int afterDirect = computeDirectChildCount(afterRoot);
                        int afterTotal = computeTotalNodeCount(afterRoot);
                        LOGGER.info("Post-save structure: op={} path='{}' identity={} directChildren={} totalNodes={}", (isSaveAs?"SaveAs":"Save"), ps.getAbsolutePath(), System.identityHashCode(afterRoot), afterDirect, afterTotal);
                    } catch (Exception postEx) {
                        LOGGER.debug("Post-save structure diagnostics failed: {}", postEx.getMessage());
                    }
                } catch (Exception ex) {
                    LOGGER.warn("ProjectService save operation failed after file write: {}", ex.getMessage(), ex);
                }
                net.sourceforge.fddtools.util.PreferencesService.getInstance().setLastProjectPath(ps.getAbsolutePath());
                net.sourceforge.fddtools.util.PreferencesService.getInstance().flushNow();
                updateTitle();
            } else {
                showErrorDialog("Save Error", "Failed to save the project file.");
            }
        }, () -> showErrorDialog("Save Error", "An error occurred while saving."));
        return true; // operation started
    }

    // Compute number of direct children for known root/container node types
    private int computeDirectChildCount(Object node) {
        if (node == null) return 0;
        try {
            if (node instanceof com.nebulon.xml.fddi.Program prog) {
                return prog.getProject() == null ? 0 : prog.getProject().size();
            } else if (node instanceof com.nebulon.xml.fddi.Project proj) {
                return proj.getAspect() == null ? 0 : proj.getAspect().size();
            } else if (node instanceof com.nebulon.xml.fddi.Aspect asp) {
                return asp.getSubject() == null ? 0 : asp.getSubject().size();
            } else if (node instanceof com.nebulon.xml.fddi.Subject subj) {
                return subj.getActivity() == null ? 0 : subj.getActivity().size();
            } else if (node instanceof com.nebulon.xml.fddi.Activity act) {
                return act.getFeature() == null ? 0 : act.getFeature().size();
            }
        } catch (Exception ignored) {}
        return 0;
    }

    // Recursively count total nodes (approximate structure size)
    private int computeTotalNodeCount(Object node) {
        if (node == null) return 0;
        int count = 1; // include self
        try {
            if (node instanceof com.nebulon.xml.fddi.Program prog && prog.getProject() != null) {
                for (var p : prog.getProject()) count += computeTotalNodeCount(p);
            } else if (node instanceof com.nebulon.xml.fddi.Project proj && proj.getAspect() != null) {
                for (var a : proj.getAspect()) count += computeTotalNodeCount(a);
            } else if (node instanceof com.nebulon.xml.fddi.Aspect asp && asp.getSubject() != null) {
                for (var s : asp.getSubject()) count += computeTotalNodeCount(s);
            } else if (node instanceof com.nebulon.xml.fddi.Subject subj && subj.getActivity() != null) {
                for (var act : subj.getActivity()) count += computeTotalNodeCount(act);
            } else if (node instanceof com.nebulon.xml.fddi.Activity act && act.getFeature() != null) {
                for (var f : act.getFeature()) count += computeTotalNodeCount(f);
            }
        } catch (Exception ignored) {}
        return count;
    }

    // Heuristic: if file exists and is larger than a tiny threshold, treat as previously populated.
    private boolean fileAppearsPreviouslyPopulated(String path) {
        if (path == null) return false;
        try {
            java.io.File f = new java.io.File(path);
            return f.exists() && f.length() > 200; // minimal .fddi with only root is typically quite small
        } catch (Exception e) { return false; }
    }

    // Remove duplicate .fddi occurrences in a raw path before ensureFddiOrXmlExtension adds one
    private static String stripDuplicateFddi(String path){
        if (path==null) return null;
        String lower = path.toLowerCase();
        if (!lower.contains(".fddi")) return path; // nothing to strip
        // Collapse any repeated .fddi.fddi... at end to single
        while (lower.endsWith(".fddi.fddi")) {
            path = path.substring(0, path.length()-5); // remove one suffix
            lower = path.toLowerCase();
        }
        return path;
    }

    // --- Filename helpers (package-private for tests via reflection) ---
    private static String buildDefaultSaveFileName(String displayName) {
        // Return a base filename WITHOUT extension; macOS file dialogs were showing double extensions
        String base = displayName;
        if (base == null || base.isBlank() || base.equalsIgnoreCase("New Program") || base.equalsIgnoreCase("New Program.fddi")) {
            base = "New Program";
        }
        while (base.toLowerCase().endsWith(".fddi")) {
            base = base.substring(0, base.length() - 5);
        }
        return base; // no extension
    }

    private static String ensureFddiOrXmlExtension(String absolutePath) {
        if (absolutePath == null) return null;
        String lower = absolutePath.toLowerCase();
        if (lower.endsWith(".fddi") || lower.endsWith(".xml")) return absolutePath;
        return absolutePath + ".fddi";
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
    net.sourceforge.fddtools.service.LoggingService.getInstance().audit("nodeCut", java.util.Map.of("selectedNode", selected.getName()), () -> selected.getClass().getSimpleName());
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
                net.sourceforge.fddtools.service.LoggingService.getInstance().audit("nodeCopy", java.util.Map.of("selectedNode", selected.getName()), () -> selected.getClass().getSimpleName());
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
                    if (cmd.getPasted() != null) {
                        net.sourceforge.fddtools.service.LoggingService.getInstance().audit("nodePaste", java.util.Map.of("selectedNode", cmd.getPasted().getName()), () -> clipboard.getClass().getSimpleName());
                    }
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
                    if (projectTreeFX != null) {
                        projectTreeFX.refresh();
                        projectTreeFX.selectNode(node);
                    }
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
        // Headless / test guard: if stage has no scene yet, skip dialog prompts
        if (primaryStage == null || primaryStage.getScene() == null) {
            if (ModelState.getInstance().isDirty()) {
                LOGGER.warn("Skipping unsaved-changes prompt (no scene yet) - test/headless mode");
            }
            return true;
        }
        if (ModelState.getInstance().isDirty()) {
            ButtonType saveButton = new ButtonType("Save");
            ButtonType dontSaveButton = new ButtonType("Don't Save");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType choice = DialogService.getInstance().confirmWithChoices(primaryStage,
                    "Unsaved Changes",
                    "Save changes before closing?",
                    "You have unsaved changes. Do you want to save them?",
                    saveButton, dontSaveButton, cancelButton);
            if (choice == saveButton) { return saveCurrentProjectBlocking(); }
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
    javafx.scene.text.Font fxFont = DEFAULT_FONT;
	canvasFX = new FDDCanvasFX(rootNode, fxFont);
    canvasFX.restoreLastZoomIfEnabled();
        rightSplitPane.getItems().clear();
        rightSplitPane.getItems().add(canvasFX);
        if (infoPanelContainer.isVisible()) {
            rightSplitPane.getItems().add(infoPanelContainer);
            rightSplitPane.setDividerPositions(0.7);
        } else {
            rightSplitPane.setDividerPositions(1.0);
        }
    mainSplitPane.getItems().clear();
    // Ensure project tree keeps a reasonable fixed min width and does not collapse
    projectTreeFX.setMinWidth(140);
    projectTreeFX.setPrefWidth(220);
    // Wrap right side in a BorderPane so canvas can grow while optional panels share space
    BorderPane rightWrapper = new BorderPane(rightSplitPane);
    rightWrapper.setMinWidth(200);
    // Allow right side (canvas) to grow while left stays visible
    javafx.scene.layout.Priority priority = javafx.scene.layout.Priority.ALWAYS;
    javafx.scene.layout.HBox.setHgrow(rightWrapper, priority);
    mainSplitPane.getItems().addAll(projectTreeFX, rightWrapper);
        double pos = LayoutPreferencesService.getInstance().getMainDividerPosition().orElse(0.25);
        mainSplitPane.setDividerPositions(pos);
    // Apply initial fixed percentages after layout pass
        updateTitle();
        updateUndoRedoState();
        if (isNew) {
            // Register existing root instance with ProjectService so subsequent saves serialize same object
            ProjectService.getInstance().newProject(rootNode, rootNode.getName());
        } else {
            // When opening, we already called ProjectService.open/openWithRoot before this method; do not overwrite root
        }
    }

    // (Removed fixed-percentage panel logic; right controls now part of canvas action bar.)

    private void loadProjectFromPath(String absolutePath, boolean fromRecent) {
        if (absolutePath == null) return;
        LOGGER.debug("Loading project from path: {} (recent={})", absolutePath, fromRecent);
        try {
            FDDINode rootNode = (FDDINode) FDDIXMLFileReader.read(absolutePath);
            if (rootNode == null) {
                showErrorDialog("Open Project Failed", "Failed to parse the selected file.");
                return;
            }
            ProjectService.getInstance().openWithRoot(absolutePath, rootNode);
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
