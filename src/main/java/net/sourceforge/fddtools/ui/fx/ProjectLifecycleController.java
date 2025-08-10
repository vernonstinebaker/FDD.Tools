package net.sourceforge.fddtools.ui.fx;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.persistence.FDDIXMLFileReader;
import net.sourceforge.fddtools.service.DialogService;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.util.PreferencesService;
import net.sourceforge.fddtools.util.UnsavedChangesHandler;
import net.sourceforge.fddtools.util.RecentFilesService;
import net.sourceforge.fddtools.util.I18n; // added
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Extracted controller focused strictly on project lifecycle concerns (new/open/save/recents).
 * UI interactions (dialogs) are funneled through DialogService. Heavy logic removed from FDDMainWindowFX.
 */
public class ProjectLifecycleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectLifecycleController.class);

    public interface Host {
        Stage getPrimaryStage();
        void rebuildProjectUI(FDDINode root, boolean markDirty);
        void refreshRecentFilesMenu();
        void updateTitle();
        boolean canClose();
        void showErrorDialog(String title, String message);
    }

    private final Host host;
    public ProjectLifecycleController(Host host){ this.host = host; }

    // --- Public API ---
    public void requestNewProject(){ if (!host.canClose()) return; createFreshProject(); }
    public void requestOpenProject(){ if (!host.canClose()) return; openProjectDialog(); }
    public void openSpecificRecent(String path){ handleUnsavedChanges(() -> openRecentInternal(path)); }
    public boolean saveBlocking(){ return saveCurrentProjectBlocking(); }

    // --- Implementation ---
    private void createFreshProject(){ FDDINode rootNode = createNewRootNode(); host.rebuildProjectUI(rootNode, true); net.sourceforge.fddtools.state.ModelEventBus.get().publish(net.sourceforge.fddtools.state.ModelEventBus.EventType.PROJECT_LOADED, rootNode); LOGGER.info("New project created"); }

    private FDDINode createNewRootNode(){ try { var of = new com.nebulon.xml.fddi.ObjectFactory(); var prog = of.createProgram(); prog.setName("New Program"); return (FDDINode)prog; } catch(Exception e){ LOGGER.error("Failed to create root: {}", e.getMessage(), e); return null; } }

    private void openProjectDialog(){ try { FileChooser fc = new FileChooser(); fc.setTitle("Open FDD Project"); fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("FDD Files","*.fddi","*.xml"), new FileChooser.ExtensionFilter("All Files","*.*")); File f = fc.showOpenDialog(host.getPrimaryStage()); if(f!=null){ FDDINode root=(FDDINode)FDDIXMLFileReader.read(f.getAbsolutePath()); host.rebuildProjectUI(root, false); net.sourceforge.fddtools.state.ModelEventBus.get().publish(net.sourceforge.fddtools.state.ModelEventBus.EventType.PROJECT_LOADED, root); RecentFilesService.getInstance().addRecentFile(f.getAbsolutePath()); host.refreshRecentFilesMenu(); PreferencesService.getInstance().setLastProjectPath(f.getAbsolutePath()); PreferencesService.getInstance().flushNow(); } } catch(Exception e){ LOGGER.error("Open failed: {}", e.getMessage(), e); host.showErrorDialog("Open Project Failed", "Error loading file: "+e.getMessage()); } }

    private void openRecentInternal(String path){ File f = new File(path); if(!f.exists()){ host.showErrorDialog("Open Recent", "File no longer exists: "+path); RecentFilesService.getInstance().clear(); host.refreshRecentFilesMenu(); return; } try { FDDINode root=(FDDINode)FDDIXMLFileReader.read(f.getAbsolutePath()); host.rebuildProjectUI(root, false); net.sourceforge.fddtools.state.ModelEventBus.get().publish(net.sourceforge.fddtools.state.ModelEventBus.EventType.PROJECT_LOADED, root); RecentFilesService.getInstance().addRecentFile(path); host.refreshRecentFilesMenu(); } catch(Exception e){ LOGGER.error("Failed to load recent: {}", e.getMessage(), e); host.showErrorDialog("Open Project Failed","Error loading file: "+e.getMessage()); } }

    private void handleUnsavedChanges(Runnable proceed){ UnsavedChangesHandler.handle(ModelState.getInstance().isDirty(), () -> { ButtonType save = new ButtonType(I18n.get("UnsavedChanges.Save")); ButtonType dont = new ButtonType(I18n.get("UnsavedChanges.DontSave")); ButtonType cancel = new ButtonType(I18n.get("BusyOverlay.Cancel"), ButtonBar.ButtonData.CANCEL_CLOSE); ButtonType c = DialogService.getInstance().confirmWithChoices(host.getPrimaryStage(),I18n.get("UnsavedChanges.Title"),I18n.get("UnsavedChanges.Header"),I18n.get("UnsavedChanges.Content"), save,dont,cancel); if(c==save) return UnsavedChangesHandler.Decision.SAVE; if(c==dont) return UnsavedChangesHandler.Decision.DONT_SAVE; return UnsavedChangesHandler.Decision.CANCEL; }, this::saveCurrentProjectBlocking, proceed); }

    private boolean saveCurrentProjectBlocking(){ try{ ProjectService ps = ProjectService.getInstance(); if(ps.getRoot()==null) return true; String before = ps.getAbsolutePath(); if(before!=null){ boolean ok=ps.save(); if(ok){ PreferencesService.getInstance().setLastProjectPath(ps.getAbsolutePath()); PreferencesService.getInstance().flushNow(); host.updateTitle(); } return ok; } else { FileChooser fc=new FileChooser(); fc.setTitle("Save FDD Project"); fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("FDD Files","*.fddi"), new FileChooser.ExtensionFilter("XML Files","*.xml")); fc.setInitialFileName(buildDefaultSaveFileName(ps.getDisplayName())); File f=fc.showSaveDialog(host.getPrimaryStage()); if(f==null) return false; boolean ok=ps.saveAs(ensureFddiOrXmlExtension(f.getAbsolutePath())); if(ok){ PreferencesService.getInstance().setLastProjectPath(ps.getAbsolutePath()); PreferencesService.getInstance().flushNow(); host.updateTitle(); } return ok; } } catch(Exception ex){ LOGGER.error("Save failed: {}", ex.getMessage(), ex); host.showErrorDialog("Save Failed", ex.getMessage()); return false; } }

    // --- Helpers replicated (could be moved to utility class) ---
    static String buildDefaultSaveFileName(String displayName){ String base = displayName; if(base==null||base.isBlank()||base.equalsIgnoreCase("New Program")||base.equalsIgnoreCase("New Program.fddi")) base="New Program"; while(base.toLowerCase().endsWith(".fddi")) base=base.substring(0, base.length()-5); return base; }
    static String ensureFddiOrXmlExtension(String path){ if(path==null) return null; String lower=path.toLowerCase(); if(lower.endsWith(".fddi")||lower.endsWith(".xml")) return path; return path+".fddi"; }
}
