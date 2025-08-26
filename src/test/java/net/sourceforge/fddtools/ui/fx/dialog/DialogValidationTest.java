package net.sourceforge.fddtools.ui.fx.dialog;

import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Subject;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/** Comprehensive tests for core dialog validation logic without launching dialogs. */
public class DialogValidationTest {
    private final ObjectFactory of = new ObjectFactory();

    // Helper classes for testing specific validations
    private static class DummySubject extends Subject { 
        public DummySubject(){ setName("Subject A"); setPrefix("SUB"); } 
    }
    private static class DummyActivity extends Activity { 
        public DummyActivity(){ setName("Activity A"); setInitials("AA"); } 
    }
    private static class DummyFeature extends Feature { 
        public DummyFeature(){ setName("Feature A"); setInitials("FA"); } 
    }

    @Test
    void requiresNameAlways() {
        FDDINode feature = (FDDINode) of.createFeature();
        List<DialogValidation.Field> errors = DialogValidation.validate(feature, "", "AB", "P");
        assertTrue(errors.contains(DialogValidation.Field.NAME));
    }

    @Test
    void emptyNameFails() {
        DummySubject subj = new DummySubject();
        List<DialogValidation.Field> errors = DialogValidation.validate(subj, "   ", "", "SUB");
        assertTrue(errors.contains(DialogValidation.Field.NAME), "Empty name should produce NAME error");
    }

    @Test
    void validSubjectPasses() {
        DummySubject subj = new DummySubject();
        List<DialogValidation.Field> errors = DialogValidation.validate(subj, "Subject B", "", "SUB");
        assertTrue(errors.isEmpty(), "Valid subject should have no errors");
    }

    @Test
    void ownerOptionalForFeature() {
        FDDINode feature = (FDDINode) of.createFeature();
        List<DialogValidation.Field> errors = DialogValidation.validate(feature, "Feature 1", "", "P");
        assertFalse(errors.stream().anyMatch(f -> f.name().equals("OWNER"))); // OWNER removed
    }

    @Test
    void ownerOptionalForActivity() {
        DummyActivity act = new DummyActivity();
        List<DialogValidation.Field> errors = DialogValidation.validate(act, "Activity B", "   ", "");
        assertFalse(errors.stream().anyMatch(f -> f.name().equals("OWNER"))); // OWNER removed
    }

    @Test
    void ownerOptionalForFeatureEmpty() {
        DummyFeature feat = new DummyFeature();
        List<DialogValidation.Field> errors = DialogValidation.validate(feat, "Feature B", "   ", "");
        assertFalse(errors.stream().anyMatch(f -> f.name().equals("OWNER"))); // OWNER removed
    }

    @Test
    void prefixRequiredForSubject() {
        FDDINode subject = (FDDINode) of.createSubject();
        List<DialogValidation.Field> errors = DialogValidation.validate(subject, "Subject 1", "AB", "");
        assertTrue(errors.contains(DialogValidation.Field.PREFIX));
    }
}
