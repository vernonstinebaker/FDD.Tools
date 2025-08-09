package net.sourceforge.fddtools.service;

import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.persistence.FDDIXMLFileReader;
import net.sourceforge.fddtools.persistence.FDDIXMLFileWriter;

import java.io.File;

/**
 * Handles creation, opening, and saving of project root nodes.
 * Thin service layer to decouple file IO from UI.
 */
public class ProjectFileService {
    private static final ProjectFileService INSTANCE = new ProjectFileService();
    public static ProjectFileService getInstance() { return INSTANCE; }
    private ProjectFileService() {}

    public FDDINode createNewRoot(String name) {
        ObjectFactory factory = new ObjectFactory();
        Program program = factory.createProgram();
        program.setName(name == null ? "New Program" : name);
        return (FDDINode) program;
    }

    public FDDINode open(String absolutePath) throws Exception {
        Object obj = FDDIXMLFileReader.read(absolutePath);
        if (obj instanceof FDDINode node) {
            return node;
        }
        throw new IllegalStateException("File did not contain valid FDDINode root: " + absolutePath);
    }

    public boolean save(FDDINode root, String absolutePath) throws Exception {
        if (root == null) throw new IllegalArgumentException("Root node is null");
        File file = new File(absolutePath);
        return FDDIXMLFileWriter.write(root, file.getAbsolutePath());
    }
}
