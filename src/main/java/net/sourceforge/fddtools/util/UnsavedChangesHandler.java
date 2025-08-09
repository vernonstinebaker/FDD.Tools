package net.sourceforge.fddtools.util;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Pure logic helper to coordinate unsaved-changes workflow.
 * Makes behavior testable without invoking UI components.
 */
public final class UnsavedChangesHandler {
    private UnsavedChangesHandler() {}

    public enum Decision { SAVE, DONT_SAVE, CANCEL }

    /**
     * Handle unsaved state and optionally run proceed.
     * @param isDirty whether there are unsaved changes
     * @param prompt supplier returning user decision (only called when dirty)
     * @param save action executed when decision == SAVE; returns true if save succeeded
     * @param proceed runnable executed when (not dirty) OR (decision == DONT_SAVE) OR (decision == SAVE && save succeeded)
     * @return true if navigation (proceed) occurred; false if cancelled or save failed
     */
    public static boolean handle(boolean isDirty,
                                  Supplier<Decision> prompt,
                                  BooleanSupplier save,
                                  Runnable proceed) {
        Objects.requireNonNull(prompt, "prompt");
        Objects.requireNonNull(save, "save");
        Objects.requireNonNull(proceed, "proceed");
        if (!isDirty) { proceed.run(); return true; }
        Decision d = prompt.get();
        if (d == null) return false;
        switch (d) {
            case SAVE -> { if (save.getAsBoolean()) { proceed.run(); return true; } return false; }
            case DONT_SAVE -> { proceed.run(); return true; }
            case CANCEL -> { return false; }
            default -> { return false; }
        }
    }
}
