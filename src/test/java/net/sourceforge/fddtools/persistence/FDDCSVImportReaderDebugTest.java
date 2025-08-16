package net.sourceforge.fddtools.persistence;

import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Subject;
import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;
import com.opencsv.exceptions.CsvValidationException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FDDCSVImportReader Debug Test")
class FDDCSVImportReaderDebugTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Debug CSV structure parsing")
    void debugCSVStructure() throws IOException, CsvValidationException {
        Path debugCsvFile = tempDir.resolve("debug.csv");
        String debugCsvContent = """
                1,Develop,100%,Sun 1/1/06,OwnerDevelop
                2,Subject_Level2,50%,Mon 1/2/06,OwnerSubject
                3,Activity_Level3,75%,Tue 1/3/06,OwnerActivity
                4,Feature_Level4,60%,Wed 1/4/06,OwnerFeature
                """;
        Files.writeString(debugCsvFile, debugCsvContent);
        
        Project project = FDDCSVImportReader.read(debugCsvFile.toString());
        
        System.out.println("=== DEBUG CSV STRUCTURE ===");
        System.out.println("Project: " + project.getName());
        
        Aspect aspect = project.getAspect().get(0);
        System.out.println("Aspect: " + aspect.getName());
        
        Subject subject = aspect.getSubject().get(0);
        System.out.println("Subject: " + subject.getName());
        
        Activity activity = subject.getActivity().get(0);
        System.out.println("Activity: " + activity.getName());
        System.out.println("Activity initials: " + activity.getInitials());
        
        Feature feature = activity.getFeature().get(0);
        System.out.println("Feature: " + feature.getName());
        System.out.println("Feature initials: " + feature.getInitials());
        System.out.println("Feature progress (milestones): " + feature.getMilestone().size());
        
        // Just verify basic structure works
        assertNotNull(project);
        assertNotNull(aspect);
        assertNotNull(subject);
        assertNotNull(activity);
        assertNotNull(feature);
    }
}
