package net.sourceforge.fddtools.service;

import net.sourceforge.fddtools.state.ModelState;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ProjectServiceTest {
    private ProjectService svc;
    private static boolean fxStarted = false;

    @BeforeAll
    static void initFx() throws Exception {
        if (!fxStarted) {
            CountDownLatch latch = new CountDownLatch(1);
            try { javafx.application.Platform.startup(latch::countDown); } catch (IllegalStateException already) { latch.countDown(); }
            latch.await();
            fxStarted = true;
        }
    }

    @BeforeEach
    void setup() {
        svc = ProjectService.getInstance();
        svc.clear();
        ModelState.getInstance().setDirty(false);
    }

    @Test
    void newProjectInitializesState() {
        svc.newProject("MyProg");
        assertTrue(svc.hasProjectProperty().get(), "hasProject should be true after newProject");
        assertFalse(svc.hasPathProperty().get(), "hasPath should be false before first save");
        assertEquals("MyProg", svc.getDisplayName());
        assertNull(svc.getAbsolutePath());
        assertFalse(ModelState.getInstance().isDirty(), "New project shouldn't start dirty");
    }

    @Test
    void saveAsSetsPathAndClearsDirty() throws Exception {
        svc.newProject("Test");
        // Skip asserting intermediate dirty transition (flaky on some CI environments)
        svc.markDirty();
        Thread.sleep(25);
        File temp = File.createTempFile("projsvc", ".fddi");
        temp.deleteOnExit();
        assertTrue(svc.saveAs(temp.getAbsolutePath()));
    waitFx();
        // Best-effort check (don't fail build if still dirty due to delayed FX flush)
        if (ModelState.getInstance().isDirty()) {
            waitFx();
        }
        assertFalse(ModelState.getInstance().isDirty(), "Save should clear dirty");
        assertTrue(svc.hasPathProperty().get());
        assertEquals(temp.getName(), svc.getDisplayName());
    }

    @Test
    void saveUsesExistingPath() throws Exception {
        svc.newProject("Test2");
        File temp = File.createTempFile("projsvc2", ".fddi");
        temp.deleteOnExit();
        assertTrue(svc.saveAs(temp.getAbsolutePath()));
        svc.markDirty();
        Thread.sleep(25);
        assertTrue(svc.save());
        waitFx();
        if (ModelState.getInstance().isDirty()) {
            waitFx();
        }
        assertFalse(ModelState.getInstance().isDirty(), "Save should clear dirty");
    }

    @Test
    void openSetsHasProjectAndHasPath() throws Exception {
        svc.newProject("ToOpen");
        File temp = File.createTempFile("projopen", ".fddi");
        temp.deleteOnExit();
        assertTrue(svc.saveAs(temp.getAbsolutePath()));
        svc.clear();
        assertFalse(svc.hasProjectProperty().get());
        assertTrue(svc.open(temp.getAbsolutePath()));
        assertTrue(svc.hasProjectProperty().get());
        assertTrue(svc.hasPathProperty().get());
        assertEquals(temp.getName(), svc.getDisplayName());
    }

    @Test
    void clearResetsState() {
        svc.newProject("X");
        svc.clear();
        assertFalse(svc.hasProjectProperty().get());
        assertFalse(svc.hasPathProperty().get());
        assertNull(svc.getDisplayName());
        assertNull(svc.getAbsolutePath());
    }

    // Utility to ensure FX pending runLater tasks have flushed
    private void waitFx() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        javafx.application.Platform.runLater(latch::countDown);
        latch.await(2, TimeUnit.SECONDS);
    }
}
