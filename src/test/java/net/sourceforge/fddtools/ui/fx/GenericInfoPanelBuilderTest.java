package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import net.sourceforge.fddtools.testutil.JavaFXTestHarness;

/** Tests GenericInfoPanelBuilder field population for different node types. */
public class GenericInfoPanelBuilderTest {

    @Test
    void buildsPanelsWithCorrectFieldsPerType() {
        JavaFXTestHarness.init();
        JavaFXTestHarness.runAndWait(() -> {
            // Subject: expects prefix field, no owner field
            Subject subject = new Subject(); subject.setName("SubName"); subject.setPrefix("PX");
            GenericInfoPanelBuilder.Result subjRes = GenericInfoPanelBuilder.build(subject);
            assertNotNull(subjRes.prefixField);
            assertEquals("PX", subjRes.prefixField.getText());
            assertNull(subjRes.ownerField);
            assertEquals("SubName", subjRes.nameField.getText());

            // Activity: expects owner field, no prefix field
            Activity activity = new Activity(); activity.setName("ActName"); activity.setInitials("AL");
            GenericInfoPanelBuilder.Result actRes = GenericInfoPanelBuilder.build(activity);
            assertNull(actRes.prefixField);
            assertNotNull(actRes.ownerField);
            assertEquals("AL", actRes.ownerField.getText());
            assertEquals("ActName", actRes.nameField.getText());

            // Feature: expects owner field, no prefix field
            Feature feature = new Feature(); feature.setName("Feat"); feature.setInitials("FT");
            GenericInfoPanelBuilder.Result featRes = GenericInfoPanelBuilder.build(feature);
            assertNull(featRes.prefixField);
            assertNotNull(featRes.ownerField);
            assertEquals("FT", featRes.ownerField.getText());
            assertEquals("Feat", featRes.nameField.getText());

            // Program: only name field
            Program program = new Program(); program.setName("Prog");
            GenericInfoPanelBuilder.Result progRes = GenericInfoPanelBuilder.build(program);
            assertNull(progRes.prefixField);
            assertNull(progRes.ownerField);
            assertEquals("Prog", progRes.nameField.getText());
        });
    }
}
