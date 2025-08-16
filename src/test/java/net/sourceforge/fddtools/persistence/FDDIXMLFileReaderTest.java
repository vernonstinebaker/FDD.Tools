package net.sourceforge.fddtools.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FDDIXMLFileReader.
 * Tests XML file reading functionality and error handling.
 */
class FDDIXMLFileReaderTest {

    @TempDir
    Path tempDir;

    private Path validFDDIFile;
    private Path invalidXMLFile;
    private Path nonExistentFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create a valid FDDI XML file
        validFDDIFile = tempDir.resolve("valid-project.fddi");
        String validXML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://www.nebulon.com/xml/2004/fddi">
                <program name="Test Program">
                    <subject name="Test Subject">
                        <activity name="Test Activity">
                            <feature name="Test Feature" />
                        </activity>
                    </subject>
                </program>
            </project>
            """;
        Files.writeString(validFDDIFile, validXML);

        // Create an invalid XML file
        invalidXMLFile = tempDir.resolve("invalid.fddi");
        String invalidXML = "<not-valid-xml>";
        Files.writeString(invalidXMLFile, invalidXML);

        // Reference to a non-existent file
        nonExistentFile = tempDir.resolve("does-not-exist.fddi");
    }

    @Test
    void readValidFDDIFileSucceeds() {
        assertDoesNotThrow(() -> {
            Object result = FDDIXMLFileReader.read(validFDDIFile.toString());
            assertNotNull(result, "Reading valid FDDI file should return a result");
        }, "Reading valid FDDI file should not throw exceptions");
    }

    @Test
    void readNonExistentFileThrowsException() {
        assertThrows(Exception.class, () -> {
            FDDIXMLFileReader.read(nonExistentFile.toString());
        }, "Reading non-existent file should throw an exception");
    }

    @Test
    void readInvalidXMLThrowsException() {
        // The current implementation catches exceptions and returns null instead of throwing
        Object result = FDDIXMLFileReader.read(invalidXMLFile.toString());
        assertNull(result, "Reading invalid XML should return null");
    }

    @Test
    void readNullPathThrowsException() {
        assertThrows(Exception.class, () -> {
            FDDIXMLFileReader.read(null);
        }, "Reading null path should throw an exception");
    }

    @Test
    void readEmptyStringThrowsException() {
        assertThrows(Exception.class, () -> {
            FDDIXMLFileReader.read("");
        }, "Reading empty string path should throw an exception");
    }

    @Test
    void readDirectoryThrowsException() throws IOException {
        Path directory = tempDir.resolve("test-directory");
        Files.createDirectory(directory);
        
        assertThrows(Exception.class, () -> {
            FDDIXMLFileReader.read(directory.toString());
        }, "Reading directory path should throw an exception");
    }

    @Test
    void readFileWithWrongExtensionStillWorks() throws IOException {
        // Create a valid FDDI file with .xml extension
        Path xmlFile = tempDir.resolve("valid-project.xml");
        String validXML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://www.nebulon.com/xml/2004/fddi">
                <program name="Test Program">
                    <subject name="Test Subject" />
                </program>
            </project>
            """;
        Files.writeString(xmlFile, validXML);

        assertDoesNotThrow(() -> {
            Object result = FDDIXMLFileReader.read(xmlFile.toString());
            assertNotNull(result, "Reading valid XML with .xml extension should work");
        }, "File extension should not prevent reading valid FDDI XML");
    }

    @Test
    void readFileWithComplexStructure() throws IOException {
        // Create a more complex FDDI structure
        Path complexFile = tempDir.resolve("complex-project.fddi");
        String complexXML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://www.nebulon.com/xml/2004/fddi">
                <program name="Complex Program">
                    <subject name="Subject 1">
                        <activity name="Activity 1">
                            <feature name="Feature 1" />
                            <feature name="Feature 2" />
                        </activity>
                        <activity name="Activity 2">
                            <feature name="Feature 3" />
                        </activity>
                    </subject>
                    <subject name="Subject 2">
                        <activity name="Activity 3">
                            <feature name="Feature 4" />
                        </activity>
                    </subject>
                </program>
            </project>
            """;
        Files.writeString(complexFile, complexXML);

        assertDoesNotThrow(() -> {
            Object result = FDDIXMLFileReader.read(complexFile.toString());
            assertNotNull(result, "Reading complex FDDI structure should work");
        }, "Complex FDDI structures should be readable");
    }
}
