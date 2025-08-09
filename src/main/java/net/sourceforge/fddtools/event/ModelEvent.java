package net.sourceforge.fddtools.event;

import net.sourceforge.fddtools.model.FDDINode;

/** Marker sealed interface for simple model change events. */
public sealed interface ModelEvent permits ModelEvent.NodeAdded, ModelEvent.NodeRemoved, ModelEvent.NodeUpdated, ModelEvent.ProjectReplaced {
    record NodeAdded(FDDINode parent, FDDINode node) implements ModelEvent {}
    record NodeRemoved(FDDINode parent, FDDINode node) implements ModelEvent {}
    record NodeUpdated(FDDINode node) implements ModelEvent {}
    record ProjectReplaced(FDDINode oldRoot, FDDINode newRoot) implements ModelEvent {}
}
