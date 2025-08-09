package net.sourceforge.fddtools.util;

import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecentFilesServiceTest {
    private File temp1, temp2, temp3, temp4, temp5, temp6, temp7, temp8, temp9, temp10, temp11;

    @BeforeEach
    void setup() throws Exception {
        // Clear any prior state
        RecentFilesService.getInstance().clear();
        temp1 = File.createTempFile("rfs1", ".tmp");
        temp2 = File.createTempFile("rfs2", ".tmp");
        temp3 = File.createTempFile("rfs3", ".tmp");
        temp4 = File.createTempFile("rfs4", ".tmp");
        temp5 = File.createTempFile("rfs5", ".tmp");
        temp6 = File.createTempFile("rfs6", ".tmp");
        temp7 = File.createTempFile("rfs7", ".tmp");
        temp8 = File.createTempFile("rfs8", ".tmp");
        temp9 = File.createTempFile("rfs9", ".tmp");
        temp10 = File.createTempFile("rfs10", ".tmp");
        temp11 = File.createTempFile("rfs11", ".tmp");
    }

    @AfterEach
    void tearDown() {
        temp1.delete(); temp2.delete(); temp3.delete(); temp4.delete(); temp5.delete(); temp6.delete(); temp7.delete(); temp8.delete(); temp9.delete(); temp10.delete(); temp11.delete();
        RecentFilesService.getInstance().clear();
    }

    @Test
    void addsAndDedupesAndCapsAt10() {
        RecentFilesService svc = RecentFilesService.getInstance();
        svc.addRecentFile(temp1.getAbsolutePath());
        svc.addRecentFile(temp2.getAbsolutePath());
        svc.addRecentFile(temp3.getAbsolutePath());
        svc.addRecentFile(temp4.getAbsolutePath());
        svc.addRecentFile(temp5.getAbsolutePath());
        svc.addRecentFile(temp6.getAbsolutePath());
        svc.addRecentFile(temp7.getAbsolutePath());
        svc.addRecentFile(temp8.getAbsolutePath());
        svc.addRecentFile(temp9.getAbsolutePath());
        svc.addRecentFile(temp10.getAbsolutePath());
        svc.addRecentFile(temp11.getAbsolutePath()); // pushes oldest out

        List<String> recents = svc.getRecentFiles();
        assertEquals(10, recents.size(), "Should cap at 10 entries");
        assertFalse(recents.contains(temp1.getAbsolutePath()), "Oldest should be removed");
        assertEquals(temp11.getAbsolutePath(), recents.get(0), "Most recent first");

        // Re-add existing -> should move to front
        svc.addRecentFile(temp5.getAbsolutePath());
        recents = svc.getRecentFiles();
        assertEquals(temp5.getAbsolutePath(), recents.get(0));
    }

    @Test
    void ignoresMissingFilesAndPrunes() throws Exception {
        RecentFilesService svc = RecentFilesService.getInstance();
        svc.addRecentFile(temp1.getAbsolutePath());
        svc.addRecentFile(temp2.getAbsolutePath());
        Files.deleteIfExists(temp1.toPath());
        List<String> recents = svc.getRecentFiles();
        assertEquals(1, recents.size(), "Missing file should be pruned");
        assertEquals(temp2.getAbsolutePath(), recents.get(0));
    }
}
