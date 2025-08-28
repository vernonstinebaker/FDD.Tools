package net.sourceforge.fddtools.ui.fx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import javafx.scene.control.ScrollBar;
import javafx.application.Platform;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.testutil.FxTestUtil;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Subject;
import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TreeView scroll behavior during drag and drop operations.
 * Ensures that:
 * 1. Normal drag/drop within visible area doesn't cause unwanted scrolling
 * 2. Scroll suppression mechanisms work correctly
 * 3. Cycle detection prevents invalid moves
 */
public class FDDTreeViewScrollBehaviorTest {
    
    @BeforeAll
    static void startFx() throws Exception {
        FxTestUtil.ensureStarted();
    }
    
    private FDDINode createLargeTestHierarchy() {
        Program program = new Program();
        program.setName("Test Program");
        
        // Create enough projects to force scrolling
        for (int i = 0; i < 8; i++) {
            Project project = new Project();
            project.setName("Project " + i);
            program.addChild(project);
            
            // Add aspects to each project
            for (int j = 0; j < 2; j++) {
                Aspect aspect = new Aspect();
                aspect.setName("Aspect " + i + "-" + j);
                project.addChild(aspect);
                
                // Add subjects to each aspect
                for (int k = 0; k < 2; k++) {
                    Subject subject = new Subject();
                    subject.setName("Subject " + i + "-" + j + "-" + k);
                    aspect.addChild(subject);
                    
                    // Add activities to each subject
                    for (int l = 0; l < 2; l++) {
                        Activity activity = new Activity();
                        activity.setName("Activity " + i + "-" + j + "-" + k + "-" + l);
                        subject.addChild(activity);
                        
                        // Add features to each activity
                        Feature feature = new Feature();
                        feature.setName("Feature " + i + "-" + j + "-" + k + "-" + l + "-0");
                        activity.addChild(feature);
                    }
                }
            }
        }
        
        return program;
    }
    
    @Test
    @DisplayName("Normal drag/drop within visible area should not cause scrolling")
    public void testNormalDragDropNoScrolling() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final FDDTreeViewFX[] treeRef = new FDDTreeViewFX[1];
        final ScrollBar[] scrollBarRef = new ScrollBar[1];
        final double[] initialScrollPosition = {-1};
        
        Platform.runLater(() -> {
            try {
                // Create tree with test hierarchy
                FDDINode rootNode = createLargeTestHierarchy();
                FDDTreeViewFX treeView = new FDDTreeViewFX();
                treeRef[0] = treeView;
                treeView.populateTree(rootNode);
                
                // Find scroll bar
                Platform.runLater(() -> {
                    var scrollBars = treeView.lookupAll(".scroll-bar");
                    for (var node : scrollBars) {
                        if (node instanceof ScrollBar sb && sb.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
                            scrollBarRef[0] = sb;
                            initialScrollPosition[0] = sb.getValue();
                            break;
                        }
                    }
                    
                    // Test drag/drop operation
                    FDDINode sourceNode = findNodeByName(rootNode, "Project 0");
                    FDDINode targetParent = findNodeByName(rootNode, "Project 1");
                    
                    if (sourceNode != null && targetParent != null) {
                        treeView.setSuppressAutoScroll(true);
                        treeView.updateAfterMove(sourceNode, targetParent, 0);
                        
                        // Check scroll position after operation
                        Platform.runLater(() -> {
                            if (scrollBarRef[0] != null) {
                                double finalScrollPosition = scrollBarRef[0].getValue();
                                assertEquals(initialScrollPosition[0], finalScrollPosition, 0.01, 
                                    "Scroll position should not change during normal drag/drop");
                            }
                            latch.countDown();
                        });
                    } else {
                        latch.countDown();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }
    
    @Test
    @DisplayName("Scroll suppression flag should work correctly")
    public void testScrollSuppressionFlag() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                FDDINode rootNode = createLargeTestHierarchy();
                FDDTreeViewFX treeView = new FDDTreeViewFX();
                treeView.populateTree(rootNode);
                
                // Test that suppression flag prevents scrollTo calls
                treeView.setSuppressAutoScroll(true);
                assertTrue(getSuppressionFlag(treeView), "Suppression flag should be true");
                
                // Test that disabling suppression works
                treeView.setSuppressAutoScroll(false);
                assertFalse(getSuppressionFlag(treeView), "Suppression flag should be false");
                
                latch.countDown();
                
            } catch (Exception e) {
                e.printStackTrace();
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }
    
    @Test
    @DisplayName("Cycle detection should work correctly")
    public void testCycleDetection() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                FDDINode rootNode = createLargeTestHierarchy();
                FDDTreeViewFX treeView = new FDDTreeViewFX();
                treeView.populateTree(rootNode);
                
                // Test that moving a parent under its child is prevented
                FDDINode parentNode = findNodeByName(rootNode, "Project 0");
                FDDINode childNode = findNodeByName(parentNode, "Aspect 0-0");
                
                if (parentNode != null && childNode != null) {
                    // This should be prevented by cycle detection
                    int childCountBefore = childNode.getChildren().size();
                    treeView.updateAfterMove(parentNode, childNode, 0);
                    
                    Platform.runLater(() -> {
                        int childCountAfter = childNode.getChildren().size();
                        
                        // Verify the move was prevented
                        assertEquals(childCountBefore, childCountAfter,
                            "Cycle-creating move should be prevented");
                        
                        latch.countDown();
                    });
                } else {
                    latch.countDown();
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Test should complete within 5 seconds");
    }
    
    // Helper methods
    private FDDINode findNodeByName(FDDINode parent, String name) {
        if (parent != null && name.equals(parent.getName())) {
            return parent;
        }
        
        if (parent != null) {
            for (var child : parent.getChildren()) {
                if (child instanceof FDDINode childNode) {
                    FDDINode result = findNodeByName(childNode, name);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        
        return null;
    }
    
    private boolean getSuppressionFlag(FDDTreeViewFX treeView) {
        try {
            // Use reflection to access the private suppressAutoScroll field
            var field = FDDTreeViewFX.class.getDeclaredField("suppressAutoScroll");
            field.setAccessible(true);
            return (Boolean) field.get(treeView);
        } catch (Exception e) {
            fail("Could not access suppressAutoScroll field: " + e.getMessage());
            return false;
        }
    }
}
