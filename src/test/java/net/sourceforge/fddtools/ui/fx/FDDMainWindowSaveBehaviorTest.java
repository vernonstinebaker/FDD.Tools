package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.util.FileNameUtil;
import net.sourceforge.fddtools.testutil.FxTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.sourceforge.fddtools.service.PreferencesService;
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
        // Use FxTestUtil instead of manual Platform.startup()
        FxTestUtil.ensureStarted();
    }

    @BeforeEach
    void reset() throws Exception {
        PreferencesService.getInstance().clearRecentFiles();
        ModelState.getInstance().setDirty(false);
        // Create a fresh window instance on FX thread for each test
        FxTestUtil.runOnFxAndWait(5, () -> {
            sharedWindow = new FDDMainWindowFX(new javafx.stage.Stage());
        });
    }

    @Test
    void saveMenuShouldBeEnabledForDirtyUnsavedProject() throws Exception {
        FxTestUtil.runOnFxAndWait(5, () -> {
            new FDDMainWindowFX(new javafx.stage.Stage());
            // New project created in constructor; now mark dirty and wait for flag
            ProjectService.getInstance().markDirty();
        });
        
        // Small delay to allow Platform.runLater operations to complete
        Thread.sleep(200);
        assertTrue(ProjectService.getInstance().hasProjectProperty().get(), "Project should exist");
        assertNull(ProjectService.getInstance().getAbsolutePath(), "Unsaved path should be null");
        assertTrue(ModelState.getInstance().isDirty(), "Project should be dirty");
        boolean predicate = ProjectService.getInstance().hasProjectProperty().get() && ModelState.getInstance().isDirty();
        assertTrue(predicate, "Logical predicate for Save enablement should be true");
    }

    @Test
    void buildDefaultSaveFileNameStripsDuplicateExtensions() throws Exception {
        assertEquals("New Program", FileNameUtil.buildDefaultSaveFileName(null));
        assertEquals("New Program", FileNameUtil.buildDefaultSaveFileName("New Program"));
        assertEquals("Example", FileNameUtil.buildDefaultSaveFileName("Example"));
        assertEquals("Example", FileNameUtil.buildDefaultSaveFileName("Example.fddi"));
        assertEquals("Example", FileNameUtil.buildDefaultSaveFileName("Example.fddi.fddi"));
    }

    @Test
    void stripDuplicateFddiCollapsesChain() throws Exception {
        assertEquals("/tmp/test.fddi", FileNameUtil.stripDuplicateFddi("/tmp/test.fddi"));
        assertEquals("/tmp/test.fddi", FileNameUtil.stripDuplicateFddi("/tmp/test.fddi.fddi"));
        assertEquals("/tmp/test.fddi", FileNameUtil.stripDuplicateFddi("/tmp/test.fddi.fddi.fddi"));
    }

    @Test
    void ensureExtensionAddsWhenMissing() throws Exception {
        assertEquals("/tmp/test.fddi", FileNameUtil.ensureFddiOrXmlExtension("/tmp/test"));
        assertEquals("/tmp/test.fddi", FileNameUtil.ensureFddiOrXmlExtension("/tmp/test.fddi"));
        assertEquals("/tmp/test.xml", FileNameUtil.ensureFddiOrXmlExtension("/tmp/test.xml"));
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
        PreferencesService.getInstance().clearRecentFiles();
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
        assertFalse(PreferencesService.getInstance().getRecentFiles().isEmpty());
        assertEquals(pathA, PreferencesService.getInstance().getRecentFiles().get(0));
        int sizeAfterFirst = PreferencesService.getInstance().getRecentFiles().size();

        // Mark dirty and perform silent save (same path) - MRU should NOT grow or duplicate
        ProjectService.getInstance().markDirty();
        Thread.sleep(80);
    saveToFile.invoke(sharedWindow, pathA);
        waitFor(() -> !ModelState.getInstance().isDirty(), 5000);
        assertEquals(sizeAfterFirst, PreferencesService.getInstance().getRecentFiles().size(), "MRU size should not change after silent save");
        assertEquals(pathA, PreferencesService.getInstance().getRecentFiles().get(0));
    }

    @Test
    void multipleSaveAsOperationsUpdateRecentOrdering() throws Exception {
        PreferencesService.getInstance().clearRecentFiles();
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
        var recents = PreferencesService.getInstance().getRecentFiles();
        assertTrue(recents.size() >= 2, "At least two recent entries expected");
        assertEquals(pathB, recents.get(0), "Most recent should be new path");
        assertEquals(pathA, recents.get(1), "Previous path should shift to second");
    }

    @Test
    void openingExistingProjectThenSaveIsSilentAndDoesNotDuplicateMRU() throws Exception {
        PreferencesService.getInstance().clearRecentFiles();
        Method saveToFile = FDDMainWindowFX.class.getDeclaredMethod("saveToFile", String.class);
        saveToFile.setAccessible(true);
        // Initial Save As to create a real file on disk
        ProjectService.getInstance().markDirty();
        Thread.sleep(120);
        String existingPath = Files.createTempFile("fdd-open-silent", ".fddi").toString();
        saveToFile.invoke(sharedWindow, existingPath);
        waitFor(() -> existingPath.equals(ProjectService.getInstance().getAbsolutePath()) && !ModelState.getInstance().isDirty(), 5000);
        int mruSizeAfterInitial = PreferencesService.getInstance().getRecentFiles().size();
        assertTrue(mruSizeAfterInitial >= 1, "MRU should contain initial path");

        // Simulate closing and reopening the project (like user selecting Open)
        PreferencesService.getInstance().clearRecentFiles();
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
        assertEquals(mruSizeAfterInitial, PreferencesService.getInstance().getRecentFiles().size(), "MRU size should remain unchanged after silent save of reopened project");
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
