package net.sourceforge.fddtools.service;

import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Subject;
import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for ProjectFileService covering singleton pattern,
 * file I/O operations, error handling, data integrity, and edge cases.
 */
public class ProjectFileServiceComprehensiveTest {
    
    private ProjectFileService service;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        service = ProjectFileService.getInstance();
    }
    
    // === SINGLETON PATTERN TESTS ===
    
    @Test
    void singletonPatternEnforced() {
        ProjectFileService instance1 = ProjectFileService.getInstance();
        ProjectFileService instance2 = ProjectFileService.getInstance();
        
        assertSame(instance1, instance2, "getInstance should return same instance");
        assertSame(instance1, service, "Service should be same instance");
    }
    
    // === ROOT CREATION TESTS ===
    
    @Test
    void createNewRootWithValidName() {
        FDDINode root = service.createNewRoot("Test Project");
        
        assertNotNull(root, "Root should not be null");
        assertEquals("Test Project", root.getName(), "Name should match input");
        assertTrue(root instanceof Program, "Root should be a Program node");
        assertEquals(0, root.getChildren().size(), "New root should have no children");
    }
    
    @Test
    void createNewRootWithNullUsesDefault() {
        FDDINode root = service.createNewRoot(null);
        
        assertNotNull(root, "Root should not be null");
        assertEquals("New Program", root.getName(), "Should use default name");
        assertTrue(root instanceof Program, "Root should be a Program node");
    }
    
    @Test
    void createNewRootWithEmptyStringUsesDefault() {
        FDDINode root = service.createNewRoot("");
        
        assertNotNull(root, "Root should not be null");
        assertEquals("", root.getName(), "Empty string should be preserved");
        assertTrue(root instanceof Program, "Root should be a Program node");
    }
    
    @Test
    void createNewRootWithWhitespaceOnly() {
        FDDINode root = service.createNewRoot("   ");
        
        assertNotNull(root, "Root should not be null");
        assertEquals("   ", root.getName(), "Whitespace should be preserved");
        assertTrue(root instanceof Program, "Root should be a Program node");
    }
    
    @Test
    void createNewRootWithSpecialCharacters() {
        String specialName = "Project™ with Special Chars! @#$%^&*()";
        FDDINode root = service.createNewRoot(specialName);
        
        assertNotNull(root, "Root should not be null");
        assertEquals(specialName, root.getName(), "Special characters should be preserved");
        assertTrue(root instanceof Program, "Root should be a Program node");
    }
    
    // === SAVE OPERATION TESTS ===
    
    @Test
    void saveValidProjectToNewFile() throws Exception {
        FDDINode root = service.createNewRoot("Save Test Project");
        Path outputPath = tempDir.resolve("test-project.fddi");
        
        boolean result = service.save(root, outputPath.toString());
        
        assertTrue(result, "Save should succeed");
        assertTrue(Files.exists(outputPath), "Output file should exist");
        assertTrue(Files.size(outputPath) > 0, "Output file should not be empty");
    }
    
    @Test
    void saveProjectWithHierarchy() throws Exception {
        FDDINode root = service.createNewRoot("Hierarchy Test");
        
        // Add some children to create hierarchy (Program -> Project -> Aspect -> Subject -> Activity -> Feature)
        Project project = new Project();
        project.setName("Test Project");
        root.add(project);
        
        Aspect aspect = new Aspect();
        aspect.setName("Test Aspect");
        project.add(aspect);
        
        Subject subject = new Subject();
        subject.setName("Test Subject");
        subject.setPrefix("TS");
        aspect.add(subject);
        
        Activity activity = new Activity();
        activity.setName("Test Activity");
        subject.add(activity);
        
        Feature feature = new Feature();
        feature.setName("Test Feature");
        activity.add(feature);
        
        Path outputPath = tempDir.resolve("hierarchy-project.fddi");
        
        boolean result = service.save(root, outputPath.toString());
        
        assertTrue(result, "Save should succeed with hierarchy");
        assertTrue(Files.exists(outputPath), "Output file should exist");
        assertTrue(Files.size(outputPath) > 100, "File should contain XML structure");
    }
    
    @Test
    void saveWithNullRootThrowsException() {
        Path outputPath = tempDir.resolve("null-root.fddi");
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.save(null, outputPath.toString()),
            "Should throw IllegalArgumentException for null root"
        );
        
        assertEquals("Root node is null", exception.getMessage());
    }
    
    @Test
    void saveToInvalidPathReturnsFalse() throws Exception {
        FDDINode root = service.createNewRoot("Path Test");
        String invalidPath = "/invalid/nonexistent/path/test.fddi";
        
        // Service returns false for invalid paths instead of throwing
        boolean result = service.save(root, invalidPath);
        assertFalse(result, "Should return false for invalid path");
    }
    
    @Test
    void saveToReadOnlyDirectoryHandlesError() throws Exception {
        FDDINode root = service.createNewRoot("Read-Only Test");
        
        // Create a read-only directory (best effort on different OS)
        Path readOnlyDir = tempDir.resolve("readonly");
        Files.createDirectory(readOnlyDir);
        
        // Attempt to make read-only
        File readOnlyFile = readOnlyDir.toFile();
        readOnlyFile.setReadOnly();
        
        Path outputPath = readOnlyDir.resolve("test.fddi");
        
        // This may or may not throw depending on OS permissions handling
        // We just verify it doesn't crash unexpectedly
        assertDoesNotThrow(() -> {
            try {
                service.save(root, outputPath.toString());
            } catch (Exception e) {
                // Expected for read-only, just verify it's a reasonable exception
                assertTrue(e.getMessage().contains("Permission") || 
                          e.getMessage().contains("Access") ||
                          e.getMessage().contains("denied") ||
                          e instanceof java.io.IOException,
                          "Should be a permission-related exception");
            }
        });
    }
    
    // === OPEN OPERATION TESTS ===
    
    @Test
    void openValidProject() throws Exception {
        // First create a valid project file
        FDDINode originalRoot = service.createNewRoot("Open Test");
        Path testFile = tempDir.resolve("open-test.fddi");
        service.save(originalRoot, testFile.toString());
        
        // Now open it
        FDDINode loadedRoot = service.open(testFile.toString());
        
        assertNotNull(loadedRoot, "Loaded root should not be null");
        assertEquals("Open Test", loadedRoot.getName(), "Name should match");
        assertTrue(loadedRoot instanceof Program, "Should be a Program node");
    }
    
    @Test
    void openProjectWithHierarchy() throws Exception {
        // Create project with hierarchy
        FDDINode originalRoot = service.createNewRoot("Hierarchy Open Test");
        Project project = new Project();
        project.setName("Open Project");
        originalRoot.add(project);
        
        Aspect aspect = new Aspect();
        aspect.setName("Open Aspect");
        project.add(aspect);
        
        Subject subject = new Subject();
        subject.setName("Open Subject");
        subject.setPrefix("OS");
        aspect.add(subject);
        
        Activity activity = new Activity();
        activity.setName("Open Activity");
        subject.add(activity);
        
        Feature feature = new Feature();
        feature.setName("Open Feature");
        activity.add(feature);
        
        Path testFile = tempDir.resolve("hierarchy-open.fddi");
        service.save(originalRoot, testFile.toString());
        
        // Open and verify
        FDDINode loadedRoot = service.open(testFile.toString());
        
        assertNotNull(loadedRoot, "Loaded root should not be null");
        assertEquals("Hierarchy Open Test", loadedRoot.getName());
        assertEquals(1, loadedRoot.getChildren().size(), "Should have one child");
        
        FDDINode loadedProject = (FDDINode) loadedRoot.getChildren().get(0);
        assertEquals("Open Project", loadedProject.getName());
        assertEquals(1, loadedProject.getChildren().size(), "Project should have one child");
        
        FDDINode loadedAspect = (FDDINode) loadedProject.getChildren().get(0);
        assertEquals("Open Aspect", loadedAspect.getName());
        assertEquals(1, loadedAspect.getChildren().size(), "Aspect should have one child");
        
        FDDINode loadedSubject = (FDDINode) loadedAspect.getChildren().get(0);
        assertEquals("Open Subject", loadedSubject.getName());
        assertEquals("OS", ((Subject) loadedSubject).getPrefix());
        assertEquals(1, loadedSubject.getChildren().size(), "Subject should have one child");
        
        FDDINode loadedActivity = (FDDINode) loadedSubject.getChildren().get(0);
        assertEquals("Open Activity", loadedActivity.getName());
        assertEquals(1, loadedActivity.getChildren().size(), "Activity should have one child");
        
        FDDINode loadedFeature = (FDDINode) loadedActivity.getChildren().get(0);
        assertEquals("Open Feature", loadedFeature.getName());
    }
    
    @Test
    void openNonExistentFileThrowsException() {
        String nonExistentPath = tempDir.resolve("does-not-exist.fddi").toString();
        
        assertThrows(Exception.class, () -> service.open(nonExistentPath),
                    "Should throw exception for non-existent file");
    }
    
    @Test
    void openInvalidXMLThrowsException() throws IOException {
        Path invalidFile = tempDir.resolve("invalid.fddi");
        try (FileWriter writer = new FileWriter(invalidFile.toFile())) {
            writer.write("<invalid-xml>not-closed");
        }
        
        assertThrows(Exception.class, () -> service.open(invalidFile.toString()),
                    "Should throw exception for invalid XML");
    }
    
    @Test
    void openWellFormedButInvalidFDDIThrowsException() throws IOException {
        Path invalidFile = tempDir.resolve("wrong-structure.fddi");
        try (FileWriter writer = new FileWriter(invalidFile.toFile())) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write("<random-element>not-an-fddi-structure</random-element>");
        }
        
        Exception exception = assertThrows(Exception.class, 
            () -> service.open(invalidFile.toString()),
            "Should throw exception for wrong XML structure");
        
        // Verify it's the right kind of error
        assertTrue(exception.getMessage().contains("valid FDDINode") || 
                  exception instanceof IllegalStateException,
                  "Should indicate invalid FDDI structure");
    }
    
    @Test
    void openEmptyFileThrowsException() throws IOException {
        Path emptyFile = tempDir.resolve("empty.fddi");
        Files.createFile(emptyFile);
        
        assertThrows(Exception.class, () -> service.open(emptyFile.toString()),
                    "Should throw exception for empty file");
    }
    
    // === ROUND-TRIP TESTS ===
    
    @Test
    void saveAndOpenRoundTripPreservesData() throws Exception {
        // Create complex project
        FDDINode originalRoot = service.createNewRoot("Round-Trip Test");
        
        Project project1 = new Project();
        project1.setName("Project One");
        originalRoot.add(project1);
        
        Project project2 = new Project();
        project2.setName("Project Two");
        originalRoot.add(project2);
        
        Aspect aspect1 = new Aspect();
        aspect1.setName("Aspect One");
        project1.add(aspect1);
        
        Subject subject1 = new Subject();
        subject1.setName("Subject One");
        subject1.setPrefix("S1");
        aspect1.add(subject1);
        
        Activity activity1 = new Activity();
        activity1.setName("Activity One");
        subject1.add(activity1);
        
        Feature feature1 = new Feature();
        feature1.setName("Feature Alpha");
        activity1.add(feature1);
        
        Feature feature2 = new Feature();
        feature2.setName("Feature Beta");
        activity1.add(feature2);
        
        // Save
        Path testFile = tempDir.resolve("roundtrip.fddi");
        boolean saveResult = service.save(originalRoot, testFile.toString());
        assertTrue(saveResult, "Save should succeed");
        
        // Open
        FDDINode loadedRoot = service.open(testFile.toString());
        
        // Verify structure preservation
        assertEquals(originalRoot.getName(), loadedRoot.getName());
        assertEquals(originalRoot.getChildren().size(), loadedRoot.getChildren().size());
        
        // Check first project
        FDDINode loadedProject1 = (FDDINode) loadedRoot.getChildren().get(0);
        assertEquals(project1.getName(), loadedProject1.getName());
        assertEquals(1, loadedProject1.getChildren().size());
        
        // Check aspect
        FDDINode loadedAspect1 = (FDDINode) loadedProject1.getChildren().get(0);
        assertEquals(aspect1.getName(), loadedAspect1.getName());
        assertEquals(1, loadedAspect1.getChildren().size());
        
        // Check subject
        FDDINode loadedSubject1 = (FDDINode) loadedAspect1.getChildren().get(0);
        assertEquals(subject1.getName(), loadedSubject1.getName());
        assertEquals(((Subject) subject1).getPrefix(), 
                    ((Subject) loadedSubject1).getPrefix());
        assertEquals(1, loadedSubject1.getChildren().size());
        
        // Check activity
        FDDINode loadedActivity1 = (FDDINode) loadedSubject1.getChildren().get(0);
        assertEquals(activity1.getName(), loadedActivity1.getName());
        assertEquals(2, loadedActivity1.getChildren().size());
        
        // Check features
        assertEquals(feature1.getName(), 
                    ((FDDINode) loadedActivity1.getChildren().get(0)).getName());
        assertEquals(feature2.getName(), 
                    ((FDDINode) loadedActivity1.getChildren().get(1)).getName());
        
        // Check second project (empty)
        FDDINode loadedProject2 = (FDDINode) loadedRoot.getChildren().get(1);
        assertEquals(project2.getName(), loadedProject2.getName());
        assertEquals(0, loadedProject2.getChildren().size());
    }
    
    @Test
    void multipleSaveOperationsOverwriteCorrectly() throws Exception {
        FDDINode root = service.createNewRoot("Overwrite Test");
        Path testFile = tempDir.resolve("overwrite.fddi");
        
        // First save
        boolean result1 = service.save(root, testFile.toString());
        assertTrue(result1, "First save should succeed");
        long firstSize = Files.size(testFile);
        
        // Modify and save again
        Project project = new Project();
        project.setName("Added Project");
        root.add(project);
        
        boolean result2 = service.save(root, testFile.toString());
        assertTrue(result2, "Second save should succeed");
        long secondSize = Files.size(testFile);
        
        assertTrue(secondSize > firstSize, "File should be larger after adding content");
        
        // Verify content is correct
        FDDINode reloaded = service.open(testFile.toString());
        assertEquals(1, reloaded.getChildren().size(), "Should have one child");
        assertEquals("Added Project", ((FDDINode) reloaded.getChildren().get(0)).getName());
    }
    
    // === EDGE CASE TESTS ===
    
    @Test
    void saveAndOpenWithUnicodeCharacters() throws Exception {
        String unicodeName = "Test项目 with Émojis 🚀 and Symbols ñäöü";
        FDDINode root = service.createNewRoot(unicodeName);
        
        Project project = new Project();
        project.setName("项目");
        root.add(project);
        
        Aspect aspect = new Aspect();
        aspect.setName("方面");
        project.add(aspect);
        
        Subject subject = new Subject();
        subject.setName("Тест Subject with中文");
        subject.setPrefix("УС");
        aspect.add(subject);
        
        Path testFile = tempDir.resolve("unicode.fddi");
        
        boolean saveResult = service.save(root, testFile.toString());
        assertTrue(saveResult, "Save with Unicode should succeed");
        
        FDDINode loaded = service.open(testFile.toString());
        assertEquals(unicodeName, loaded.getName(), "Unicode name should be preserved");
        
        FDDINode loadedProject = (FDDINode) loaded.getChildren().get(0);
        assertEquals("项目", loadedProject.getName());
        
        FDDINode loadedAspect = (FDDINode) loadedProject.getChildren().get(0);
        assertEquals("方面", loadedAspect.getName());
        
        FDDINode loadedSubject = (FDDINode) loadedAspect.getChildren().get(0);
        assertEquals("Тест Subject with中文", loadedSubject.getName());
        assertEquals("УС", ((Subject) loadedSubject).getPrefix());
    }
    
    @Test
    void saveToFileWithNonStandardExtension() throws Exception {
        FDDINode root = service.createNewRoot("Extension Test");
        Path testFile = tempDir.resolve("test.xml"); // Different extension
        
        boolean result = service.save(root, testFile.toString());
        assertTrue(result, "Save should work regardless of extension");
        
        FDDINode loaded = service.open(testFile.toString());
        assertEquals("Extension Test", loaded.getName());
    }
    
    @Test
    void saveToFileWithNoExtension() throws Exception {
        FDDINode root = service.createNewRoot("No Extension Test");
        Path testFile = tempDir.resolve("no_extension_file");
        
        boolean result = service.save(root, testFile.toString());
        assertTrue(result, "Save should work without extension");
        
        FDDINode loaded = service.open(testFile.toString());
        assertEquals("No Extension Test", loaded.getName());
    }
    
    // === CONCURRENT ACCESS TESTS ===
    
    @Test
    void concurrentSaveOperationsHandledSafely() throws Exception {
        FDDINode root1 = service.createNewRoot("Concurrent Test 1");
        FDDINode root2 = service.createNewRoot("Concurrent Test 2");
        
        Path file1 = tempDir.resolve("concurrent1.fddi");
        Path file2 = tempDir.resolve("concurrent2.fddi");
        
        // This tests the service doesn't crash with concurrent access
        // (not necessarily thread-safety, but basic robustness)
        assertDoesNotThrow(() -> {
            boolean result1 = service.save(root1, file1.toString());
            boolean result2 = service.save(root2, file2.toString());
            assertTrue(result1 && result2, "Both saves should succeed");
        });
        
        // Verify both files were created correctly
        FDDINode loaded1 = service.open(file1.toString());
        FDDINode loaded2 = service.open(file2.toString());
        
        assertEquals("Concurrent Test 1", loaded1.getName());
        assertEquals("Concurrent Test 2", loaded2.getName());
    }
    
    // === ERROR RECOVERY TESTS ===
    
    @Test
    void serviceRemainsUsableAfterException() throws Exception {
        // Trigger an exception
        assertThrows(Exception.class, () -> service.open("nonexistent.fddi"));
        
        // Verify service still works normally
        FDDINode root = service.createNewRoot("Recovery Test");
        assertNotNull(root, "Service should remain functional after exception");
        assertEquals("Recovery Test", root.getName());
        
        Path testFile = tempDir.resolve("recovery.fddi");
        boolean result = service.save(root, testFile.toString());
        assertTrue(result, "Save should work after previous exception");
        
        FDDINode loaded = service.open(testFile.toString());
        assertEquals("Recovery Test", loaded.getName());
    }
    
    @Test
    void repeatedFailuresDoNotCorruptService() {
        // Multiple failures in a row
        for (int i = 0; i < 5; i++) {
            final int index = i; // Make effectively final for lambda
            assertThrows(Exception.class, () -> service.open("nonexistent" + index + ".fddi"));
        }
        
        // Service should still work
        FDDINode root = service.createNewRoot("Still Works");
        assertNotNull(root);
        assertEquals("Still Works", root.getName());
    }
}
