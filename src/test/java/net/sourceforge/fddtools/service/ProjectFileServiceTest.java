package net.sourceforge.fddtools.service;

import net.sourceforge.fddtools.model.FDDINode;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectFileServiceTest {

    @Test
    void createNewRootDefaultsNameWhenNull() {
        FDDINode node = ProjectFileService.getInstance().createNewRoot(null);
        assertNotNull(node.getName());
        assertTrue(node.getName().length() > 0);
    }

    @Test
    void saveWithNullRootThrows() {
        assertThrows(IllegalArgumentException.class, () -> ProjectFileService.getInstance().save(null, "/tmp/foo.fddi"));
    }

    @Test
    void openInvalidFileThrows() throws Exception {
        File temp = File.createTempFile("invalid", ".fddi");
        try (FileWriter fw = new FileWriter(temp)) { fw.write("<not-xml>"); }
        assertThrows(Exception.class, () -> ProjectFileService.getInstance().open(temp.getAbsolutePath()));
        // cleanup
        temp.delete();
    }
}
