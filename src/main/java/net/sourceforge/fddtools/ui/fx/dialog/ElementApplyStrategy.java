package net.sourceforge.fddtools.ui.fx.dialog;

import net.sourceforge.fddtools.model.FDDINode;

/** Strategy for applying dialog field values back to a specific node type. */
public interface ElementApplyStrategy {
    /**
     * Apply changes to the supplied node.
     * @param node node being edited
     * @return true if apply succeeded (dialog may close) false to veto and keep dialog open
     */
    boolean apply(FDDINode node) throws Exception;
}
