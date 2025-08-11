package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.Subject;
import net.sourceforge.fddtools.ui.fx.dialog.DialogValidation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Focused tests for core dialog validation logic without launching dialogs. */
public class DialogValidationTest {

    private static class DummySubject extends Subject { public DummySubject(){ setName("Subject A"); setPrefix("SUB"); } }
    private static class DummyActivity extends Activity { public DummyActivity(){ setName("Activity A"); setInitials("AA"); } }
    private static class DummyFeature extends Feature { public DummyFeature(){ setName("Feature A"); setInitials("FA"); } }

    @Test
    void emptyNameFails() {
        DummySubject subj = new DummySubject();
        List<String> errors = DialogValidation.validate(subj, "   ", "", "SUB");
        assertFalse(errors.isEmpty(), "Empty name should produce validation errors");
    }

    @Test
    void validSubjectPasses() {
        DummySubject subj = new DummySubject();
        List<String> errors = DialogValidation.validate(subj, "Subject B", "", "SB");
        assertTrue(errors.isEmpty(), "Valid subject should have no errors");
    }

    @Test
    void activityOwnerRequired() {
        DummyActivity act = new DummyActivity();
        List<String> errors = DialogValidation.validate(act, "Activity B", "   ", "");
        assertFalse(errors.isEmpty(), "Activity without owner should fail");
    }

    @Test
    void featureOwnerRequired() {
        DummyFeature feat = new DummyFeature();
        List<String> errors = DialogValidation.validate(feat, "Feature B", "   ", "");
        assertFalse(errors.isEmpty(), "Feature without owner should fail");
    }
}
