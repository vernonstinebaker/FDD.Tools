package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Subject;
import com.nebulon.xml.fddi.Activity;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import net.sourceforge.fddtools.testutil.JavaFXTestHarness;

/** Tests for FeatureWorkPackageHelper logic without full UI dialog. */
public class FeatureWorkPackageHelperTest {

    private Project createProjectHierarchy(Feature feature) {
        Program program = new Program(); program.setName("Prog");
        Project project = new Project(); project.setName("Proj");
        Aspect aspect = new Aspect(); aspect.setName("Asp");
        Subject subject = new Subject(); subject.setName("Sub"); subject.setPrefix("XX");
        Activity activity = new Activity(); activity.setName("Act");
        program.add(project);
        project.add(aspect);
        aspect.add(subject);
        subject.add(activity);
        activity.add(feature);
        return project;
    }

    @Test
    void initializeAndApplySelectionAssignsFeature() {
        Feature feature = new Feature(); feature.setName("F1");
        Project project = createProjectHierarchy(feature);
        // create two work packages
        WorkPackage wp1 = new WorkPackage(); wp1.setName("WP1");
        WorkPackage wp2 = new WorkPackage(); wp2.setName("WP2");
        project.getAny().add(wp1);
        project.getAny().add(wp2);

        JavaFXTestHarness.init();
        JavaFXTestHarness.runAndWait(() -> {
            javafx.scene.control.ComboBox<WorkPackage> combo = new javafx.scene.control.ComboBox<>();
            WorkPackage old = FeatureWorkPackageHelper.initializeCombo(feature, project, combo);
            assertNull(old);
            assertEquals(3, combo.getItems().size()); // unassigned + 2
            assertEquals(0, wp1.getFeatureList().size());

            combo.setValue(wp1);
            FeatureWorkPackageHelper.applySelection(feature, old, combo.getValue());
            assertTrue(wp1.getFeatureList().contains(feature.getSeq()));

            // Reassign to wp2
            WorkPackage previous = wp1;
            combo.setValue(wp2);
            FeatureWorkPackageHelper.applySelection(feature, previous, combo.getValue());
            assertFalse(wp1.getFeatureList().contains(feature.getSeq()));
            assertTrue(wp2.getFeatureList().contains(feature.getSeq()));
        });
    }
}
