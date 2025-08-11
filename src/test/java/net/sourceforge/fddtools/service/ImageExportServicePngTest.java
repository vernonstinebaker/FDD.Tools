package net.sourceforge.fddtools.service;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.CRC32;

import static org.junit.jupiter.api.Assertions.*;

public class ImageExportServicePngTest {
    @BeforeAll
    static void initFx() throws Exception {
        if (Platform.isFxApplicationThread()) return; // already running in FX
        try {
            // Attempt a benign call to detect initialized toolkit
            Platform.runLater(() -> {});
            return; // succeeded -> toolkit active
        } catch (IllegalStateException ignored) { }
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await();
    }

    @Test
    void exportedPngHasValidSignatureAndIHDR() throws Exception {
        Canvas c = new Canvas(20,20);
        var g = c.getGraphicsContext2D();
        g.setFill(Color.RED); g.fillRect(0,0,20,20);
        File tmp = File.createTempFile("fdd_export",".png");

        // Run export on FX thread so ImageExportService uses fast path (avoids latch timeout in headless CI)
    AtomicReference<Throwable> failure = new AtomicReference<>();
    CountDownLatch latch = new CountDownLatch(1);
    int serviceTimeout = Integer.getInteger("fdd.image.snapshot.timeout.seconds", 12);
    int waitSeconds = Math.max(8, serviceTimeout + 3); // allow a little beyond service timeout
        Platform.runLater(() -> {
            try {
                ImageExportService.getInstance().export(c, tmp, "png");
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                latch.countDown();
            }
        });
    boolean finished = latch.await(waitSeconds, TimeUnit.SECONDS);
    assertTrue(finished, "Export did not finish on FX thread within "+waitSeconds+"s");
        if (failure.get() != null) {
            if (failure.get() instanceof Exception e) throw e;
            throw new RuntimeException(failure.get());
        }
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
