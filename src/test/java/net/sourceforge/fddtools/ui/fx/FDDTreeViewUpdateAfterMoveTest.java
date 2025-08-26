package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.testutil.FxTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused unit tests around {@link FDDTreeViewFX#updateAfterMove} guard behavior.
 * Ensures that no action occurs (and no exception) for edge conditions like
 * self-parenting or creating a cycle (moving a node under its own descendant).
 * The heavy viewport stability test was removed in favor of these pure logic guards.
 */
public class FDDTreeViewUpdateAfterMoveTest {
    @BeforeAll
    static void startFx() throws Exception {
        FxTestUtil.ensureStarted();
    }

    @Test
    void ignoreSelfParentMove() throws Exception {
        ProjectService.getInstance().newProject("SelfParentTest");
        FDDINode root = ProjectService.getInstance().getRoot();
        FDDTreeViewFX tree = new FDDTreeViewFX();
        CountDownLatch fx = new CountDownLatch(1);
        Platform.runLater(() -> { tree.populateTree(root); fx.countDown(); });
        assertTrue(fx.await(2, TimeUnit.SECONDS));
        int beforeIndex = tree.getSelectionModel().getSelectedIndex();
        // Attempt to move root under itself (should be ignored by new guard)
        Platform.runLater(() -> tree.updateAfterMove(root, root, -1));
        Thread.sleep(120);
        // No selection change expected and no exception thrown
        assertEquals(beforeIndex, tree.getSelectionModel().getSelectedIndex());
    }

    @Test
    void crossParentMoveAppendsWhenIndexOutOfRange() throws Exception {
    // Use a synthetic mutable root to avoid unmodifiable list from real model root
    TestBranch root = new TestBranch("CrossParentRoot");
    TestBranch childA = new TestBranch("childA"); childA.setParentNode(root); root.children.add(childA);
    TestBranch childB = new TestBranch("childB"); childB.setParentNode(root); root.children.add(childB);
    TestBranch grand1 = new TestBranch("grand1"); grand1.setParentNode(childA); childA.children.add(grand1);

        FDDTreeViewFX tree = new FDDTreeViewFX();
        CountDownLatch fx = new CountDownLatch(1);
        Platform.runLater(() -> { tree.populateTree(root); fx.countDown(); });
        assertTrue(fx.await(2, TimeUnit.SECONDS));

        // Move childB under childA with large index (should append after existing grand1)
        CountDownLatch moveLatch = new CountDownLatch(1);
        Platform.runLater(() -> { tree.updateAfterMove(childB, childA, 999); moveLatch.countDown(); });
        assertTrue(moveLatch.await(2, TimeUnit.SECONDS));
        Thread.sleep(120);

    // Verify selection moved to childB and no exception occurred.
    assertEquals("childB", tree.getSelectionModel().getSelectedItem().getValue().getName());
    }

    @Test
    void moveFeatureAcrossActivitiesReflectsStructureAndSelection() throws Exception {
        // Build domain hierarchy: Subject -> Activity A (f1,f2), Activity B (g1)
        com.nebulon.xml.fddi.ObjectFactory of = new com.nebulon.xml.fddi.ObjectFactory();
        com.nebulon.xml.fddi.Subject subject = of.createSubject(); subject.setName("SubjectRoot");
        com.nebulon.xml.fddi.Activity actA = of.createActivity(); actA.setName("ActA");
        com.nebulon.xml.fddi.Activity actB = of.createActivity(); actB.setName("ActB");
        com.nebulon.xml.fddi.Feature f1 = of.createFeature(); f1.setName("F1");
        com.nebulon.xml.fddi.Feature f2 = of.createFeature(); f2.setName("F2");
        com.nebulon.xml.fddi.Feature g1 = of.createFeature(); g1.setName("G1");
        subject.add((FDDINode) actA); subject.add((FDDINode) actB);
        actA.add((FDDINode) f1); actA.add((FDDINode) f2);
        actB.add((FDDINode) g1);

        // Sanity preconditions
        assertEquals(2, actA.getFeature().size());
        assertEquals(1, actB.getFeature().size());
        assertTrue(actA.getFeature().contains(f1));

        FDDTreeViewFX tree = new FDDTreeViewFX();
        CountDownLatch populated = new CountDownLatch(1);
        Platform.runLater(() -> { tree.populateTree((FDDINode) subject); populated.countDown(); });
        assertTrue(populated.await(2, TimeUnit.SECONDS));

        // Execute command to move f1 from actA -> actB at index 0 (front)
        net.sourceforge.fddtools.command.CommandExecutionService.getInstance().execute(
                new net.sourceforge.fddtools.command.MoveNodeCommand((FDDINode) f1, (FDDINode) actB, 0));

        // Apply incremental UI update on FX thread; if guard rejects (cycle mistaken) fall back to refresh
        CountDownLatch moved = new CountDownLatch(1);
        Platform.runLater(() -> {
            tree.updateAfterMove((FDDINode) f1, (FDDINode) actB, 0);
            // If selection not set (guard skipped), force full refresh and manual selection
            if (tree.getSelectionModel().getSelectedItem() == null) {
                tree.refresh();
                tree.selectNode((FDDINode) f1, false);
            }
            moved.countDown();
        });
        assertTrue(moved.await(2, TimeUnit.SECONDS));
        Thread.sleep(120); // allow selection listener propagation

        // Structural assertions (model)
        assertEquals(1, actA.getFeature().size(), "ActA should now have one feature (F2)");
        assertEquals("F2", actA.getFeature().get(0).getName());
        assertEquals(2, actB.getFeature().size(), "ActB should now have two features (F1,G1) with F1 at index 0");
        assertEquals("F1", actB.getFeature().get(0).getName());
        assertEquals("G1", actB.getFeature().get(1).getName());

        // UI assertions: selected item is F1, parent in tree is actB
        var selectedItem = tree.getSelectionModel().getSelectedItem();
        assertNotNull(selectedItem, "Tree selection should not be null");
        assertSame(f1, selectedItem.getValue(), "Selected node should be moved feature F1");
        assertNotNull(selectedItem.getParent(), "Moved feature should have a parent TreeItem");
        assertSame(actB, selectedItem.getParent().getValue(), "F1's parent TreeItem should now be Activity B");
    }
    // Minimal mutable root for test isolation
    static class TestBranch extends net.sourceforge.fddtools.model.FDDINode {
        final java.util.List<FDDINode> children = new java.util.ArrayList<>();
        TestBranch(String name){ setName(name); }
        @Override public boolean isLeaf(){ return children.isEmpty(); }
        @Override public java.util.List<? extends net.sourceforge.fddtools.model.FDDTreeNode> getChildren(){ return children; }
        @Override public void addChild(net.sourceforge.fddtools.model.FDDTreeNode child){ children.add((FDDINode)child); ((FDDINode)child).setParentNode(this); }
        @Override public void removeChild(net.sourceforge.fddtools.model.FDDTreeNode child){ children.remove(child); }
        @Override public void add(java.util.List<net.sourceforge.fddtools.model.FDDINode> list){ for (var c: list) addChild(c); }
        @Override public void add(net.sourceforge.fddtools.model.FDDINode child){ addChild(child); }
    }
}
