package net.sourceforge.fddtools.ui.fx;

import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.Milestone;
import com.nebulon.xml.fddi.MilestoneInfo;
import com.nebulon.xml.fddi.StatusEnum;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.function.Supplier;

/**
 * Extracted reusable milestone alignment & update logic from FDDElementDialogFX
 * to enable unit testing without JavaFX UI dependencies.
 */
public final class FeatureMilestoneHelper {
    private FeatureMilestoneHelper() {}

    /** Represents an updated milestone row coming from the dialog form. */
    public static final class MilestoneUpdate {
        public XMLGregorianCalendar planned;
        public XMLGregorianCalendar actual; // nullable
        public boolean complete;
    }

    /** Align feature.milestone list length and default fields to milestoneInfo size. */
    public static void alignMilestones(Feature feature, List<MilestoneInfo> infos, Supplier<XMLGregorianCalendar> dateSupplier) {
        if (infos == null) return; // nothing to align
        // Trim extras
        while (feature.getMilestone().size() > infos.size()) {
            feature.getMilestone().remove(feature.getMilestone().size() - 1);
        }
        // Add missing
        while (feature.getMilestone().size() < infos.size()) {
            Milestone m = new com.nebulon.xml.fddi.ObjectFactory().createMilestone();
            m.setPlanned(safeDate(dateSupplier));
            m.setStatus(StatusEnum.NOTSTARTED);
            feature.getMilestone().add(m);
        }
        // Ensure defaults
        for (Milestone m : feature.getMilestone()) {
            if (m.getPlanned() == null) m.setPlanned(safeDate(dateSupplier));
            if (m.getStatus() == null) m.setStatus(StatusEnum.NOTSTARTED);
        }
    }

    /** Apply updates captured from form controls back into feature milestones (assumes aligned). */
    public static void applyUpdates(Feature feature, List<MilestoneUpdate> updates) {
        if (updates == null) return;
        for (int i = 0; i < updates.size() && i < feature.getMilestone().size(); i++) {
            Milestone target = feature.getMilestone().get(i);
            MilestoneUpdate u = updates.get(i);
            if (u.planned != null) target.setPlanned(u.planned);
            target.setActual(u.actual); // may be null
            target.setStatus(u.complete ? StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
        }
        feature.calculateProgress();
        feature.calculateTargetDate();
    }

    /** Convenience supplier that returns current date xml calendar. */
    public static Supplier<XMLGregorianCalendar> todaySupplier() {
        return () -> safeDate(() -> {
            try { return DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()); }
            catch (DatatypeConfigurationException e) { return null; }
        });
    }

    private static XMLGregorianCalendar safeDate(Supplier<XMLGregorianCalendar> sup){
        try { return sup.get(); } catch(Exception e){ return null; }
    }
}
