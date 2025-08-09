package net.sourceforge.fddtools.persistence;

import com.nebulon.xml.fddi.*;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.Test;

import java.io.File;
import javax.xml.datatype.DatatypeFactory;

import static org.junit.jupiter.api.Assertions.*;

/** Basic save/open round trip to ensure structural fields persist. */
public class PersistenceRoundTripTest {

    private Program buildSampleProgram() {
        ObjectFactory of = new ObjectFactory();
        Program program = of.createProgram();
        program.setName("SampleProgram");
        Project project = of.createProject();
        project.setName("ProjA");
        Aspect aspect = of.createAspect();
        aspect.setName("Aspect1");
        aspect.setInfo(of.createAspectInfo());
        aspect.getInfo().setSubjectName("Subj");
        aspect.getInfo().setActivityName("Act");
        aspect.getInfo().setFeatureName("Feat");
        aspect.getInfo().setMilestoneName("MS"); // required by schema
        // Add single milestoneInfo entry with required fields
        MilestoneInfo mi = of.createMilestoneInfo();
        mi.setName("MS Definition");
        mi.setEffort(5);
        aspect.getInfo().getMilestoneInfo().add(mi);
        Subject subject = of.createSubject();
        subject.setPrefix("S1");
        subject.setName("Subj1");
        Activity activity = of.createActivity();
        activity.setName("Act1");
        Feature feature = of.createFeature();
        feature.setName("Feat1");
        Milestone ms = of.createMilestone();
        try { ms.setPlanned(DatatypeFactory.newInstance().newXMLGregorianCalendar("2025-12-31")); } catch (Exception ignored) {}
        ms.setStatus(StatusEnum.COMPLETE);
        feature.getMilestone().add(ms);
        activity.add(feature);
        subject.add(activity);
        aspect.add(subject);
        project.add(aspect);
        program.add(project);
        return program;
    }

    @Test
    void saveAndOpenRoundTripPreservesNames() throws Exception {
        Program original = buildSampleProgram();
        File tmp = File.createTempFile("round", ".fddi");
        tmp.deleteOnExit();
        boolean wrote = FDDIXMLFileWriter.write(original, tmp.getAbsolutePath());
        assertTrue(wrote);
        Object read = FDDIXMLFileReader.read(tmp.getAbsolutePath());
        assertNotNull(read);
        assertTrue(read instanceof Program);
        Program loaded = (Program) read;
        assertEquals("SampleProgram", loaded.getName());
        assertFalse(loaded.getChildren().isEmpty());
        var proj = loaded.getChildren().get(0); // Project
        assertEquals("ProjA", ((FDDINode) proj).getName());
    }
}
