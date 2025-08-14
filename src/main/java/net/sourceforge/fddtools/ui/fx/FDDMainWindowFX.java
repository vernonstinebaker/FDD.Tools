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

@SuppressWarnings({"unused"}) // Some menu item references kept for future dynamic enablement even if not yet used
public class FDDMainWindowFX extends BorderPane implements FDDTreeContextMenuHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FDDMainWindowFX.class);
    
    // Core components
    private final Stage primaryStage;
    // Former options model removed; keep default font settings locally
    private static final javafx.scene.text.Font DEFAULT_FONT = javafx.scene.text.Font.font("System", 12);
    // Clipboard state moved into FDDNodeEditActions
    
    // UI Components
    private MenuBar menuBar;
    private ToolBar toolBar;
    private SplitPane mainSplitPane;
    private SplitPane rightSplitPane; // For canvas and panels
    private TabPane infoPanelContainer; // Container for aspect and work package panels
    private FDDTreeViewFX projectTreeFX;
    private FDDCanvasFX canvasFX;
    private FDDStatusBarFX statusBar; // retains action panel + undo/redo summary only
    
    // Menu component references retained for dynamic enable/disable and label updates
    private Menu recentFilesMenu; // dynamic recent files submenu
    private MenuItem fileSave;
    private MenuItem fileSaveAs;
    private MenuItem editCut;
    private MenuItem editCopy;
    private MenuItem editPaste;
    private MenuItem editDelete;
    private MenuItem editEdit;
    private MenuItem editUndo;
    private MenuItem editRedo;

    // Command & helper infrastructure
    private final CommandExecutionService commandExec; // centralized command execution
    private final FDDFileActions fileActions;
    private final FDDCommandBindings commandBindings;
    private final FDDLayoutController layoutController;
    private final ProjectLifecycleController projectLifecycle;
    private FDDNodeEditActions nodeActions;
    private SelectionCommandMediator selectionMediator;

    public FDDMainWindowFX(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.commandExec = CommandExecutionService.getInstance();
    this.fileActions = new FDDFileActions(new FDDFileActions.Host() {
            @Override public void showErrorDialog(String title, String message) { FDDMainWindowFX.this.showErrorDialog(title,message); }
            @Override public void refreshRecentFilesMenu() { FDDMainWindowFX.this.refreshRecentFilesMenu(); }
            @Override public void updateTitle() { FDDMainWindowFX.this.updateTitle(); }
            @Override public boolean canClose() { return FDDMainWindowFX.this.canClose(); }
            @Override public void loadProjectFromPath(String path, boolean rebuildUI) throws Exception { layoutController.loadProjectFromPath(path); }
            @Override public void rebuildProjectUI(FDDINode root, boolean markDirty) { layoutController.rebuildProjectUI(root, markDirty); }
            @Override public Stage getPrimaryStage() { return primaryStage; }
        });
    this.commandBindings = new FDDCommandBindings(commandExec, this::refreshView, this::updateTitle);
    this.layoutController = new FDDLayoutController(new FDDLayoutController.Host() {
        @Override public void setProjectTree(FDDTreeViewFX tree) { projectTreeFX = tree; }
        @Override public void setCanvas(FDDCanvasFX canvas) { canvasFX = canvas; }
        @Override public void onSelectionChanged(FDDINode node) { onTreeSelectionChanged(node); }
        @Override public void updateTitle() { FDDMainWindowFX.this.updateTitle(); }
        @Override public void showErrorDialog(String title, String msg) { FDDMainWindowFX.this.showErrorDialog(title,msg); }
        @Override public void updateUndoRedo() { commandBindings.updateUndoRedoState(); }
        @Override public javafx.scene.text.Font getDefaultFont() { return DEFAULT_FONT; }
        @Override public SplitPane getMainSplit() { return mainSplitPane; }
        @Override public SplitPane getRightSplit() { return rightSplitPane; }
        @Override public TabPane getInfoTabs() { return infoPanelContainer; }
    @Override public FDDTreeContextMenuHandler contextMenuHandler() { return FDDMainWindowFX.this; }
    });
    this.projectLifecycle = new ProjectLifecycleController(new ProjectLifecycleController.Host() {
        @Override public Stage getPrimaryStage() { return primaryStage; }
        @Override public void rebuildProjectUI(FDDINode root, boolean markDirty) { layoutController.rebuildProjectUI(root, markDirty); }
        @Override public void refreshRecentFilesMenu() { FDDMainWindowFX.this.refreshRecentFilesMenu(); }
        @Override public void updateTitle() { FDDMainWindowFX.this.updateTitle(); }
        @Override public boolean canClose() { return FDDMainWindowFX.this.canClose(); }
        @Override public void showErrorDialog(String title, String message) { FDDMainWindowFX.this.showErrorDialog(title,message); }
    });
        
    // Options system removed; using default font configuration
        
        // Setup macOS integration FIRST
        setupMacOSIntegration();
        
        // Build the UI
        initializeComponents();
        layoutComponents();
        // Initialize node edit actions helper
        this.nodeActions = new FDDNodeEditActions(commandExec, new FDDNodeEditActions.Host() {
            @Override public FDDINode getSelectedNode() { return FDDMainWindowFX.this.getSelectedNode(); }
            @Override public void afterModelMutation(FDDINode nodeToSelect) { selectionMediator.afterModelMutation(nodeToSelect); }
            @Override public void markDirty() { FDDMainWindowFX.this.markDirty(); }
            @Override public boolean isRoot(FDDINode node) { return projectTreeFX!=null && projectTreeFX.getRoot()!=null && projectTreeFX.getRoot().getValue()==node; }
            @Override public void showError(String title, String message) { showErrorDialog(title, message); }
            @Override public Stage getPrimaryStage() { return primaryStage; }
        });
        this.selectionMediator = new SelectionCommandMediator(new SelectionCommandMediator.Host() {
            @Override public FDDTreeViewFX getProjectTree() { return projectTreeFX; }
            @Override public FDDCanvasFX getCanvas() { return canvasFX; }
            @Override public Stage getPrimaryStage() { return primaryStage; }
            @Override public void configureDialogCentering(Stage stage) { FDDMainWindowFX.this.configureDialogCentering(stage); }
            @Override public void updateUndoRedo() { commandBindings.updateUndoRedoState(); }
        }, commandExec, commandBindings);
        
    // Create new project
    projectLifecycle.requestNewProject();
        // Bind menu enablement to model state
    // Menu items now use direct property bindings; no additional listener wiring needed.
        
        LOGGER.info("FDDMainWindowFX initialized successfully");
        // Subscribe to model events
        net.sourceforge.fddtools.state.ModelEventBus.get().subscribe(new java.util.function.Consumer<>() {
            private boolean scheduled;
            private void schedule(){ if(scheduled) return; scheduled=true; Platform.runLater(()->{ scheduled=false; refreshView(); }); }
            @Override public void accept(net.sourceforge.fddtools.state.ModelEventBus.Event ev){
                switch(ev.type){
                    case NODE_UPDATED, TREE_STRUCTURE_CHANGED, PROJECT_LOADED -> schedule();
                    case UI_LANGUAGE_CHANGED -> Platform.runLater(() -> { FDDMainWindowFX.this.refreshI18n(); FDDMainWindowFX.this.refreshView(); });
                    case UI_THEME_CHANGED -> Platform.runLater(this::schedule);
                }
            }
        });
    }
    
    private void setupMacOSIntegration() {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            try {
                // macOS handlers now encapsulated in MacOSIntegrationService (no-op placeholder for future Desktop API binding)
                org.slf4j.LoggerFactory.getLogger(FDDMainWindowFX.class).info("macOS integration initialized (handlers managed centrally)");
            } catch (Exception e) {
                LOGGER.warn("Failed to setup macOS integration: {}", e.getMessage());
            }
        }
    }
    
    private void initializeComponents() {
        // Create menu bar via factory
        var menuComponents = FDDMainMenuFactory.build(new FDDMainMenuFactory.Actions() {
            @Override public void onNew() { requestNewProject(); }
            @Override public void onOpen() { requestOpenProject(); }
            @Override public void onSave() { fileActions.saveProject(); }
            @Override public void onSaveAs() { fileActions.saveProjectAs(); }
            @Override public void onExit() { exitApplication(); }
            @Override public void onUndo() { commandBindings.performUndo(); }
            @Override public void onRedo() { commandBindings.performRedo(); }
            @Override public void onCut() { nodeActions.cut(); }
            @Override public void onCopy() { nodeActions.copy(); }
            @Override public void onPaste() { nodeActions.paste(); }
            @Override public void onDelete() { nodeActions.delete(); }
            @Override public void onEdit() { selectionMediator.editSelectedNode(); }
            @Override public void onPreferences() { showPreferencesDialog(); }
            @Override public void onRefresh() { refreshView(); }
            @Override public void onAbout() { showAboutDialog(); }
        }, primaryStage);
        menuBar = menuComponents.menuBar();
        recentFilesMenu = menuComponents.recentFilesMenu();
        fileSave = menuComponents.fileSave();
        fileSaveAs = menuComponents.fileSaveAs();
        editCut = menuComponents.editCut();
        editCopy = menuComponents.editCopy();
        editPaste = menuComponents.editPaste();
        editDelete = menuComponents.editDelete();
        editEdit = menuComponents.editEdit();
        editUndo = menuComponents.editUndo();
        editRedo = menuComponents.editRedo();
        // Create toolbar via factory
        toolBar = FDDToolBarFactory.build(new FDDToolBarFactory.Actions() {
            @Override public void onNew() { newProject(); }
            @Override public void onOpen() { requestOpenProject(); }
            @Override public void onSave() { fileActions.saveProject(); }
            @Override public void onCut() { nodeActions.cut(); }
            @Override public void onCopy() { nodeActions.copy(); }
            @Override public void onPaste() { nodeActions.paste(); }
        });
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
    // Create status bar component (no generic status text)
    statusBar = new FDDStatusBarFX();
    statusBar.setActionHandler(new FDDActionPanelFX.FDDActionHandler() {
            @Override public void onAdd() { addFromSelected(); }
            @Override public void onDelete() { var n = getSelectedNode(); if (n!=null) deleteNode(n); }
            @Override public void onEdit() { var n = getSelectedNode(); if (n!=null) editNode(n); }
            @Override public void onAddProgram() { var n = getSelectedNode(); if (n!=null) addProgram(n); }
            @Override public void onAddProject() { var n = getSelectedNode(); if (n!=null) addProject(n); }
            @Override public FDDINode getSelectedNode() { return FDDMainWindowFX.this.getSelectedNode(); }
        });
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
    
    // Removed large inline createMenuBar() implementation (moved to FDDMainMenuFactory)
    
    // removed inline createToolBar (extracted to FDDToolBarFactory)
    
    // helper for status bar add action (moved logic)
    private void addFromSelected() {
        FDDINode selectedNode = getSelectedNode();
        if (selectedNode == null) return;
        if (selectedNode instanceof Program) {
            addProject(selectedNode);
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
    
    private void createInfoPanelContainer() {
        infoPanelContainer = new TabPane();
        infoPanelContainer.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        infoPanelContainer.setPrefHeight(250);
        infoPanelContainer.setVisible(false); // Hidden by default until a relevant node is selected
        
        // Container starts empty - tabs will be added dynamically based on node selection
    }
    
    // Wrapper that handles unsaved state before creating new project
    private void requestNewProject(){ projectLifecycle.requestNewProject(); }
    private void requestOpenProject(){ projectLifecycle.requestOpenProject(); }

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

    // Lightweight i18n refresh hook. Rebuild menus/labels that use I18n registries.
    private void refreshI18n() {
        try {
            // Rebuild menu bar text via factory using existing actions
            var menuComponents = FDDMainMenuFactory.build(new FDDMainMenuFactory.Actions() {
                @Override public void onNew() { requestNewProject(); }
                @Override public void onOpen() { requestOpenProject(); }
                @Override public void onSave() { fileActions.saveProject(); }
                @Override public void onSaveAs() { fileActions.saveProjectAs(); }
                @Override public void onExit() { exitApplication(); }
                @Override public void onUndo() { commandBindings.performUndo(); }
                @Override public void onRedo() { commandBindings.performRedo(); }
                @Override public void onCut() { nodeActions.cut(); }
                @Override public void onCopy() { nodeActions.copy(); }
                @Override public void onPaste() { nodeActions.paste(); }
                @Override public void onDelete() { nodeActions.delete(); }
                @Override public void onEdit() { selectionMediator.editSelectedNode(); }
                @Override public void onPreferences() { showPreferencesDialog(); }
                @Override public void onRefresh() { refreshView(); }
                @Override public void onAbout() { showAboutDialog(); }
            }, primaryStage);
            menuBar.getMenus().setAll(menuComponents.menuBar().getMenus());
        } catch (Exception ignored) { }
    }

    private boolean saveCurrentProjectBlocking(){ return projectLifecycle.saveBlocking(); }

    private void newProject() { requestNewProject(); }

    private void openSpecificProject(String path) { projectLifecycle.openSpecificRecent(path); }
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
    
    private FDDINode createNewRootNode() { return null; } // now handled in ProjectLifecycleController
    
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
    
    private void closeCurrentProject() { layoutController.closeCurrentProject(); }
    
    // save operations delegated to FDDFileActions (saveProject / saveProjectAs / saveToFile removed)

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
    
    // cut/copy/paste/delete logic moved to FDDNodeEditActions
    
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
                selectionMediator.afterModelMutation(newNode);
                LOGGER.info("Added new node via command: " + newNode.getClass().getSimpleName());
            } else {
                LOGGER.info("User cancelled adding new " + newNode.getClass().getSimpleName());
            }
        });
    }
    
    // undo/redo delegated to commandBindings
    private void markDirty() { ProjectService.getInstance().markDirty(); commandBindings.updateUndoRedoState(); }
    // afterModelMutation now handled by SelectionCommandMediator
    // ===== Restored methods & interface implementations (previously truncated) =====

    // FDDTreeContextMenuHandler implementation
    @Override
    public void onSelectionChanged(FDDINode selectedNode) { selectionMediator.onTreeSelectionChanged(selectedNode); }
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
    public void editNode(FDDINode node) { selectionMediator.editSelectedNode(node); }
    @Override
    public void deleteNode(FDDINode node) { nodeActions.delete(); }

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
                        commandBindings.updateUndoRedoState();
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

    private void onTreeSelectionChanged(FDDINode selectedNode) { selectionMediator.onTreeSelectionChanged(selectedNode); }

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
    // rebuildProjectUI delegated to FDDLayoutController

    // (Removed fixed-percentage panel logic; right controls now part of canvas action bar.)

    // loadProjectFromPath delegated to FDDLayoutController

    // Package-private getters for tests
    FDDTreeViewFX getProjectTree() { return projectTreeFX; }
    FDDCanvasFX getCanvas() { return canvasFX; }

    // === Backwards-compatibility private helpers for existing tests ===
    // Delegates to extracted FDDFileActions implementation. Tests access via reflection.
    private boolean saveToFile(String fileName) { return fileActions.saveToFile(fileName); }
    private void loadProjectFromPath(String path, boolean rebuildUI) throws Exception { layoutController.loadProjectFromPath(path); }
    private void rebuildProjectUI(FDDINode root, boolean markDirty) {
        // markDirty parameter preserved for backward compatibility with tests; layout controller
        // includes its own 'isNew' flag which we don't expose here (always false for rebuild path).
        layoutController.rebuildProjectUI(root, false);
        if (markDirty) ProjectService.getInstance().markDirty();
    }
}
