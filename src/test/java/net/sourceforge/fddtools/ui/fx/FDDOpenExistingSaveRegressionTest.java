package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.persistence.FDDIXMLFileWriter;
import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test for previously observed bug: opening an existing file then performing Save overwrote
 * the file with a minimal "New Program" root (ProjectService root mismatch).
 * This test ensures that:
 *  1) Open registers the loaded root with ProjectService
 *  2) Added child nodes persist on Save
 *  3) Reopen shows same structure (not collapsed to empty)
 */
public class FDDOpenExistingSaveRegressionTest {
    private static boolean fxStarted;
    private FDDMainWindowFX window;

    @BeforeAll
    static void startFx() throws Exception {
        if (!fxStarted) {
            CountDownLatch latch = new CountDownLatch(1);
            try { Platform.startup(latch::countDown); } catch (IllegalStateException already) { latch.countDown(); }
            latch.await();
            fxStarted = true;
        }
    }

    @BeforeEach
    void setup() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { window = new FDDMainWindowFX(new javafx.stage.Stage()); latch.countDown(); });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        // Clear any initial project state
        ProjectService.getInstance().clear();
        ModelState.getInstance().setDirty(false);
    }

    @Test
    void openModifySavePersistsStructure() throws Exception {
        // Build a program with one project child
        ObjectFactory factory = new ObjectFactory();
        Program program = factory.createProgram();
        program.setName("Existing Program");
        Project child = factory.createProject();
        child.setName("Child Project");
        // Add child project via underlying JAXB list (assume getProject())
        try {
            @SuppressWarnings("unchecked")
            List<Project> list = (List<Project>) Program.class.getMethod("getProject").invoke(program);
            list.add(child);
        } catch (NoSuchMethodException nsme) {
            fail("Program.getProject() not found; model structure changed");
        }
        Path temp = Files.createTempFile("fdd-regression-existing", ".fddi");
        temp.toFile().deleteOnExit();
        assertTrue(FDDIXMLFileWriter.write(program, temp.toString()));

        // Open the file via window helper (reflective load path used by Open Recent)
        Method loadProjectFromPath = FDDMainWindowFX.class.getDeclaredMethod("loadProjectFromPath", String.class, boolean.class);
        loadProjectFromPath.setAccessible(true);
        loadProjectFromPath.invoke(window, temp.toString(), false);

        // Verify ProjectService root is the loaded program with one project
        FDDINode psRoot = ProjectService.getInstance().getRoot();
        assertNotNull(psRoot, "ProjectService root should be set after open");
        assertTrue(psRoot instanceof Program, "Root should be Program");
        @SuppressWarnings("unchecked") List<Project> projects = (List<Project>) Program.class.getMethod("getProject").invoke(psRoot);
        assertEquals(1, projects.size(), "Loaded program should have one child project before modification");

        // Add another project node programmatically to simulate user add
        Project added = factory.createProject();
        added.setName("Added Project");
        projects.add(added);
        ProjectService.getInstance().markDirty();
        Thread.sleep(120); // allow dirty flag propagate
        assertTrue(ModelState.getInstance().isDirty(), "Dirty flag should be set after modification");

        // Save to same path silently
        Method saveToFile = FDDMainWindowFX.class.getDeclaredMethod("saveToFile", String.class);
        saveToFile.setAccessible(true);
        saveToFile.invoke(window, temp.toString());
        waitFor(() -> !ModelState.getInstance().isDirty(), 5000);

        // Re-open file fresh to verify two projects persisted
        ProjectService.getInstance().clear();
        loadProjectFromPath.invoke(window, temp.toString(), false);
        FDDINode reopened = ProjectService.getInstance().getRoot();
        assertTrue(reopened instanceof Program);
        @SuppressWarnings("unchecked") List<Project> reopenedProjects = (List<Project>) Program.class.getMethod("getProject").invoke(reopened);
        assertEquals(2, reopenedProjects.size(), "After save + reopen, project count should be 2 (original + added)");
    }

    private void waitFor(Check c, long timeoutMs) throws InterruptedException { long start=System.currentTimeMillis(); while(System.currentTimeMillis()-start<timeoutMs){ if(c.ok()) return; Thread.sleep(50);} fail("Condition not met in timeout"); }
    @FunctionalInterface private interface Check { boolean ok(); }
}
