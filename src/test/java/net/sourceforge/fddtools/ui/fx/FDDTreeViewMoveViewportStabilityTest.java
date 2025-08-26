package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
// No Stage/Scene to keep headless friendly; relying on logical selection only.
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.state.ModelState;
import net.sourceforge.fddtools.testutil.FxTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test: moving a node via updateAfterMove should not force a scroll jump.
 * We simulate by constructing a reasonably tall tree then invoking updateAfterMove and ensuring
 * the previously captured selected index remains visible (we approximate by verifying index unchanged
 * and no exception). Direct ScrollBar value access is brittle in headless test; we assert selection stable.
 */
public class FDDTreeViewMoveViewportStabilityTest {
    @BeforeAll
    static void startFx() throws Exception {
        FxTestUtil.ensureStarted();
    }

    @Test
    void selectionPersistsWithoutScrollJumpOnMove() throws Exception {
        CountDownLatch fx = new CountDownLatch(1);
        final FDDTreeViewFX[] ref = new FDDTreeViewFX[1];
        Platform.runLater(() -> {
            ProjectService.getInstance().newProject("ViewportTest");
            FDDINode root = ProjectService.getInstance().getRoot();
            FDDTreeViewFX tree = new FDDTreeViewFX();
            ref[0] = tree;
            tree.populateTree(root);
            fx.countDown();
        });
        assertTrue(fx.await(3, TimeUnit.SECONDS));
        Thread.sleep(150);
        FDDTreeViewFX tree = ref[0];
        assertNotNull(tree);
        // Select root
        Platform.runLater(() -> tree.selectNode(ProjectService.getInstance().getRoot(), false));
        Thread.sleep(120);
        int beforeIndex = tree.getSelectionModel().getSelectedIndex();
        // Invoke updateAfterMove with no structural change (same parent) to mimic reordering edge case
        Platform.runLater(() -> tree.updateAfterMove(ProjectService.getInstance().getRoot(), ProjectService.getInstance().getRoot(), -1));
        Thread.sleep(120);
        int afterIndex = tree.getSelectionModel().getSelectedIndex();
        assertEquals(beforeIndex, afterIndex, "Selected index should remain stable after updateAfterMove");
        assertFalse(ModelState.getInstance().isDirty(), "Viewport operation should not change dirty state");
    }
}
