package net.sourceforge.fddtools.command;

import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.model.FDDTreeNode;
import net.sourceforge.fddtools.util.ObjectCloner;
import com.nebulon.xml.fddi.Feature;
import java.util.List;

/** Paste (clone) a clipboard node under a target parent. */
public class PasteNodeCommand implements Command {
    private final FDDINode parent;
    private final FDDINode clipboardSource;
    private FDDINode pasted;
    private boolean executed;
    private final boolean resequenceFeatures;

    public PasteNodeCommand(FDDINode parent, FDDINode clipboardSource, boolean resequenceFeatures) {
        this.parent = parent;
        this.clipboardSource = clipboardSource;
        this.resequenceFeatures = resequenceFeatures;
    }

    @Override
    public void execute() {
        if (executed) return;
        pasted = (FDDINode) ObjectCloner.deepClone(clipboardSource);
        if (pasted == null) throw new IllegalStateException("Clipboard clone failed");
        if (resequenceFeatures) {
            List<Feature> features = pasted.getFeaturesForNode();
            for (Feature f : features) {
                f.setSeq(f.getNextSequence());
            }
        }
        parent.add(pasted);
        pasted.calculateProgress();
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) return;
        parent.removeChild(pasted);
        executed = false;
    }

    public FDDINode getPasted() { return pasted; }

    @Override
    public String description() { return "Paste " + clipboardSource.getName(); }
}
