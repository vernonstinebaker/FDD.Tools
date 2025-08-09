package net.sourceforge.fddtools.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies ProjectService precondition exceptions. */
public class ProjectServiceExceptionTest {

    @Test
    void saveWithoutProjectThrows() {
        ProjectService svc = ProjectService.getInstance();
        svc.clear();
        assertThrows(IllegalStateException.class, svc::save);
    }

    @Test
    void saveWithoutPathThrows() throws Exception {
        ProjectService svc = ProjectService.getInstance();
        svc.clear();
        svc.newProject("Unsaved");
        assertThrows(IllegalStateException.class, svc::save);
    }

    @Test
    void saveAsWithoutProjectThrows() {
        ProjectService svc = ProjectService.getInstance();
        svc.clear();
        assertThrows(IllegalStateException.class, () -> svc.saveAs("/tmp/nowhere.fddi"));
    }
}
