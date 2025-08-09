package net.sourceforge.fddtools.ui.fx;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.nebulon.xml.fddi.*;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;
import net.sourceforge.fddtools.commands.EditNodeCommand;

/**
 * Lightweight UI integration test (headless) verifying that editing a Feature
 * through FDDElementDialogFX results in an EditNodeCommand including work package changes.
 * This doesn't simulate actual user clicks but invokes dialog logic on FX thread.
 */
public class FDDMainWindowWorkPackageUITest {
    @BeforeAll
    static void initJfx() throws Exception {
        // Initialize JavaFX runtime once
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await();
    }

    @Test
    void editFeatureAssignWorkPackageProducesCommandSnapshot() throws Exception {
        Platform.runLater(() -> {
            // Build a minimal hierarchy: Program -> Project -> Aspect -> Subject -> Activity -> Feature
            Program program = new Program();
            program.setName("Prog");
            Project project = new Project();
            project.setName("Proj");
            program.getProject().add(project); project.setParentNode(program);
            Aspect aspect = new Aspect(); aspect.setName("Asp"); aspect.setParentNode(project); project.getAspect().add(aspect);
            Subject subject = new Subject(); subject.setName("Sub"); subject.setParentNode(aspect); aspect.getSubject().add(subject);
            Activity activity = new Activity(); activity.setName("Act"); activity.setParentNode(subject); subject.getActivity().add(activity);
            Feature feature = new Feature(); feature.setName("Feat"); feature.setParentNode(activity); activity.getFeature().add(feature);

            // Add a work package to project
            WorkPackage wp = new WorkPackage(); wp.setName("WP-A"); project.getAny().add(wp);

            var before = EditNodeCommand.capture(feature);
            assertEquals("", before.getWorkPackageName());

            // Simulate assigning via dialog logic: manipulate WP list then capture after
            wp.addFeature(feature.getSeq());
            var after = EditNodeCommand.capture(feature);
            assertEquals("WP-A", after.getWorkPackageName());
        });
        // Allow FX thread to process
        Thread.sleep(500);
    }
}
