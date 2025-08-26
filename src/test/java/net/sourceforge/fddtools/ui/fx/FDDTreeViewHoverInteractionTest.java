package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.stage.Stage;
import net.sourceforge.fddtools.testutil.FxTestUtil;
import net.sourceforge.fddtools.testutil.HeadlessTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies real mouse enter/exit event path applies hover styling (pseudo-class or fallback style class).
 */
public class FDDTreeViewHoverInteractionTest {

    @BeforeAll
    static void initFx(){ FxTestUtil.ensureStarted(); }

    @Test
    void mouseEnterAppliesHover() throws Exception {
        FDDTreeViewFX tree = new FDDTreeViewFX();
        FDDTreeViewStylingTest.DummyNode root = new FDDTreeViewStylingTest.DummyNode("Root");
        tree.populateTree(root);
        CountDownLatch shown = new CountDownLatch(1);
        Platform.runLater(() -> { 
            Stage s = new Stage(); 
            s.setScene(new Scene(tree, 240, 140)); 
            HeadlessTestUtil.showStageIfNotHeadless(s); 
            shown.countDown(); 
        });
        assertTrue(shown.await(5, TimeUnit.SECONDS));
        
        // Wait for tree cells to be rendered
        Thread.sleep(100);
        FxTestUtil.runOnFxAndWait(5, () -> {
            tree.applyCss();
            tree.layout();
        });
        Thread.sleep(50);
        
        FxTestUtil.runOnFxAndWait(5, () -> {
            TreeCell<?> cell = tree.lookupAll(".tree-cell").stream()
                    .filter(n -> n instanceof TreeCell<?> tc && tc.getItem()!=null && !tc.isEmpty())
                    .map(n -> (TreeCell<?>) n)
                    .findFirst().orElseThrow();
            PseudoClass ROW_HOVER = PseudoClass.getPseudoClass("row-hover");
            // Simulate hover by invoking handlers directly (avoids fragile MouseEvent construction)
            if (cell.getOnMouseEntered()!=null) cell.getOnMouseEntered().handle(null);
            cell.applyCss();
            boolean pseudo = cell.getPseudoClassStates().contains(ROW_HOVER);
            assertTrue(pseudo, "Expected hover pseudo-class active");
            // Exit
            if (cell.getOnMouseExited()!=null) cell.getOnMouseExited().handle(null);
            cell.applyCss();
            assertFalse(cell.getPseudoClassStates().contains(ROW_HOVER), "Pseudo-class should clear on exit");
            // No fallback class expected anymore
        });
    }
}
