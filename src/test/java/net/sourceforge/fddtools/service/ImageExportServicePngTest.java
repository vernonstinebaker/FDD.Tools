package net.sourceforge.fddtools.service;

import javafx.scene.canvas.Canvas;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import net.sourceforge.fddtools.testutil.FxTestUtil;

import java.io.File;
import java.nio.file.Files;
import java.util.zip.CRC32;

import static org.junit.jupiter.api.Assertions.*;

public class ImageExportServicePngTest {
    @BeforeAll
    static void initFx() {
        FxTestUtil.ensureStarted();
    }

    @Test
    void exportedPngHasValidSignatureAndIHDR() throws Exception {
    // Ensure larger timeout for this suite run (dynamic read each export call)
    System.setProperty("fdd.image.snapshot.timeout.seconds", "25");
    Canvas c = new Canvas(20,20); // Content drawing skipped to avoid FX queue contention
        File tmp = File.createTempFile("fdd_export",".png");
    // Call export directly (current thread not FX); service schedules snapshot and waits.
        ImageExportService.getInstance().export(c, tmp, "png");
        byte[] bytes = Files.readAllBytes(tmp.toPath());
        assertTrue(bytes.length > 50, "PNG should not be trivially small");
        // PNG signature
        byte[] sig = new byte[]{(byte)0x89,'P','N','G',0x0D,0x0A,0x1A,0x0A};
        for(int i=0;i<8;i++) assertEquals(sig[i], bytes[i], "Signature mismatch at index "+i);
        // IHDR length should be 13
        int ihdrLen = ((bytes[8]&0xFF)<<24)|((bytes[9]&0xFF)<<16)|((bytes[10]&0xFF)<<8)|(bytes[11]&0xFF);
        assertEquals(13, ihdrLen, "IHDR length must be 13");
        // CRC check IHDR
        int crcIndex = 8 + 4 + 4 + ihdrLen; // len + type + data
        CRC32 crc = new CRC32();
        crc.update(bytes,12,4+ihdrLen); // type+data
        long expected = ((bytes[crcIndex]&0xFFL)<<24)|((bytes[crcIndex+1]&0xFFL)<<16)|((bytes[crcIndex+2]&0xFFL)<<8)|(bytes[crcIndex+3]&0xFFL);
        assertEquals(expected, crc.getValue(), "IHDR CRC mismatch");
    }
}
