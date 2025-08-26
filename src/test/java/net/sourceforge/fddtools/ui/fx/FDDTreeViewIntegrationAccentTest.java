package net.sourceforge.fddtools.ui.fx;

import javafx.stage.Stage;
import javafx.scene.Scene;
import net.sourceforge.fddtools.testutil.FxTestUtil;
import net.sourceforge.fddtools.testutil.HeadlessTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Integration test ensuring semantic-theme.css is injected and orange selection class present. */
public class FDDTreeViewIntegrationAccentTest {
    @BeforeAll static void startFx(){ FxTestUtil.ensureStarted(); }

    @Test
    void semanticStylesheetAndAccentPresent() throws Exception {
        FDDTreeViewFX tree = new FDDTreeViewFX();
        var root = new FDDTreeViewStylingTest.DummyNode("Root");
        tree.populateTree(root);
        FxTestUtil.runOnFxAndWait(5, () -> {
            Stage stage = new Stage();
            Scene scene = new Scene(tree, 200, 200);
            stage.setScene(scene); 
            HeadlessTestUtil.showStageIfNotHeadless(stage);
        });
        FxTestUtil.runOnFxAndWait(5, () -> {
            var scene = tree.getScene();
            assertNotNull(scene, "Scene should be attached");
            assertTrue(scene.getStylesheets().stream().anyMatch(s -> s.contains("semantic-theme.css")), "semantic-theme.css missing");
            assertTrue(tree.getStyleClass().contains("selection-accent-orange"), "Tree missing selection-accent-orange class");
        });
    }
}
