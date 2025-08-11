package net.sourceforge.fddtools.service;

import org.junit.jupiter.api.Test;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.File;
import java.nio.file.Files;
import java.util.zip.CRC32;

import static org.junit.jupiter.api.Assertions.*;

public class ImageExportServicePngTest {
    @Test
    void encodedPngHasValidSignatureAndIHDR() throws Exception {
        // Prepare a WritableImage directly (no FX snapshot). Fill a few pixels for non-trivial data.
        WritableImage wi = new WritableImage(20,20);
        var pw = wi.getPixelWriter();
        for(int y=0;y<20;y++){
            for(int x=0;x<20;x++){
                pw.setColor(x,y, (x+y)%2==0 ? Color.rgb(100,150,200,0.8) : Color.rgb(30,60,90,0.4));
            }
        }
        File tmp = File.createTempFile("fdd_export",".png");
        ImageExportService.getInstance().encodePng(wi, tmp);
    byte[] bytes = Files.readAllBytes(tmp.toPath());
    assertTrue(bytes.length >= 80, "PNG should contain expected structure (length="+bytes.length+")");
    // Ensure IDAT chunk exists
    String asString = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
    assertTrue(asString.contains("IDAT"), "PNG missing IDAT chunk marker");
        byte[] sig = new byte[]{(byte)0x89,'P','N','G',0x0D,0x0A,0x1A,0x0A};
        for(int i=0;i<8;i++) assertEquals(sig[i], bytes[i], "Signature mismatch at index "+i);
        int ihdrLen = ((bytes[8]&0xFF)<<24)|((bytes[9]&0xFF)<<16)|((bytes[10]&0xFF)<<8)|(bytes[11]&0xFF);
        assertEquals(13, ihdrLen, "IHDR length must be 13");
        int crcIndex = 8 + 4 + 4 + ihdrLen;
        CRC32 crc = new CRC32();
        crc.update(bytes,12,4+ihdrLen);
        long expected = ((bytes[crcIndex]&0xFFL)<<24)|((bytes[crcIndex+1]&0xFFL)<<16)|((bytes[crcIndex+2]&0xFFL)<<8)|(bytes[crcIndex+3]&0xFFL);
        assertEquals(expected, crc.getValue(), "IHDR CRC mismatch");
    }
}
