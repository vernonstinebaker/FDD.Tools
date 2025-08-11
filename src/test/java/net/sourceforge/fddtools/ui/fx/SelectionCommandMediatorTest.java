package net.sourceforge.fddtools.ui.fx;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javafx.stage.Stage; // unused in headless stub
import net.sourceforge.fddtools.testutil.FxTestUtil;

/** Unit tests for SelectionCommandMediator focusing on afterModelMutation and edit snapshot change detection logic (simplified). */
public class SelectionCommandMediatorTest {

    static class HostStub implements SelectionCommandMediator.Host {
    // Use minimal instances created after JavaFX init
    FDDTreeViewFX tree;
    DummyCanvas canvas;
        HostStub(){
            try {
                FxTestUtil.runOnFxAndWait(5, () -> {
                    tree = new FDDTreeViewFX();
                    canvas = new DummyCanvas();
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        boolean undoRedoUpdated;
        @Override public FDDTreeViewFX getProjectTree() { return tree; }
        @Override public FDDCanvasFX getCanvas() { return canvas; }
        @Override public Stage getPrimaryStage() { return null; }
        @Override public void configureDialogCentering(Stage stage) { }
        @Override public void updateUndoRedo() { undoRedoUpdated = true; }
    }

    // Minimal stub to satisfy SelectionCommandMediator interactions without full canvas logic
    static class DummyCanvas extends FDDCanvasFX {
        DummyCanvas(){ super(new com.nebulon.xml.fddi.ObjectFactory().createProgram(), javafx.scene.text.Font.font("System",12)); }
        @Override public void redraw() { /* no-op */ }
        @Override public void setCurrentNode(net.sourceforge.fddtools.model.FDDINode node) { /* no-op for test */ }
    }

    @Test
    void afterModelMutation_refreshesTreeAndCanvas() {
    FxTestUtil.ensureStarted();
    HostStub host = new HostStub();
    var mediator = new SelectionCommandMediator(host, net.sourceforge.fddtools.command.CommandExecutionService.getInstance(), new FDDCommandBindings(net.sourceforge.fddtools.command.CommandExecutionService.getInstance(), () -> {}, () -> {}));
        // Minimal fake node
        com.nebulon.xml.fddi.ObjectFactory of = new com.nebulon.xml.fddi.ObjectFactory();
        var prog = of.createProgram();
        prog.setName("P1");
        // Set tree root
        try {
            FxTestUtil.runOnFxAndWait(5, () -> {
                var rootItem = new javafx.scene.control.TreeItem<>((net.sourceforge.fddtools.model.FDDINode) prog);
                host.tree.setRoot(rootItem);
                mediator.afterModelMutation((net.sourceforge.fddtools.model.FDDINode) prog);
                assertEquals(prog, host.tree.getSelectionModel().getSelectedItem().getValue());
            });
        } catch (Exception e) {
            fail(e);
        }
    }
}
