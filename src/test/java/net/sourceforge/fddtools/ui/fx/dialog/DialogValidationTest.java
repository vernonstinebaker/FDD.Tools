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
        List<String> errors = DialogValidation.validate(feature, "", "AB", "P");
        assertTrue(errors.stream().anyMatch(k -> k.contains("Name")));
    }

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
    void requiresOwnerForFeature() {
        FDDINode feature = (FDDINode) of.createFeature();
        List<String> errors = DialogValidation.validate(feature, "Feature 1", "", "P");
        assertTrue(errors.stream().anyMatch(k -> k.contains("Owner") || k.contains("OwnerRequired")));
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

    @Test
    void requiresPrefixForSubject() {
        FDDINode subject = (FDDINode) of.createSubject();
        List<String> errors = DialogValidation.validate(subject, "Subject 1", "AB", "");
        assertTrue(errors.stream().anyMatch(k -> k.contains("Prefix")));
    }
}
