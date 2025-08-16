package net.sourceforge.fddtools.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FastByteArrayOutputStream.
 * Tests high-performance byte array output stream functionality.
 */
class FastByteArrayOutputStreamTest {

    private FastByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        outputStream = new FastByteArrayOutputStream();
    }

    @Test
    void defaultConstructor() {
        FastByteArrayOutputStream stream = new FastByteArrayOutputStream();
        assertNotNull(stream, "Stream should be created with default constructor");
        assertEquals(0, stream.getSize(), "Initial size should be 0");
    }

    @Test
    void constructorWithSize() {
        FastByteArrayOutputStream stream = new FastByteArrayOutputStream(100);
        assertNotNull(stream, "Stream should be created with size parameter");
        assertEquals(0, stream.getSize(), "Initial size should be 0 regardless of buffer capacity");
    }

    @Test
    void writeSingleByte() throws IOException {
        outputStream.write(65); // 'A'
        assertEquals(1, outputStream.getSize(), "Size should be 1 after writing one byte");
        
        byte[] result = outputStream.getByteArray();
        assertTrue(result.length >= 1, "Result array should have at least 1 element");
        assertEquals(65, result[0], "Written byte should be 65 ('A')");
    }

    @Test
    void writeByteArray() throws IOException {
        byte[] data = "Hello".getBytes();
        outputStream.write(data);
        
        assertEquals(5, outputStream.getSize(), "Size should be 5 after writing 'Hello'");
        
        byte[] result = outputStream.getByteArray();
        assertEquals("Hello", new String(result, 0, outputStream.getSize()), "Result should be 'Hello'");
    }

    @Test
    void writeByteArrayWithOffsetAndLength() throws IOException {
        byte[] data = "Hello, World!".getBytes();
        outputStream.write(data, 7, 5); // Write "World"
        
        assertEquals(5, outputStream.getSize(), "Size should be 5 after writing 'World'");
        
        byte[] result = outputStream.getByteArray();
        assertEquals("World", new String(result, 0, outputStream.getSize()), "Result should be 'World'");
    }

    @Test
    void writeMultipleOperations() throws IOException {
        outputStream.write(72); // 'H'
        outputStream.write("ello".getBytes());
        outputStream.write(", World!".getBytes(), 0, 8);
        
        assertEquals(13, outputStream.getSize(), "Size should be 13");
        
        byte[] result = outputStream.getByteArray();
        assertEquals("Hello, World!", new String(result, 0, outputStream.getSize()), "Result should be 'Hello, World!'");
    }

    @Test
    void reset() throws IOException {
        outputStream.write("Test data".getBytes());
        assertEquals(9, outputStream.getSize(), "Size should be 9 before reset");
        
        outputStream.reset();
        assertEquals(0, outputStream.getSize(), "Size should be 0 after reset");
    }

    @Test
    void getByteArrayMultipleCalls() throws IOException {
        outputStream.write("Test".getBytes());
        
        byte[] result1 = outputStream.getByteArray();
        byte[] result2 = outputStream.getByteArray();
        
        assertSame(result1, result2, "getByteArray should return same array reference");
        assertEquals("Test", new String(result1, 0, outputStream.getSize()), "Content should be 'Test'");
    }

    @Test
    void getInputStream() throws IOException {
        String testData = "Source data for input stream";
        outputStream.write(testData.getBytes());
        
        try (InputStream inputStream = outputStream.getInputStream()) {
            byte[] readBuffer = new byte[testData.length()];
            int bytesRead = inputStream.read(readBuffer);
            
            assertEquals(testData.length(), bytesRead, "Should read all written data");
            assertEquals(testData, new String(readBuffer), "Read data should match written data");
        }
    }

    @Test
    void writeEmptyArray() throws IOException {
        byte[] emptyArray = new byte[0];
        outputStream.write(emptyArray);
        
        assertEquals(0, outputStream.getSize(), "Writing empty array should not change size");
    }

    @Test
    void writeZeroLengthSubArray() throws IOException {
        byte[] data = "Hello".getBytes();
        outputStream.write(data, 2, 0); // Write 0 bytes starting at index 2
        
        assertEquals(0, outputStream.getSize(), "Writing 0 bytes should not change size");
    }

    @Test
    void growBuffer() throws IOException {
        // Create a small buffer and exceed its capacity
        try (FastByteArrayOutputStream smallStream = new FastByteArrayOutputStream(5)) {
            byte[] largeData = "This is a string longer than the initial buffer size".getBytes();
            smallStream.write(largeData);
            
            assertEquals(largeData.length, smallStream.getSize(), "Size should match written data length");
            
            byte[] result = smallStream.getByteArray();
            assertEquals(new String(largeData), new String(result, 0, smallStream.getSize()), "Content should match written data");
        }
    }

    @Test
    void close() {
        // OutputStream close() typically does nothing for ByteArrayOutputStream
        assertDoesNotThrow(() -> {
            outputStream.close();
        }, "Close should not throw exceptions");
        
        // Stream should still be usable after close
        assertDoesNotThrow(() -> {
            outputStream.write(65);
            assertEquals(1, outputStream.getSize(), "Stream should still be usable after close");
        }, "Stream should remain functional after close");
    }

    @Test
    void writeNegativeBytes() throws IOException {
        outputStream.write(-1); // Should be treated as 255
        outputStream.write(-128); // Should be treated as 128
        
        byte[] result = outputStream.getByteArray();
        assertEquals(2, outputStream.getSize(), "Should have written 2 bytes");
        assertEquals((byte) 255, result[0], "First byte should be 255");
        assertEquals((byte) 128, result[1], "Second byte should be 128");
    }

    @Test
    void largeDataWrite() throws IOException {
        // Test with larger data to verify buffer growth
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("Data").append(i).append(" ");
        }
        String largeString = sb.toString();
        
        outputStream.write(largeString.getBytes());
        
        assertEquals(largeString.length(), outputStream.getSize(), "Size should match large data length");
        
        byte[] result = outputStream.getByteArray();
        assertEquals(largeString, new String(result, 0, outputStream.getSize()), "Content should match large data");
    }

    @Test
    void performanceCharacteristics() throws IOException {
        // Test that operations are fast (non-synchronized)
        long startTime = System.nanoTime();
        
        // Perform many operations
        for (int i = 0; i < 10000; i++) {
            outputStream.write(i % 256);
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        assertEquals(10000, outputStream.getSize(), "Should have written 10000 bytes");
        assertTrue(duration > 0, "Operations should complete in measurable time");
    }

    @Test
    void bufferGrowthCharacteristics() throws IOException {
        // Test that buffer grows efficiently
        try (FastByteArrayOutputStream stream = new FastByteArrayOutputStream(10)) {
            // Write data that will cause multiple buffer growths
            for (int i = 0; i < 1000; i++) {
                stream.write(i % 256);
            }
            
            assertEquals(1000, stream.getSize(), "Should have written 1000 bytes");
            
            // Verify the data is intact
            byte[] result = stream.getByteArray();
            for (int i = 0; i < 1000; i++) {
                assertEquals((byte)(i % 256), result[i], "Byte at position " + i + " should be correct");
            }
        }
    }

    @Test
    void inputStreamIntegration() throws IOException {
        // Test round-trip: write to output stream, read via input stream
        String[] testStrings = {"Hello", " ", "World", "!", " ", "Testing", " ", "123"};
        
        for (String str : testStrings) {
            outputStream.write(str.getBytes());
        }
        
        try (InputStream inputStream = outputStream.getInputStream()) {
            StringBuilder readBack = new StringBuilder();
            int b;
            while ((b = inputStream.read()) != -1) {
                readBack.append((char) b);
            }
            
            String expected = String.join("", testStrings);
            assertEquals(expected, readBack.toString(), "Round-trip data should be identical");
        }
    }
}
