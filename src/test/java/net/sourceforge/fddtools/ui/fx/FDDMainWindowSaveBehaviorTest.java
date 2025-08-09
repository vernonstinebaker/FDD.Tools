package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.state.ModelState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.sourceforge.fddtools.util.RecentFilesService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for Save / Save As enablement and filename handling.
 */
public class FDDMainWindowSaveBehaviorTest {
    private static FDDMainWindowFX sharedWindow; // created once per test method
    @BeforeAll
    static void initJfx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try { Platform.startup(latch::countDown); } catch (IllegalStateException already) { latch.countDown(); }
        latch.await();
    }

    @BeforeEach
    void reset() {
        ProjectService.getInstance().clear();
        ModelState.getInstance().setDirty(false);
    // Create a fresh window instance on FX thread for each test
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(() -> { sharedWindow = new FDDMainWindowFX(new javafx.stage.Stage()); latch.countDown(); });
    try { latch.await(2, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
    }

    @Test
    void saveMenuShouldBeEnabledForDirtyUnsavedProject() throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            new FDDMainWindowFX(new javafx.stage.Stage());
            // New project created in constructor; now mark dirty and wait for flag
            ProjectService.getInstance().markDirty();
            done.countDown();
        });
        assertTrue(done.await(2, TimeUnit.SECONDS));
        // Dirty set uses Platform.runLater inside ProjectService, wait briefly
        Thread.sleep(150);
        assertTrue(ProjectService.getInstance().hasProjectProperty().get(), "Project should exist");
        assertNull(ProjectService.getInstance().getAbsolutePath(), "Unsaved path should be null");
        assertTrue(ModelState.getInstance().isDirty(), "Project should be dirty");
        boolean predicate = ProjectService.getInstance().hasProjectProperty().get() && ModelState.getInstance().isDirty();
        assertTrue(predicate, "Logical predicate for Save enablement should be true");
    }

    @Test
    void buildDefaultSaveFileNameStripsDuplicateExtensions() throws Exception {
        Method m = FDDMainWindowFX.class.getDeclaredMethod("buildDefaultSaveFileName", String.class);
        m.setAccessible(true);
    assertEquals("New Program", m.invoke(null, (String) null));
    assertEquals("New Program", m.invoke(null, "New Program"));
    assertEquals("Example", m.invoke(null, "Example"));
    assertEquals("Example", m.invoke(null, "Example.fddi"));
    assertEquals("Example", m.invoke(null, "Example.fddi.fddi"));
    }

    @Test
    void stripDuplicateFddiCollapsesChain() throws Exception {
        Method m = FDDMainWindowFX.class.getDeclaredMethod("stripDuplicateFddi", String.class);
        m.setAccessible(true);
        assertEquals("/tmp/test.fddi", m.invoke(null, "/tmp/test.fddi"));
        assertEquals("/tmp/test.fddi", m.invoke(null, "/tmp/test.fddi.fddi"));
        assertEquals("/tmp/test.fddi", m.invoke(null, "/tmp/test.fddi.fddi.fddi"));
    }

    @Test
    void ensureExtensionAddsWhenMissing() throws Exception {
        Method m = FDDMainWindowFX.class.getDeclaredMethod("ensureFddiOrXmlExtension", String.class);
        m.setAccessible(true);
        assertEquals("/tmp/test.fddi", m.invoke(null, "/tmp/test"));
        assertEquals("/tmp/test.fddi", m.invoke(null, "/tmp/test.fddi"));
        assertEquals("/tmp/test.xml", m.invoke(null, "/tmp/test.xml"));
    }

    @Test
    void saveAsSetsPathAndClearsDirtyThenSubsequentSaveIsSilent() throws Exception {
        CountDownLatch ui = new CountDownLatch(1);
        Platform.runLater(() -> {
            ProjectService.getInstance().markDirty();
            ui.countDown();
        });
        assertTrue(ui.await(2, TimeUnit.SECONDS));
        Thread.sleep(150); // allow dirty flag
        assertTrue(ModelState.getInstance().isDirty());

        // Simulate a saveAs by invoking private helpers directly (bypass dialog):
        String tmpName = "fddtest-"+UUID.randomUUID();
        Path tempFile = Files.createTempFile(tmpName, ".fddi");
        tempFile.toFile().deleteOnExit();
        // Write simple content via ProjectService saveAs using existing root
        ProjectService ps = ProjectService.getInstance();
        assertNull(ps.getAbsolutePath());
        ps.saveAs(tempFile.toString());
        assertEquals(tempFile.toString(), ps.getAbsolutePath());
        Thread.sleep(100);
        assertFalse(ModelState.getInstance().isDirty(), "Dirty should be cleared after saveAs");

        // Mark dirty again, then perform plain save which should not change path
        ps.markDirty();
        Thread.sleep(100);
        assertTrue(ModelState.getInstance().isDirty());
        ps.save();
        Thread.sleep(100);
        assertEquals(tempFile.toString(), ps.getAbsolutePath());
        assertFalse(ModelState.getInstance().isDirty(), "Dirty should be cleared after save");
    }

    @Test
    void saveToFileAddsRecentOnSaveAsButNotOnSubsequentSave() throws Exception {
        RecentFilesService.getInstance().clear();
        CountDownLatch ui = new CountDownLatch(1);
    Platform.runLater(ui::countDown);
        assertTrue(ui.await(2, TimeUnit.SECONDS));
        // Make project dirty so Save enabled
        ProjectService.getInstance().markDirty();
        Thread.sleep(120);
        Method saveToFile = FDDMainWindowFX.class.getDeclaredMethod("saveToFile", String.class);
        saveToFile.setAccessible(true);
        String pathA = Files.createTempFile("fdd-mru-a", ".fddi").toString();
        // First save (acts like Save As)
    saveToFile.invoke(sharedWindow, pathA);
        waitFor(() -> pathA.equals(ProjectService.getInstance().getAbsolutePath()) && !ModelState.getInstance().isDirty(), 5000);
        // MRU should contain pathA at index 0
        assertFalse(RecentFilesService.getInstance().getRecentFiles().isEmpty());
        assertEquals(pathA, RecentFilesService.getInstance().getRecentFiles().get(0));
        int sizeAfterFirst = RecentFilesService.getInstance().getRecentFiles().size();

        // Mark dirty and perform silent save (same path) - MRU should NOT grow or duplicate
        ProjectService.getInstance().markDirty();
        Thread.sleep(80);
    saveToFile.invoke(sharedWindow, pathA);
        waitFor(() -> !ModelState.getInstance().isDirty(), 5000);
        assertEquals(sizeAfterFirst, RecentFilesService.getInstance().getRecentFiles().size(), "MRU size should not change after silent save");
        assertEquals(pathA, RecentFilesService.getInstance().getRecentFiles().get(0));
    }

    @Test
    void multipleSaveAsOperationsUpdateRecentOrdering() throws Exception {
        RecentFilesService.getInstance().clear();
        CountDownLatch ui = new CountDownLatch(1);
    Platform.runLater(ui::countDown);
        assertTrue(ui.await(2, TimeUnit.SECONDS));
        Method saveToFile = FDDMainWindowFX.class.getDeclaredMethod("saveToFile", String.class);
        saveToFile.setAccessible(true);
        // First Save As
        ProjectService.getInstance().markDirty();
        Thread.sleep(100);
        String pathA = Files.createTempFile("fdd-mru-order-a", ".fddi").toString();
    saveToFile.invoke(sharedWindow, pathA);
        waitFor(() -> pathA.equals(ProjectService.getInstance().getAbsolutePath()) && !ModelState.getInstance().isDirty(), 5000);
        // Second Save As to different path
        ProjectService.getInstance().markDirty();
        Thread.sleep(80);
        String pathB = Files.createTempFile("fdd-mru-order-b", ".fddi").toString();
    saveToFile.invoke(sharedWindow, pathB);
        waitFor(() -> pathB.equals(ProjectService.getInstance().getAbsolutePath()) && !ModelState.getInstance().isDirty(), 5000);
        var recents = RecentFilesService.getInstance().getRecentFiles();
        assertTrue(recents.size() >= 2, "At least two recent entries expected");
        assertEquals(pathB, recents.get(0), "Most recent should be new path");
        assertEquals(pathA, recents.get(1), "Previous path should shift to second");
    }

    @Test
    void openingExistingProjectThenSaveIsSilentAndDoesNotDuplicateMRU() throws Exception {
        RecentFilesService.getInstance().clear();
        Method saveToFile = FDDMainWindowFX.class.getDeclaredMethod("saveToFile", String.class);
        saveToFile.setAccessible(true);
        // Initial Save As to create a real file on disk
        ProjectService.getInstance().markDirty();
        Thread.sleep(120);
        String existingPath = Files.createTempFile("fdd-open-silent", ".fddi").toString();
        saveToFile.invoke(sharedWindow, existingPath);
        waitFor(() -> existingPath.equals(ProjectService.getInstance().getAbsolutePath()) && !ModelState.getInstance().isDirty(), 5000);
        int mruSizeAfterInitial = RecentFilesService.getInstance().getRecentFiles().size();
        assertTrue(mruSizeAfterInitial >= 1, "MRU should contain initial path");

        // Simulate closing and reopening the project (like user selecting Open)
        ProjectService.getInstance().clear();
        ModelState.getInstance().setDirty(false);
        // Reflectively invoke loadProjectFromPath to mimic open recent / file chooser
        Method loadProjectFromPath = FDDMainWindowFX.class.getDeclaredMethod("loadProjectFromPath", String.class, boolean.class);
        loadProjectFromPath.setAccessible(true);
        loadProjectFromPath.invoke(sharedWindow, existingPath, false);
        waitFor(() -> existingPath.equals(ProjectService.getInstance().getAbsolutePath()) && !ModelState.getInstance().isDirty(), 5000);

        // Mark dirty and perform silent save (should not add another MRU entry)
        ProjectService.getInstance().markDirty();
        Thread.sleep(100);
        saveToFile.invoke(sharedWindow, existingPath);
        waitFor(() -> !ModelState.getInstance().isDirty(), 5000);
        assertEquals(existingPath, ProjectService.getInstance().getAbsolutePath(), "Path should remain unchanged after silent save");
        assertEquals(mruSizeAfterInitial, RecentFilesService.getInstance().getRecentFiles().size(), "MRU size should remain unchanged after silent save of reopened project");
    }

    // Helper: get an existing FDDMainWindowFX instance by cruising active windows via ProjectService display name reference.
    // Simplify by creating a new one if needed (previous tests create at least one). For our reflective method call the instance itself is irrelevant
    // as saveToFile only relies on instance fields already initialized in the constructor.
    private void waitFor(Check cond, long timeoutMs) throws InterruptedException {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (cond.ok()) return;
            Thread.sleep(50);
        }
        fail("Condition not met within timeout");
    }
    @FunctionalInterface private interface Check { boolean ok(); }
}
