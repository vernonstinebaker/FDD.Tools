package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Subject;
import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;
import net.sourceforge.fddtools.model.FDDINode;

/**
 * Ensures that rebuilding UI for an existing in-memory root populates tree and canvas.
 * Does not simulate file chooser; uses helper methods indirectly via reflection if needed.
 */
public class FDDMainWindowOpenProjectUITest {
    @BeforeAll
    static void initJfx() throws Exception {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        try { Platform.startup(latch::countDown); } catch (IllegalStateException already) { latch.countDown(); }
        latch.await();
    }

    @Test
    void rebuildProjectUIProducesTreeAndCanvas() throws Exception {
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.runLater(() -> {
            // Build minimal hierarchy Program->Project->Aspect
            Program program = new Program(); program.setName("X");
            Project project = new Project(); project.setName("P"); project.setParentNode(program); program.getProject().add(project);
            Aspect aspect = new Aspect(); aspect.setName("A"); aspect.setParentNode(project); project.getAspect().add(aspect);
            Subject subject = new Subject(); subject.setName("S"); subject.setParentNode(aspect); aspect.getSubject().add(subject);
            Activity activity = new Activity(); activity.setName("Act"); activity.setParentNode(subject); subject.getActivity().add(activity);
            Feature feature = new Feature(); feature.setName("F"); feature.setParentNode(activity); activity.getFeature().add(feature);
            FDDMainWindowFX win = new FDDMainWindowFX(new javafx.stage.Stage());
            // Use reflection to call helper rebuildProjectUI since it's private
            try {
                var m = FDDMainWindowFX.class.getDeclaredMethod("rebuildProjectUI", FDDINode.class, boolean.class);
                m.setAccessible(true);
                m.invoke(win, (FDDINode) program, false);
            } catch (Exception e) { fail("Reflection invocation failed: " + e.getMessage()); }
            assertNotNull(win.getProjectTree(), "Tree should be created");
            assertNotNull(win.getCanvas(), "Canvas should be created");
            latch.countDown();
        });
        latch.await();
    }
}
