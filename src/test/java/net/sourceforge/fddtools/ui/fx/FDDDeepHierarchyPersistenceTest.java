package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.persistence.FDDIXMLFileWriter;
import net.sourceforge.fddtools.testutil.FxTestUtil;
import com.nebulon.xml.fddi.*;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression guard: ensure a deep hierarchy (Program->Project->Aspect->Subject->Activity->Feature)
 * remains intact across open -> modify -> save -> reopen.
 * Helps detect any path where an unsaved transient root is serialized instead of the loaded root.
 */
public class FDDDeepHierarchyPersistenceTest {
    private static boolean fxStarted;
    private FDDMainWindowFX window;

    @BeforeAll
    static void startFx() throws Exception {
        if (!fxStarted) {
            FxTestUtil.ensureStarted();
            fxStarted = true;
        }
    }

    @BeforeEach
    void setup() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { window = new FDDMainWindowFX(new javafx.stage.Stage()); latch.countDown(); });
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        ProjectService.getInstance().clear();
        ModelState.getInstance().setDirty(false);
    }

    @Test
    void deepHierarchyPersistsAfterSave() throws Exception {
    ObjectFactory of = new ObjectFactory();
    Program program = of.createProgram(); program.setName("Deep Program");
    Project project = of.createProject(); project.setName("Project A");
    program.getProject().add(project); // initial minimal structure only

    Path temp = Files.createTempFile("fdd-deep-hierarchy", ".fddi");
    temp.toFile().deleteOnExit();
    assertTrue(FDDIXMLFileWriter.write(program, temp.toString()));

    Method loadProjectFromPath = FDDMainWindowFX.class.getDeclaredMethod("loadProjectFromPath", String.class, boolean.class);
    loadProjectFromPath.setAccessible(true);
    loadProjectFromPath.invoke(window, temp.toString(), false);

    // Build deeper hierarchy AFTER open to mirror real user edits (avoids JAXB ordering constraints during initial write)
    Program opened = (Program) ProjectService.getInstance().getRoot();
    Project openedProject = opened.getProject().get(0);
    Aspect aspect = of.createAspect(); aspect.setName("Aspect 1");
    openedProject.getAspect().add(aspect);
    Subject subject = of.createSubject(); subject.setPrefix("SUBJ"); subject.setName("Subject X");
    aspect.getSubject().add(subject);
    Activity activity = of.createActivity(); activity.setName("Activity 42");
    subject.getActivity().add(activity);
    Feature feature = of.createFeature(); feature.setName("Feature Z"); feature.setSeq(1); feature.setSequence(1);
    activity.getFeature().add(feature);
    // Add second feature for breadth.
    Feature feature2 = of.createFeature(); feature2.setName("Feature Y"); feature2.setSeq(2); feature2.setSequence(2);
    activity.getFeature().add(feature2);

    ProjectService.getInstance().markDirty();
    Thread.sleep(120);
    assertTrue(ModelState.getInstance().isDirty());

    // Silent save
    Method saveToFile = FDDMainWindowFX.class.getDeclaredMethod("saveToFile", String.class);
    saveToFile.setAccessible(true);
    saveToFile.invoke(window, temp.toString());
    waitFor(() -> !ModelState.getInstance().isDirty(), 7000);

    // Clear and reopen
    ProjectService.getInstance().clear();
    loadProjectFromPath.invoke(window, temp.toString(), false);
    FDDINode reopened = ProjectService.getInstance().getRoot();
    assertProgramDepth(reopened, 1,1,1,1,2); // two features now
    }

    private void assertProgramDepth(FDDINode root, int projects, int aspects, int subjects, int activities, int features) throws Exception {
        assertNotNull(root);
        assertTrue(root instanceof Program);
        Program prog = (Program) root;
        assertEquals(projects, prog.getProject().size(), "Project count mismatch");
        Project proj = prog.getProject().get(0);
        assertEquals(aspects, proj.getAspect().size(), "Aspect count mismatch");
        Aspect asp = proj.getAspect().get(0);
        assertEquals(subjects, asp.getSubject().size(), "Subject count mismatch");
        Subject subj = asp.getSubject().get(0);
        assertEquals(activities, subj.getActivity().size(), "Activity count mismatch");
        Activity act = subj.getActivity().get(0);
        assertEquals(features, act.getFeature().size(), "Feature count mismatch");
    }

    private void waitFor(Check c, long timeoutMs) throws InterruptedException { long start=System.currentTimeMillis(); while(System.currentTimeMillis()-start<timeoutMs){ if(c.ok()) return; Thread.sleep(50);} fail("Condition not met in timeout"); }
    @FunctionalInterface private interface Check { boolean ok(); }
}
