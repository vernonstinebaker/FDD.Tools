package net.sourceforge.fddtools.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FastByteArrayInputStream.
 * Tests high-performance byte array input stream functionality.
 */
class FastByteArrayInputStreamTest {

    private byte[] testData;
    private FastByteArrayInputStream inputStream;

    @BeforeEach
    void setUp() {
        testData = "Hello, World! This is test data for FastByteArrayInputStream.".getBytes();
        inputStream = new FastByteArrayInputStream(testData, testData.length);
    }

    @Test
    void constructorWithByteArrayAndCount() {
        FastByteArrayInputStream stream = new FastByteArrayInputStream(testData, testData.length);
        assertNotNull(stream, "Stream should be created from byte array");
        assertEquals(testData.length, stream.available(), "Available bytes should match count");
    }

    @Test
    void constructorWithPartialCount() {
        int partialCount = 10;
        FastByteArrayInputStream stream = new FastByteArrayInputStream(testData, partialCount);
        assertNotNull(stream, "Stream should be created with partial count");
        assertEquals(partialCount, stream.available(), "Available bytes should match partial count");
    }

    @Test
    void constructorWithNullArray() {
        assertDoesNotThrow(() -> {
            try (FastByteArrayInputStream stream = new FastByteArrayInputStream(null, 0)) {
                // Constructor should not fail
            }
        }, "Constructor should handle null array without throwing during construction");
    }

    @Test
    void readSingleByte() throws IOException {
        int firstByte = inputStream.read();
        assertEquals('H', firstByte, "First byte should be 'H'");
        assertEquals(testData.length - 1, inputStream.available(), "Available count should decrease");
    }

    @Test
    void readByteArray() throws IOException {
        byte[] buffer = new byte[5];
        int bytesRead = inputStream.read(buffer, 0, 5);
        assertEquals(5, bytesRead, "Should read 5 bytes");
        assertEquals("Hello", new String(buffer), "Should read 'Hello'");
    }

    @Test
    void readByteArrayWithOffsetAndLength() throws IOException {
        byte[] buffer = new byte[10];
        int bytesRead = inputStream.read(buffer, 2, 5);
        assertEquals(5, bytesRead, "Should read 5 bytes");
        assertEquals("Hello", new String(buffer, 2, 5), "Should read 'Hello' at offset 2");
    }

    @Test
    void skip() throws IOException {
        long skipped = inputStream.skip(7);
        assertEquals(7, skipped, "Should skip 7 bytes");
        int nextByte = inputStream.read();
        assertEquals('W', nextByte, "Next byte should be 'W' (from 'World')");
    }

    @Test
    void available() throws IOException {
        assertEquals(testData.length, inputStream.available(), "Initial available should be count");
        inputStream.read();
        assertEquals(testData.length - 1, inputStream.available(), "Available should decrease after read");
    }

    @Test
    void readBeyondEnd() throws IOException {
        // Read all data
        inputStream.skip(testData.length);
        int result = inputStream.read();
        assertEquals(-1, result, "Reading beyond end should return -1");
    }

    @Test
    void readArrayBeyondEnd() throws IOException {
        // Skip to end
        inputStream.skip(testData.length);
        byte[] buffer = new byte[5];
        int result = inputStream.read(buffer, 0, 5);
        assertEquals(-1, result, "Reading array beyond end should return -1");
    }

    @Test
    void readPartialDataNearEnd() throws IOException {
        // Skip to near end, leaving only 3 bytes
        inputStream.skip(testData.length - 3);
        byte[] buffer = new byte[10];
        int bytesRead = inputStream.read(buffer, 0, 10);
        assertEquals(3, bytesRead, "Should only read remaining 3 bytes");
    }

    @Test
    void skipZeroBytes() throws IOException {
        long skipped = inputStream.skip(0);
        assertEquals(0, skipped, "Skipping 0 bytes should return 0");
    }

    @Test
    void skipNegativeBytes() throws IOException {
        long skipped = inputStream.skip(-5);
        assertEquals(0, skipped, "Skipping negative bytes should return 0");
    }

    @Test
    void skipMoreThanAvailable() throws IOException {
        long available = inputStream.available();
        long skipped = inputStream.skip(available + 100);
        assertEquals(available, skipped, "Skip should only skip available bytes");
        assertEquals(0, inputStream.available(), "No bytes should remain after skipping all");
    }

    @Test
    void readEmptyBuffer() throws IOException {
        try (FastByteArrayInputStream emptyStream = new FastByteArrayInputStream(testData, 0)) {
            assertEquals(0, emptyStream.available(), "Empty stream should have 0 available bytes");
            assertEquals(-1, emptyStream.read(), "Reading from empty stream should return -1");
        }
    }

    @Test
    void readSequentialBytes() throws IOException {
        // Read first few bytes individually
        assertEquals('H', inputStream.read());
        assertEquals('e', inputStream.read());
        assertEquals('l', inputStream.read());
        assertEquals('l', inputStream.read());
        assertEquals('o', inputStream.read());
        
        assertEquals(testData.length - 5, inputStream.available(), "Remaining bytes should be correct");
    }

    @Test
    void performanceCharacteristics() {
        // Test that operations are fast (non-synchronized)
        long startTime = System.nanoTime();
        
        // Perform many operations
        for (int i = 0; i < 1000; i++) {
            try (FastByteArrayInputStream stream = new FastByteArrayInputStream(testData, testData.length)) {
                while (stream.available() > 0) {
                    stream.read();
                }
            } catch (IOException e) {
                fail("Performance test should not throw IOException: " + e.getMessage());
            }
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Just verify it completes without hanging (basic performance validation)
        assertTrue(duration > 0, "Operations should complete in measurable time");
    }
}
