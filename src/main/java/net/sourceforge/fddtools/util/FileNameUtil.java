package net.sourceforge.fddtools.util;

/**
 * Utility class for file name and path operations specific to FDD Tools.
 * 
 * Consolidates file path manipulation logic that was previously duplicated
 * across multiple UI classes (FDDMainWindowFX, FDDFileActions, ProjectLifecycleController).
 * 
 * @since 3.0.0
 */
public final class FileNameUtil {
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private FileNameUtil() {
        throw new AssertionError("Utility class - do not instantiate");
    }
    
    /**
     * Removes duplicate .fddi occurrences from a file path.
     * 
     * Collapses any repeated .fddi.fddi... suffixes at the end to a single .fddi
     * to prevent double extension issues when processing file paths.
     * 
     * @param path the file path to process
     * @return the path with duplicate .fddi suffixes removed, or null if input is null
     */
    public static String stripDuplicateFddi(String path) {
        if (path == null) return null;
        String lower = path.toLowerCase();
        if (!lower.contains(".fddi")) return path; // nothing to strip
        
        // Collapse any repeated .fddi.fddi... at end to single
        while (lower.endsWith(".fddi.fddi")) {
            path = path.substring(0, path.length() - 5); // remove one suffix
            lower = path.toLowerCase();
        }
        return path;
    }
    
    /**
     * Builds a default save file name from a display name.
     * 
     * Returns a base filename WITHOUT extension to prevent double extensions
     * in file dialogs. Handles special cases for default project names.
     * 
     * @param displayName the display name to process
     * @return a base filename without extension, never null
     */
    public static String buildDefaultSaveFileName(String displayName) {
        String base = displayName;
        if (base == null || base.isBlank() || 
            base.equalsIgnoreCase("New Program") || 
            base.equalsIgnoreCase("New Program.fddi")) {
            base = "New Program";
        }
        
        // Remove any existing .fddi extensions
        while (base.toLowerCase().endsWith(".fddi")) {
            base = base.substring(0, base.length() - 5);
        }
        return base;
    }
    
    /**
     * Ensures a file path has either .fddi or .xml extension.
     * 
     * Adds .fddi extension if the path doesn't already end with .fddi or .xml.
     * Used to ensure proper file extensions for FDD project files.
     * 
     * @param path the file path to process
     * @return the path with appropriate extension, or null if input is null
     */
    public static String ensureFddiOrXmlExtension(String path) {
        if (path == null) return null;
        String lower = path.toLowerCase();
        if (lower.endsWith(".fddi") || lower.endsWith(".xml")) {
            return path;
        }
        return path + ".fddi";
    }
}
