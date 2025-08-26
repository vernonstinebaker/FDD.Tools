package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.state.ModelState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for FDDFileActions save vs saveAs semantics ensuring that
 * a subsequent save (after initial Save As establishing the path) does not
 * trigger another Save As dialog invocation.
 */
public class FDDFileActionsTest {
    static class Record { final String type; Record(String t){this.type=t;} }
    static class RecordingStrategy implements FDDFileActions.FileDialogStrategy {
        final List<Record> records = new ArrayList<>();
        File fileToReturn;
        @Override public File showSave(java.util.function.Consumer<FileChooser> config, javafx.stage.Window owner){
            records.add(new Record("save"));
            // still invoke config for coverage
            FileChooser fc = new FileChooser(); config.accept(fc); return fileToReturn;
        }
        @Override public File showOpen(java.util.function.Consumer<FileChooser> config, javafx.stage.Window owner){
            records.add(new Record("open"));
            FileChooser fc = new FileChooser(); config.accept(fc); return fileToReturn;
        }
    }

    private List<Record> invocations;
    private RecordingStrategy recordingStrategy;
    private FDDFileActions actions;

    private static class DummyHost implements FDDFileActions.Host {
        @Override public void showErrorDialog(String title, String message) { }
        @Override public void refreshRecentFilesMenu() { }
        @Override public void updateTitle() { }
        @Override public boolean canClose() { return true; }
        @Override public void loadProjectFromPath(String path, boolean rebuildUI) { }
        @Override public void rebuildProjectUI(FDDINode root, boolean markDirty) { }
        @Override public javafx.stage.Stage getPrimaryStage() { return new javafx.stage.Stage(); }
    }

    @BeforeAll
    static void initFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try { Platform.startup(latch::countDown); } catch (IllegalStateException already) { latch.countDown(); }
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @BeforeEach
    void setup() throws Exception {
    recordingStrategy = new RecordingStrategy();
    invocations = recordingStrategy.records;
    // Ensure fresh project state (use explicit name)
    ProjectService.getInstance().newProject("Test Project");
        ModelState.getInstance().setDirty(false);
    actions = new FDDFileActions(new DummyHost(), recordingStrategy);
    }

    @Test
    void firstSavePromptsDialogSecondSaveSilent() throws Exception {
        // Mark project dirty so save enabled
        ProjectService.getInstance().markDirty();
        Thread.sleep(120);
        assertTrue(ModelState.getInstance().isDirty());

        // Prepare file chooser to return a temp file path for initial saveAs
        File tmp = Files.createTempFile("fdd-save-actions-", ".fddi").toFile();
        tmp.deleteOnExit();
    recordingStrategy.fileToReturn = tmp;

        CountDownLatch first = new CountDownLatch(1);
        Platform.runLater(() -> { actions.saveProject(); first.countDown(); });
        assertTrue(first.await(2, TimeUnit.SECONDS));
        // Wait for save to complete (synchronous write but FX operations scheduled)
        Thread.sleep(150);
        assertEquals(tmp.getAbsolutePath(), ProjectService.getInstance().getAbsolutePath());
        assertFalse(ModelState.getInstance().isDirty(), "Dirty cleared after saveAs");
        assertEquals(1, invocations.size(), "One dialog invocation expected (initial Save As)");

        // Mark dirty again and perform plain save which should be silent
        ProjectService.getInstance().markDirty();
        Thread.sleep(120);
        assertTrue(ModelState.getInstance().isDirty());
        CountDownLatch second = new CountDownLatch(1);
        Platform.runLater(() -> { actions.saveProject(); second.countDown(); });
        assertTrue(second.await(2, TimeUnit.SECONDS));
        Thread.sleep(120);
        assertEquals(tmp.getAbsolutePath(), ProjectService.getInstance().getAbsolutePath());
        assertFalse(ModelState.getInstance().isDirty(), "Dirty cleared after silent save");
        assertEquals(1, invocations.size(), "No additional dialog should appear for second save");
    }
}
