package net.sourceforge.fddtools.ui.fx;

import net.sourceforge.fddtools.service.PreferencesService;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.testutil.HeadlessTestUtil;
import net.sourceforge.fddtools.util.FileNameUtil;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused unit tests for ProjectLifecycleController helper logic and branching.
 * Uses a lightweight Host stub to capture calls without spinning full JavaFX UI.
 */
public class ProjectLifecycleControllerTest {

    static class HostStub implements ProjectLifecycleController.Host {
        List<String> errors = new ArrayList<>();
        boolean canClose = true;
        FDDINode lastRoot;
        boolean rebuilt;
        boolean titleUpdated;
        @Override public javafx.stage.Stage getPrimaryStage() { return null; }
        @Override public void rebuildProjectUI(FDDINode root, boolean markDirty) { this.lastRoot = root; this.rebuilt = true; }
        @Override public void refreshRecentFilesMenu() { }
        @Override public void updateTitle() { titleUpdated = true; }
        @Override public boolean canClose() { return canClose; }
        @Override public void showErrorDialog(String title, String message) { errors.add(title+": "+message); }
    }

    @BeforeEach
    void resetServices(){
        // Clear ProjectService state to ensure test isolation
        net.sourceforge.fddtools.service.ProjectService.getInstance().clear();
        // Avoid calling clear() which touches JavaFX Platform via runLater; simply ensure
        // no recent files; tests that need a project will create one indirectly.
        PreferencesService.getInstance().clearRecentFiles();
    }

    @Test
    void buildDefaultSaveFileName_basicCases(){
        assertEquals("New Program", FileNameUtil.buildDefaultSaveFileName(""));
        assertEquals("New Program", FileNameUtil.buildDefaultSaveFileName(null));
        assertEquals("New Program", FileNameUtil.buildDefaultSaveFileName("New Program"));
        assertEquals("MyProj", FileNameUtil.buildDefaultSaveFileName("MyProj.fddi"));
        assertEquals("MyProj.backup", FileNameUtil.buildDefaultSaveFileName("MyProj.backup.fddi"));
    }

    @Test
    void ensureFddiOrXmlExtension_addsWhenMissing(){
        assertEquals("abc.fddi", FileNameUtil.ensureFddiOrXmlExtension("abc"));
        assertEquals("abc.fddi", FileNameUtil.ensureFddiOrXmlExtension("abc.fddi"));
        assertEquals("abc.xml", FileNameUtil.ensureFddiOrXmlExtension("abc.xml"));
    }

    @Test
    void saveBlocking_noProjectReturnsTrue(){
        HostStub host = new HostStub();
        ProjectLifecycleController plc = HeadlessTestUtil.isHeadlessMode() 
            ? new ProjectLifecycleController(host, HeadlessTestUtil.createHeadlessProjectDialogStrategy())
            : new ProjectLifecycleController(host);
        assertTrue(plc.saveBlocking());
        assertTrue(host.errors.isEmpty());
    }

    @Test
    void newProjectCreatesRoot(){
        HostStub host = new HostStub();
        ProjectLifecycleController plc = HeadlessTestUtil.isHeadlessMode() 
            ? new ProjectLifecycleController(host, HeadlessTestUtil.createHeadlessProjectDialogStrategy())
            : new ProjectLifecycleController(host);
        plc.requestNewProject();
        assertTrue(host.rebuilt);
        assertNotNull(host.lastRoot);
    }
}
