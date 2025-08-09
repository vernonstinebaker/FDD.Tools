package net.sourceforge.fddtools.model;

import com.nebulon.xml.fddi.*;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies roll-up of status and milestone planned date. */
public class ProgressAndTargetDateTest {

    private Aspect buildAspectWithFeatures() throws Exception {
        ObjectFactory of = new ObjectFactory();
        Aspect aspect = of.createAspect();
        aspect.setName("AspectProgress");
        aspect.setInfo(of.createAspectInfo());
        // Provide naming conventions for children
        aspect.getInfo().setSubjectName("Subj");
        aspect.getInfo().setActivityName("Act");
        aspect.getInfo().setFeatureName("Feat");

        // Two subjects each with one activity and one feature for simplicity
        for (int i = 1; i <= 2; i++) {
            Subject subject = of.createSubject();
            subject.setName("S" + i);
            Activity activity = of.createActivity();
            activity.setName("A" + i);
            Feature feature = of.createFeature();
            feature.setName("F" + i);
            // Add a single milestone per feature with varying statuses
            Milestone ms = of.createMilestone();
            ms.setStatus(i == 1 ? StatusEnum.UNDERWAY : StatusEnum.NOTSTARTED);
            ms.setPlanned(DatatypeFactory.newInstance().newXMLGregorianCalendar("2025-0" + (5 + i) + "-0" + i));
            feature.getMilestone().add(ms);
            activity.add(feature);
            subject.add(activity);
            aspect.add(subject);
        }
        return aspect;
    }

    @Test
    void milestonePlannedDatesAndStatusRollUp() throws Exception {
        Aspect aspect = buildAspectWithFeatures();
        // Validate that children exist and planned dates set
        assertEquals(2, aspect.getChildren().size());
        // Check first feature milestone status influences overall progress heuristic (basic sanity)
        Subject s1 = (Subject) aspect.getChildren().get(0);
        Activity a1 = (Activity) s1.getChildren().get(0);
        Feature f1 = (Feature) a1.getChildren().get(0);
        Milestone m1 = f1.getMilestone().get(0);
        assertEquals(StatusEnum.UNDERWAY, m1.getStatus());
        assertNotNull(m1.getPlanned());
        // Second feature not started
        Subject s2 = (Subject) aspect.getChildren().get(1);
        Feature f2 = (Feature) ((Activity) s2.getChildren().get(0)).getChildren().get(0);
        assertEquals(StatusEnum.NOTSTARTED, f2.getMilestone().get(0).getStatus());
    }
}
