package net.sourceforge.fddtools.ui.fx;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.Test;

import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.Milestone;
import com.nebulon.xml.fddi.MilestoneInfo;
import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.StatusEnum;

/**
 * Unit tests for {@link FeatureMilestoneHelper}.
 */
public class FeatureMilestoneHelperTest {

    private MilestoneInfo info(String name) {
        MilestoneInfo mi = new MilestoneInfo();
        mi.setName(name);
        return mi;
    }

    @Test
    void alignMilestones_addsAndRemovesToMatchInfo() throws Exception {
        Feature feature = new Feature();
        ObjectFactory of = new ObjectFactory();
        // Pre-populate with 3 milestones
        for (int i = 0; i < 3; i++) {
            Milestone m = of.createMilestone();
            XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
            m.setPlanned(date);
            m.setStatus(StatusEnum.NOTSTARTED);
            feature.getMilestone().add(m);
        }

        List<MilestoneInfo> info = List.of(info("A"), info("B")); // Only two needed now
        FeatureMilestoneHelper.alignMilestones(feature, info, FeatureMilestoneHelper.todaySupplier());
        assertEquals(2, feature.getMilestone().size(), "Should trim to two milestones");

        // Now request 4 milestones; it should grow
        info = List.of(info("A"), info("B"), info("C"), info("D"));
        FeatureMilestoneHelper.alignMilestones(feature, info, FeatureMilestoneHelper.todaySupplier());
        assertEquals(4, feature.getMilestone().size(), "Should expand to four milestones");
        feature.getMilestone().forEach(m -> assertNotNull(m.getPlanned()));
    }

    @Test
    void applyUpdates_setsDatesStatusAndRecalculatesProgress() throws Exception {
        Feature feature = new Feature();
        List<MilestoneInfo> info = List.of(info("A"), info("B"));
        FeatureMilestoneHelper.alignMilestones(feature, info, FeatureMilestoneHelper.todaySupplier());

        // Build updates
        ArrayList<FeatureMilestoneHelper.MilestoneUpdate> updates = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            FeatureMilestoneHelper.MilestoneUpdate u = new FeatureMilestoneHelper.MilestoneUpdate();
            GregorianCalendar cal = new GregorianCalendar();
            cal.add(GregorianCalendar.DAY_OF_MONTH, i);
            u.planned = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
            if (i == 0) {
                u.actual = u.planned;
                u.complete = true;
            }
            updates.add(u);
        }

        FeatureMilestoneHelper.applyUpdates(feature, updates);
        assertEquals(StatusEnum.COMPLETE, feature.getMilestone().get(0).getStatus());
        assertEquals(StatusEnum.NOTSTARTED, feature.getMilestone().get(1).getStatus());
    assertNotNull(feature.getTargetDate());
    assertTrue(feature.getProgress().getCompletion() >= 0);
    }
}
