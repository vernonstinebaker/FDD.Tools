package net.sourceforge.fddtools.ui.fx.dialog;

import net.sourceforge.fddtools.model.FDDINode;

import java.util.ArrayList;
import java.util.List;

/** Utility performing per-type dialog field validation returning message keys. */
public final class DialogValidation {
    private DialogValidation() {}

    public enum Field { NAME, PREFIX }

    public static List<Field> validate(FDDINode node, String name, String owner, String prefix){
        List<Field> invalid = new ArrayList<>();
        // XSD shows name required for Subject, Activity, Feature, Aspect, Project (token/string without minOccurs=0)
        if(name==null || name.isBlank()) invalid.add(Field.NAME);
        // Subject prefix is required per XSD (no minOccurs=0)
        if(node instanceof com.nebulon.xml.fddi.Subject){ if(prefix==null || prefix.isBlank()) invalid.add(Field.PREFIX); }
        // Owner initials optional (initials element has minOccurs=0)
        return invalid;
    }
}
