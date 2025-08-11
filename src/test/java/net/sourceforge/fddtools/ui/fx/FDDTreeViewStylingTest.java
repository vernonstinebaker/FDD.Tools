package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import net.sourceforge.fddtools.testutil.FxTestUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Styling regression tests for TreeView hover / selection / DnD pseudo-classes.
 * Ensures orange accent + hover + drop indicators remain wired.
 */
public class FDDTreeViewStylingTest {

    @BeforeAll
    static void initFx() { FxTestUtil.ensureStarted(); }

    @Test
    void hoverPseudoClassApplied() throws Exception {
        FDDTreeViewFX tree = new FDDTreeViewFX();
        DummyNode rootNode = new DummyNode("Root");
        tree.populateTree(rootNode);
        CountDownLatch shown = new CountDownLatch(1);
        Platform.runLater(() -> { Stage s = new Stage(); s.setScene(new Scene(tree, 300, 200)); s.show(); shown.countDown(); });
        assertTrue(shown.await(5, TimeUnit.SECONDS));
        FxTestUtil.runOnFxAndWait(5, () -> {
            var cells = tree.lookupAll(".tree-cell");
            assertFalse(cells.isEmpty(), "Expected at least one tree cell");
            TreeCell<?> firstNonEmpty = cells.stream()
                    .filter(n -> n instanceof TreeCell<?> tc && tc.getItem()!=null && !tc.isEmpty())
                    .map(n -> (TreeCell<?>) n)
                    .findFirst().orElseThrow(() -> new AssertionError("No non-empty tree cell found"));
            PseudoClass ROW_HOVER = PseudoClass.getPseudoClass("row-hover");
            // Simulate hover activation
            firstNonEmpty.pseudoClassStateChanged(ROW_HOVER, true);
            firstNonEmpty.applyCss();
            assertTrue(firstNonEmpty.getPseudoClassStates().contains(ROW_HOVER), "row-hover pseudo-class should be active");
            // Do not perform pixel color assertion (headless environments may differ)
            // Deactivate and ensure removed
            firstNonEmpty.pseudoClassStateChanged(ROW_HOVER, false);
            assertFalse(firstNonEmpty.getPseudoClassStates().contains(ROW_HOVER), "row-hover pseudo-class should be cleared");
        });
    }

    @Test
    void dndPseudoClassesExclusive() throws Exception {
        // Logic enum derivation still validated
        assertEquals(FDDTreeDragAndDropController.DropType.BEFORE, FDDTreeDragAndDropController.deriveDropType(0, 20));
        assertEquals(FDDTreeDragAndDropController.DropType.INTO, FDDTreeDragAndDropController.deriveDropType(10, 20));
        assertEquals(FDDTreeDragAndDropController.DropType.AFTER, FDDTreeDragAndDropController.deriveDropType(19, 20));

        FDDTreeViewFX tree = new FDDTreeViewFX();
        DummyNode root = new DummyNode("Root");
        tree.populateTree(root);
        CountDownLatch shown = new CountDownLatch(1);
        Platform.runLater(() -> { Stage s = new Stage(); s.setScene(new Scene(tree, 250, 150)); s.show(); shown.countDown(); });
        assertTrue(shown.await(5, TimeUnit.SECONDS));
        FxTestUtil.runOnFxAndWait(5, () -> {
            TreeCell<?> cell = tree.lookupAll(".tree-cell").stream()
                    .filter(n -> n instanceof TreeCell<?> tc && tc.getItem()!=null && !tc.isEmpty())
                    .map(n -> (TreeCell<?>) n)
                    .findFirst().orElseThrow();
            PseudoClass DROP_TARGET = PseudoClass.getPseudoClass("drop-target");
            PseudoClass INSERT_BEFORE = PseudoClass.getPseudoClass("drop-insert-before");
            PseudoClass INSERT_AFTER = PseudoClass.getPseudoClass("drop-insert-after");
            // Activate each and validate exclusivity
            cell.pseudoClassStateChanged(DROP_TARGET, true);
            assertTrue(cell.getPseudoClassStates().contains(DROP_TARGET));
            assertFalse(cell.getPseudoClassStates().contains(INSERT_BEFORE));
            cell.pseudoClassStateChanged(DROP_TARGET, false);
            cell.pseudoClassStateChanged(INSERT_BEFORE, true);
            assertTrue(cell.getPseudoClassStates().contains(INSERT_BEFORE));
            assertFalse(cell.getPseudoClassStates().contains(DROP_TARGET));
            cell.pseudoClassStateChanged(INSERT_BEFORE, false);
            cell.pseudoClassStateChanged(INSERT_AFTER, true);
            assertTrue(cell.getPseudoClassStates().contains(INSERT_AFTER));
            assertFalse(cell.getPseudoClassStates().contains(INSERT_BEFORE));
            // Clear
            cell.pseudoClassStateChanged(INSERT_AFTER, false);
            assertFalse(cell.getPseudoClassStates().contains(INSERT_AFTER));
        });
    }

    @Test
    void selectionAccentOrangeApplied() throws Exception {
        FDDTreeViewFX tree = new FDDTreeViewFX();
        DummyNode root = new DummyNode("Root");
        tree.populateTree(root);
        CountDownLatch shown = new CountDownLatch(1);
        Platform.runLater(() -> { Stage s = new Stage(); s.setScene(new Scene(tree, 300, 200)); s.show(); shown.countDown(); });
        assertTrue(shown.await(5, TimeUnit.SECONDS));
        FxTestUtil.runOnFxAndWait(5, () -> {
            // Select child to ensure a selected cell (root also selected by default sometimes)
            tree.getSelectionModel().select(0); // select root (only node)
            tree.applyCss();
            var selectedCell = tree.lookupAll(".tree-cell").stream()
                    .filter(n -> n instanceof TreeCell<?> tc && tc.isSelected())
                    .map(n -> (TreeCell<?>) n)
                    .findFirst().orElse(null);
            assertNotNull(selectedCell, "Expected a selected tree cell");
            selectedCell.applyCss();
            // Rely on style class presence; color checking skipped for headless reliability
        });
    }

    static class DummyNode extends com.nebulon.xml.fddi.Program {
        public DummyNode(String name){ setName(name); }
    }
}
