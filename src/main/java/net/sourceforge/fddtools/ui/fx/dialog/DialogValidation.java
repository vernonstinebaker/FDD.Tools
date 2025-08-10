package net.sourceforge.fddtools.ui.fx.dialog;

import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.Subject;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.internationalization.Messages;

import java.util.ArrayList;
import java.util.List;

/** Utility performing per-type dialog field validation returning message keys. */
public final class DialogValidation {
    private DialogValidation() {}

    public static List<String> validate(FDDINode node, String name, String owner, String prefix){
        List<String> errors = new ArrayList<>();
        if(name==null || name.isBlank()) errors.add(Messages.VALIDATION_NAME_REQUIRED);
        if(node instanceof Subject){
            if(prefix==null || prefix.isBlank()) errors.add(Messages.VALIDATION_PREFIX_REQUIRED);
        }
        if(node instanceof Activity || node instanceof Feature){
            if(owner==null || owner.isBlank()) errors.add(Messages.VALIDATION_OWNER_REQUIRED);
        }
        return errors;
    }
}
