# Removed Files

This document tracks files that have been removed from the project as part of the modernization effort.

## Files Removed

### 1. DeepCopy.java

- **Location**: `src/main/java/net/sourceforge/fddtools/util/DeepCopy.java`
- **Reason**: Deprecated utility class
- **Replacement**: `ObjectCloner.java` provides the same functionality with modern implementation
- **Date Removed**: December 2024

### 2. OSXAdapter.java

- **Location**: `src/main/java/net/sourceforge/fddtools/platform/OSXAdapter.java`
- **Reason**: Used deprecated Apple EAWT API
- **Replacement**: `ModernMacOSHandler.java` uses Java 9+ Desktop API
- **Date Removed**: December 2024

## Notes

These files should be deleted from the repository. They have been replaced with modern implementations that provide the same functionality without deprecation warnings.

To remove these files from your local repository:

```bash
# Remove the deprecated files
rm src/main/java/net/sourceforge/fddtools/util/DeepCopy.java
rm src/main/java/net/sourceforge/fddtools/platform/OSXAdapter.java

# Stage the deletions
git rm src/main/java/net/sourceforge/fddtools/util/DeepCopy.java
git rm src/main/java/net/sourceforge/fddtools/platform/OSXAdapter.java

# Commit the changes
git commit -m "Remove deprecated DeepCopy and OSXAdapter classes"
```
