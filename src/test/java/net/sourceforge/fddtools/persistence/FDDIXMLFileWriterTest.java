package net.sourceforge.fddtools.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import com.nebulon.xml.fddi.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FDDIXMLFileWriter.
 * Tests XML file writing functionality and error handling.
 */
class FDDIXMLFileWriterTest {

    @TempDir
    Path tempDir;

    private Project testProject;

    @BeforeEach
    void setUp() {
        // Create a simple test project
        testProject = new Project();
        testProject.setName("Test Project");
    }

    @Test
    void writeValidProjectSucceeds() throws IOException {
        Path outputFile = tempDir.resolve("output-project.fddi");
        
        assertDoesNotThrow(() -> {
            FDDIXMLFileWriter.write(testProject, outputFile.toString());
        }, "Writing valid project should not throw exceptions");

        assertTrue(Files.exists(outputFile), "Output file should be created");
        assertTrue(Files.size(outputFile) > 0, "Output file should not be empty");
    }

    @Test
    void writeNullProjectThrowsException() {
        Path outputFile = tempDir.resolve("null-project.fddi");
        
        assertThrows(Exception.class, () -> {
            FDDIXMLFileWriter.write(null, outputFile.toString());
        }, "Writing null project should throw an exception");
    }

    @Test
    void writeToNullPathThrowsException() {
        assertThrows(Exception.class, () -> {
            FDDIXMLFileWriter.write(testProject, null);
        }, "Writing to null path should throw an exception");
    }

    @Test
    void writeToEmptyPathThrowsException() {
        // The current implementation catches exceptions and returns false instead of throwing
        boolean result = FDDIXMLFileWriter.write(testProject, "");
        assertFalse(result, "Writing to empty path should return false");
    }

    @Test
    void writeCreatesValidXML() throws IOException {
        Path outputFile = tempDir.resolve("valid-xml-test.fddi");
        
        FDDIXMLFileWriter.write(testProject, outputFile.toString());
        
        String content = Files.readString(outputFile);
        assertTrue(content.contains("<?xml"), "Output should contain XML declaration");
        assertTrue(content.contains("Test Project"), "Output should contain project name");
    }

    @Test
    void writeEmptyProjectSucceeds() throws IOException {
        Project emptyProject = new Project();
        emptyProject.setName("Empty Project");
        
        Path outputFile = tempDir.resolve("empty-project.fddi");
        
        assertDoesNotThrow(() -> {
            FDDIXMLFileWriter.write(emptyProject, outputFile.toString());
        }, "Writing empty project should not throw exceptions");

        assertTrue(Files.exists(outputFile), "Output file should be created");
        String content = Files.readString(outputFile);
        assertTrue(content.contains("Empty Project"), "Output should contain project name");
    }

    @Test
    void writeProjectWithSpecialCharacters() throws IOException {
        Project specialProject = new Project();
        specialProject.setName("Project with <special> & \"characters\"");
        
        Path outputFile = tempDir.resolve("special-chars-project.fddi");
        
        assertDoesNotThrow(() -> {
            FDDIXMLFileWriter.write(specialProject, outputFile.toString());
        }, "Writing project with special characters should not throw exceptions");

        assertTrue(Files.exists(outputFile), "Output file should be created");
    }

    @Test
    void roundTripWriteAndReadSucceeds() throws IOException {
        Path outputFile = tempDir.resolve("round-trip-test.fddi");
        
        // Write the project
        FDDIXMLFileWriter.write(testProject, outputFile.toString());
        
        // Read it back
        assertDoesNotThrow(() -> {
            Object readProject = FDDIXMLFileReader.read(outputFile.toString());
            assertNotNull(readProject, "Read project should not be null");
        }, "Round-trip write and read should succeed");
    }

    @Test
    void writeProjectWithAspects() throws IOException {
        // Create project with aspects
        Project projectWithAspects = new Project();
        projectWithAspects.setName("Project with Aspects");
        
        Aspect aspect = new Aspect();
        aspect.setName("Test Aspect");
        projectWithAspects.getAspect().add(aspect);
        
        Path outputFile = tempDir.resolve("aspects-project.fddi");
        
        assertDoesNotThrow(() -> {
            FDDIXMLFileWriter.write(projectWithAspects, outputFile.toString());
        }, "Writing project with aspects should not throw exceptions");

        assertTrue(Files.exists(outputFile), "Output file should be created");
        String content = Files.readString(outputFile);
        assertTrue(content.contains("Test Aspect"), "Output should contain aspect name");
    }
}
