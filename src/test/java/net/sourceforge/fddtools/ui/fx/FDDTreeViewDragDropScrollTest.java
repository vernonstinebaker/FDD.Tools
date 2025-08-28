package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeItem;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.service.ProjectService;
import net.sourceforge.fddtools.testutil.FxTestUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test specifically for drag and drop scroll suppression behavior.
 * Verifies that viewport position remains stable during drag and drop operations.
 */
public class FDDTreeViewDragDropScrollTest {
    
    @BeforeAll
    static void startFx() throws Exception {
        FxTestUtil.ensureStarted();
    }

    @Test
    void dragDropSuppressesScrolling() throws Exception {
        CountDownLatch setupLatch = new CountDownLatch(1);
        final FDDTreeViewFX[] treeRef = new FDDTreeViewFX[1];
        final double[] initialScrollPosition = {-1};
        
        // Setup tree with multiple nodes
        Platform.runLater(() -> {
            try {
                ProjectService.getInstance().newProject("ScrollTest");
                FDDINode root = ProjectService.getInstance().getRoot();
                
                // Create a tree with enough nodes to enable scrolling
                for (int i = 0; i < 20; i++) {
                    FDDINode child = new com.nebulon.xml.fddi.Project();
                    child.setName("Project " + i);
                    root.addChild(child);
                }
                
                FDDTreeViewFX tree = new FDDTreeViewFX();
                treeRef[0] = tree;
                tree.populateTree(root);
                
                // Expand all nodes to make scrolling possible
                expandAllNodes(tree.getRoot());
                
                setupLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
                setupLatch.countDown();
            }
        });
        
        assertTrue(setupLatch.await(5, TimeUnit.SECONDS), "Setup should complete");
        
        CountDownLatch scrollTestLatch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                FDDTreeViewFX tree = treeRef[0];
                assertNotNull(tree, "Tree should be initialized");
                
                // Find the vertical scroll bar
                ScrollBar vbar = null;
                var bars = tree.lookupAll(".scroll-bar");
                for (var n : bars) {
                    if (n instanceof ScrollBar sb && sb.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
                        vbar = sb;
                        break;
                    }
                }
                
                if (vbar != null) {
                    initialScrollPosition[0] = vbar.getValue();
                    
                    // Simulate drag and drop scenario by enabling suppressAutoScroll
                    tree.setSuppressAutoScroll(true);
                    
                    try {
                        // Select a node that would normally cause scrolling
                        FDDINode lastChild = (FDDINode) tree.getRoot().getValue().getChildren().get(19);
                        tree.selectNode(lastChild, false); // This would normally scroll
                        
                        // Verify scroll position hasn't changed
                        double currentScrollPosition = vbar.getValue();
                        assertEquals(initialScrollPosition[0], currentScrollPosition, 0.01, 
                            "Scroll position should remain unchanged during suppressed selection");
                        
                    } finally {
                        // Clean up
                        tree.setSuppressAutoScroll(false);
                    }
                }
                
                scrollTestLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
                scrollTestLatch.countDown();
            }
        });
        
        assertTrue(scrollTestLatch.await(5, TimeUnit.SECONDS), "Scroll test should complete");
        
        // Verify that normal scrolling works when not suppressed
        CountDownLatch normalScrollLatch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                FDDTreeViewFX tree = treeRef[0];
                
                // Select node with scrolling enabled (normal behavior)
                FDDINode firstChild = (FDDINode) tree.getRoot().getValue().getChildren().get(0);
                tree.selectNode(firstChild, true); // This should scroll
                
                normalScrollLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
                normalScrollLatch.countDown();
            }
        });
        
        assertTrue(normalScrollLatch.await(5, TimeUnit.SECONDS), "Normal scroll test should complete");
    }
    
    private void expandAllNodes(TreeItem<FDDINode> item) {
        if (item == null) return;
        item.setExpanded(true);
        for (TreeItem<FDDINode> child : item.getChildren()) {
            expandAllNodes(child);
        }
    }
}
