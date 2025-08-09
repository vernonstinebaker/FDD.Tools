package net.sourceforge.fddtools.command;

/** Basic reversible command. */
public interface Command {
    void execute();
    void undo();
    String description();
}
