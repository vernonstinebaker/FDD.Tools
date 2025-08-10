package net.sourceforge.fddtools.ui.fx.dialog;

import com.nebulon.xml.fddi.ObjectFactory;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class DialogValidationTest {
    private final ObjectFactory of = new ObjectFactory();

    @Test
    void requiresNameAlways() {
        FDDINode feature = (FDDINode) of.createFeature();
        List<String> errors = DialogValidation.validate(feature, "", "AB", "P");
        assertTrue(errors.stream().anyMatch(k -> k.contains("Name")));
    }

    @Test
    void requiresOwnerForFeature() {
        FDDINode feature = (FDDINode) of.createFeature();
        List<String> errors = DialogValidation.validate(feature, "Feature 1", "", "P");
        assertTrue(errors.stream().anyMatch(k -> k.contains("Owner") || k.contains("OwnerRequired")));
    }

    @Test
    void requiresPrefixForSubject() {
        FDDINode subject = (FDDINode) of.createSubject();
        List<String> errors = DialogValidation.validate(subject, "Subject 1", "AB", "");
        assertTrue(errors.stream().anyMatch(k -> k.contains("Prefix")));
    }
}
