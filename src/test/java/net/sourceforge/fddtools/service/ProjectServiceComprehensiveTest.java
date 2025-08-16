package net.sourceforge.fddtools.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.state.ModelState;
import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test class for ProjectService.
 * Tests project lifecycle, state management, and file operations.
 */
class ProjectServiceComprehensiveTest {

    private ProjectService projectService;
    private ObjectFactory objectFactory;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        projectService = ProjectService.getInstance();
        objectFactory = new ObjectFactory();
        
        // Clear any existing project state
        try {
            // Reset the service to a clean state
            projectService.newProject("Test Setup");
        } catch (Exception ignored) {}
    }
    
    @AfterEach
    void tearDown() {
        // Clean up any test state
        ModelState.getInstance().setDirty(false);
    }

    private void resetProjectService() {
        projectService.clear();
        ModelState.getInstance().setDirty(false);
    }

    @Test
    void singletonInstanceReturnsConsistentReference() {
        ProjectService instance1 = ProjectService.getInstance();
        ProjectService instance2 = ProjectService.getInstance();
        
        assertSame(instance1, instance2, "getInstance() should return the same instance");
        assertNotNull(instance1, "getInstance() should never return null");
    }

    @Test
    void newProjectWithName() {
        String projectName = "My Test Project";
        
        projectService.newProject(projectName);
        
        assertEquals(projectName, projectService.getDisplayName(), "Display name should match project name");
        assertNotNull(projectService.getRoot(), "Root should be created");
        assertNull(projectService.getAbsolutePath(), "New project should have no absolute path");
        assertTrue(projectService.hasProjectProperty().get(), "Should have a project");
        assertFalse(projectService.hasPathProperty().get(), "New project should not have a saved path");
        assertFalse(ModelState.getInstance().isDirty(), "New project should not be dirty");
    }

    @Test
    void newProjectWithNullName() {
        projectService.newProject(null);
        
        assertEquals("New Program", projectService.getDisplayName(), "Should use default name for null");
        assertNotNull(projectService.getRoot(), "Root should be created");
        assertTrue(projectService.hasProjectProperty().get(), "Should have a project");
        assertFalse(projectService.hasPathProperty().get(), "New project should not have a saved path");
    }

    @Test
    void newProjectWithEmptyName() {
        projectService.newProject("");
        
        assertEquals("", projectService.getDisplayName(), "Should use empty string if provided");
        assertNotNull(projectService.getRoot(), "Root should be created");
        assertTrue(projectService.hasProjectProperty().get(), "Should have a project");
    }

    @Test
    void newProjectWithExistingRoot() {
        // Create a test root node
        Program testRoot = objectFactory.createProgram();
        testRoot.setName("Test Root");
        String projectName = "External Root Project";
        
        projectService.newProject(testRoot, projectName);
        
        assertEquals(projectName, projectService.getDisplayName(), "Display name should match");
        assertSame(testRoot, projectService.getRoot(), "Should use provided root");
        assertNull(projectService.getAbsolutePath(), "New project should have no path");
        assertTrue(projectService.hasProjectProperty().get(), "Should have a project");
        assertFalse(projectService.hasPathProperty().get(), "Should not have a saved path");
    }

    @Test
    void saveWithoutProject() {
        // Clear any existing project
        resetProjectService();
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            projectService.save();
        }, "Save should throw exception when no project loaded");
        
        assertTrue(exception.getMessage().contains("No project loaded"), 
                  "Exception should indicate no project loaded");
    }

    @Test
    void saveWithoutPath() {
        projectService.newProject("Test Project");
        
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            projectService.save();
        }, "Save should throw exception when no path set");
        
        assertTrue(exception.getMessage().contains("No target path set"), 
                  "Exception should indicate no path set");
    }

    @Test
    void saveAsWithValidPath() throws Exception {
        projectService.newProject("Test Project");
        Path testFile = tempDir.resolve("test-project.fddi");
        
        boolean result = projectService.saveAs(testFile.toString());
        
        assertTrue(result, "SaveAs should succeed with valid path");
        assertEquals(testFile.toString(), projectService.getAbsolutePath(), "Should set absolute path");
        assertEquals("test-project.fddi", projectService.getDisplayName(), "Should set display name from filename");
        assertTrue(projectService.hasPathProperty().get(), "Should have a saved path");
        assertTrue(Files.exists(testFile), "File should be created");
        assertFalse(ModelState.getInstance().isDirty(), "Should not be dirty after successful save");
    }

    @Test
    void saveAsWithNullPath() {
        projectService.newProject("Test Project");
        
        assertThrows(Exception.class, () -> {
            projectService.saveAs(null);
        }, "SaveAs should throw exception for null path");
    }

    @Test
    void saveAsUpdatesPath() throws Exception {
        projectService.newProject("Test Project");
        Path testFile = tempDir.resolve("path-test.fddi");
        
        boolean result = projectService.saveAs(testFile.toString());
        
        assertTrue(result, "SaveAs should succeed");
        assertEquals(testFile.toString(), projectService.getAbsolutePath(), "Path should be updated");
        assertTrue(projectService.hasPathProperty().get(), "Should have path after saveAs");
        assertEquals("path-test.fddi", projectService.getDisplayName(), "Display name should be filename");
    }

    @Test
    void openExistingProject() throws Exception {
        // First create a project file
        projectService.newProject("Original Project");
        Path testFile = tempDir.resolve("existing-project.fddi");
        projectService.saveAs(testFile.toString());
        
        // Now test opening it
        projectService.newProject("Different Project"); // Clear current project
        
        boolean result = projectService.open(testFile.toString());
        
        assertTrue(result, "Open should succeed with existing file");
        assertEquals(testFile.toString(), projectService.getAbsolutePath(), "Should set correct path");
        assertEquals("existing-project.fddi", projectService.getDisplayName(), "Should set display name from filename");
        assertTrue(projectService.hasProjectProperty().get(), "Should have a project");
        assertTrue(projectService.hasPathProperty().get(), "Should have a saved path");
        assertFalse(ModelState.getInstance().isDirty(), "Should not be dirty after open");
    }

    @Test
    void openNonExistentProject() {
        String nonExistentPath = tempDir.resolve("non-existent.fddi").toString();
        
        boolean result = projectService.open(nonExistentPath);
        
        assertFalse(result, "Open should fail for non-existent file");
    }

    @Test
    void openWithRootNode() throws Exception {
        // Create a test file
        projectService.newProject("Test Project");
        Path testFile = tempDir.resolve("root-test.fddi");
        projectService.saveAs(testFile.toString());
        
        // Create a separate root node
        Program externalRoot = objectFactory.createProgram();
        externalRoot.setName("External Root");
        
        boolean result = projectService.openWithRoot(testFile.toString(), externalRoot);
        
        assertTrue(result, "OpenWithRoot should succeed");
        assertSame(externalRoot, projectService.getRoot(), "Should use provided root");
        assertEquals(testFile.toString(), projectService.getAbsolutePath(), "Should set correct path");
        assertEquals("root-test.fddi", projectService.getDisplayName(), "Should set display name");
    }

    @Test
    void projectStatePropertiesUpdate() {
        // Reset to clean state
        resetProjectService();
        
        // Test initial state
        assertFalse(projectService.hasProjectProperty().get(), "Should not have project initially");
        assertFalse(projectService.hasPathProperty().get(), "Should not have path initially");
        
        // Create new project
        projectService.newProject("State Test");
        assertTrue(projectService.hasProjectProperty().get(), "Should have project after creation");
        assertFalse(projectService.hasPathProperty().get(), "Should not have path for new project");
        
        // Save project
        assertDoesNotThrow(() -> {
            Path testFile = tempDir.resolve("state-test.fddi");
            projectService.saveAs(testFile.toString());
            assertTrue(projectService.hasPathProperty().get(), "Should have path after save");
        });
    }

    @Test
    void displayNameHandlesVariousPaths() throws Exception {
        projectService.newProject("Test");
        
        // Test Unix-style path
        Path unixPath = tempDir.resolve("subdir").resolve("unix-file.fddi");
        Files.createDirectories(unixPath.getParent());
        projectService.saveAs(unixPath.toString());
        assertEquals("unix-file.fddi", projectService.getDisplayName(), "Should extract filename from Unix path");
        
        // Test simple filename
        Path simplePath = tempDir.resolve("simple.fddi");
        projectService.saveAs(simplePath.toString());
        assertEquals("simple.fddi", projectService.getDisplayName(), "Should handle simple filename");
    }

    @Test
    void saveAfterSaveAs() throws Exception {
        projectService.newProject("Save Test");
        Path testFile = tempDir.resolve("save-after-saveas.fddi");
        
        // First saveAs
        projectService.saveAs(testFile.toString());
        
        // Mark as dirty and save again
        ModelState.getInstance().setDirty(true);
        boolean result = projectService.save();
        
        assertTrue(result, "Save should succeed after saveAs");
        assertFalse(ModelState.getInstance().isDirty(), "Should not be dirty after save");
    }

    @Test
    void rootNodePreservation() {
        Program originalRoot = objectFactory.createProgram();
        originalRoot.setName("Original");
        projectService.newProject(originalRoot, "Root Test");
        
        assertSame(originalRoot, projectService.getRoot(), "Should preserve exact root instance");
        
        // Add a child to verify the structure is maintained  
        // Program can contain Project children, not Aspect directly
        Project child = objectFactory.createProject();
        child.setName("Child Project");
        originalRoot.add(child);
        
        assertEquals(1, projectService.getRoot().getChildren().size(), "Should maintain root structure");
        assertSame(child, projectService.getRoot().getChildren().get(0), "Should preserve child references");
    }

    @Test
    void errorHandlingDuringOperations() {
        // Test handling of invalid paths during saveAs
        projectService.newProject("Error Test");
        
        // Try to save to an invalid path - should return false rather than throw exception
        boolean result = assertDoesNotThrow(() -> {
            return projectService.saveAs("/invalid/path/that/cannot/exist/test.fddi");
        }, "SaveAs should not throw exception for invalid path");
        
        assertFalse(result, "SaveAs should return false for invalid path");
    }

    @Test
    void projectNameSanitization() {
        // Test various project names to ensure they're handled correctly
        String[] testNames = {
            "Normal Project",
            "Project with Special!@#$%^&*()_+Characters",
            "   Project with leading/trailing spaces   ",
            "Project\nWith\nNewlines",
            "Project\tWith\tTabs",
            "VeryLongProjectNameThatExceedTypicalLengthLimitsButShouldStillBeHandledCorrectlyByTheSystem"
        };
        
        for (String name : testNames) {
            assertDoesNotThrow(() -> {
                projectService.newProject(name);
                assertEquals(name, projectService.getDisplayName(), 
                           "Should preserve project name: " + name);
            }, "Should handle project name: " + name);
        }
    }

    @Test
    void concurrentOperationsSafety() {
        // Basic test for thread safety - ProjectService should handle concurrent access
        projectService.newProject("Concurrency Test");
        
        Thread[] threads = new Thread[5];
        final boolean[] success = new boolean[5];
        
        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    String name = projectService.getDisplayName();
                    FDDINode root = projectService.getRoot();
                    String path = projectService.getAbsolutePath();
                    boolean hasProject = projectService.hasProjectProperty().get();
                    boolean hasPath = projectService.hasPathProperty().get();
                    
                    // All operations should succeed without throwing
                    // Using all variables to avoid warnings
                    success[index] = name != null && root != null && 
                                   (path != null || path == null) && 
                                   (hasProject || !hasProject) && 
                                   (hasPath || !hasPath);
                } catch (Exception e) {
                    success[index] = false;
                }
            });
        }
        
        assertDoesNotThrow(() -> {
            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join(1000); // 1 second timeout
            }
        }, "Concurrent access should not cause exceptions");
        
        for (int i = 0; i < 5; i++) {
            assertTrue(success[i], "Thread " + i + " should succeed");
        }
    }

}
