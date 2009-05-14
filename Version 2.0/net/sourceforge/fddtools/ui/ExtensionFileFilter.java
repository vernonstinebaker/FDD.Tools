package net.sourceforge.fddtools.ui;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * A FileFilter that lets you specify which file extensions will be displayed.
 * Also includes a static getFileName method that users can call to pop up a
 * JFileChooser for a set of file extensions.
 * <P>
 * Adapted from Sun SwingSet demo. Taken from Core Web Programming from Prentice
 * Hall and Sun Microsystems Press, http://www.corewebprogramming.com/. &copy;
 * 2001 Marty Hall and Larry Brown; may be freely used or adapted.
 */

public class ExtensionFileFilter extends FileFilter
{
    /**
     * Constant that indicates a "Open file" dialog
     */
    public static final int LOAD = 0;
    /**
     * Constant that indicates a "Save file" dialog
     */
    public static final int SAVE = 1;

    private String description;
    private boolean allowDirectories;
    private Hashtable extensionsTable = new Hashtable();
    private boolean allowAll = false;

    /**
     * Creates an instance of ExtensionFileFilter.
     * 
     * @param isDirectoriesAllowed
     *            If directory selection is allowed
     */
    public ExtensionFileFilter(final boolean isDirectoriesAllowed)
    {
        this.allowDirectories = isDirectoriesAllowed;
    }

    /**
     * Creates an instance of ExtensionFileFilter.
     */
    public ExtensionFileFilter()
    {
        this(true);
    }

    /**
     * Get the the selected filename from a file dialog.
     * 
     * @param initialDirectory
     *            The directory where the dialog starts.
     * @param description
     *            File type description
     * @param extension
     *            The extension filename
     * @return The selected filename
     */
    public static String getFileName(final String initialDirectory, final String description,
            final String extension)
    {
        String[] extensions = new String[]
        {extension};
        return (getFileName(initialDirectory, description, extensions, LOAD));
    }

    /**
     * Get the the selected filename from a file dialog
     * 
     * @param initialDirectory
     *            The directory where the dialog starts.
     * @param description
     *            File type description
     * @param extension
     *            The extension filename
     * @param mode
     *            The file dialog mode
     * @return The selected filename
     */
    public static String getFileName(final String initialDirectory, final String description,
            final String extension, final int mode)
    {
        String[] extensions = new String[]
        {extension};
        return (getFileName(initialDirectory, description, extensions, mode));
    }

    /**
     * Get the the selected filename from a file dialog
     * 
     * @param initialDirectory
     *            The directory where the dialog starts.
     * @param description
     *            File type description
     * @param extensions
     *            The extension filenames
     * @return The selected filename
     */
    public static String getFileName(final String initialDirectory, final String description,
            final String[] extensions)
    {
        return (getFileName(initialDirectory, description, extensions, LOAD));
    }

    /**
     * Get the the selected filename from a file dialog
     * 
     * @param initialDirectory
     *            The directory where the dialog starts.
     * @param description
     *            File type description
     * @param extensions
     *            The extension filenames
     * @param mode
     *            The file dialog mode
     * @return The selected filename
     */
    public static String getFileName(final String initialDirectory, final String description,
            final String[] extensions, final int mode)
    {
        HashMap fileTypes = new HashMap();
        fileTypes.put(extensions, description);
        return getFileName(initialDirectory, fileTypes, mode);
    }

    /**
     * Pops up a JFileChooser that lists files with the specified extensions. If
     * the mode is SAVE, then the dialog will have a Save button; otherwise, the
     * dialog will have an Open button. Returns a String corresponding to the
     * file's pathname, or null if Cancel was selected.
     * 
     * @param initialDirectory
     *            The directory where the dialog starts
     * @param fileTypes
     *            Selectable file types
     * @param mode
     *            The file dialog mode
     * @return The selected filename
     */
    public static String getFileName(final String initialDirectory, final HashMap fileTypes, final int mode)
    {
        JFileChooser chooser = new JFileChooser(initialDirectory);

        for (Iterator iterator = fileTypes.keySet().iterator(); iterator.hasNext();)
        {
            String[] extensions = (String[]) iterator.next();
            String description = (String) fileTypes.get(extensions);
            ExtensionFileFilter filter = new ExtensionFileFilter();
            filter.setDescription(description);
            for (int i = 0; i < extensions.length; i++)
            {
                String extension = extensions[i];
                filter.addExtension(extension, true);
            }
            chooser.addChoosableFileFilter(filter);
        }

        int selectVal;
        if (mode == SAVE)
        {
            selectVal = chooser.showSaveDialog(null);

        }
        else
        {
            selectVal = chooser.showOpenDialog(null);
        }
        if (selectVal == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = chooser.getSelectedFile();
            FileFilter currentFilter = chooser.getFileFilter();
            if (currentFilter.accept(selectedFile))
            {
                return selectedFile.getAbsolutePath();
            }
            else
            {
                String[] suffixes = (String[]) ((ExtensionFileFilter) currentFilter).getExtensions();
                File tmpFile = new File(selectedFile.getParentFile(), selectedFile.getName() + "."
                        + suffixes[0]);
                return tmpFile.getAbsolutePath();
            }
        }
        else
        {
            return (null);
        }
    }

    /**
     * Add specified extention to allowed list.
     * 
     * @param suffix
     *            The extension name to be added
     * @param caseInsensitive
     *            Whether the extension name is case insensitive
     */
    public final void addExtension(final String suffix, final boolean caseInsensitive)
    {
        String extension = suffix;
        if (caseInsensitive)
        {
            extension = extension.toLowerCase();
        }
        if (!extensionsTable.containsKey(extension))
        {
            extensionsTable.put(extension, new Boolean(caseInsensitive));
            if (extension.equals("*") || extension.equals("*.*") || extension.equals(".*"))
            {
                allowAll = true;
            }
        }
    }

    /**
     * Get allowed extensions.
     * 
     * @return Allowed extensions in a String array
     */
    public final String[] getExtensions()
    {
        Object[] keys = extensionsTable.keySet().toArray();
        String[] extensions = new String[keys.length];
        for (int i = 0; i < keys.length; i++)
        {
            extensions[i] = keys[i].toString();
        }

        return extensions;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean accept(final File file)
    {
        if (file.isDirectory())
        {
            return (allowDirectories);
        }
        if (allowAll)
        {
            return (true);
        }
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if ((dotIndex == -1) || (dotIndex == name.length() - 1))
        {
            return (false);
        }
        String extension = name.substring(dotIndex + 1);
        if (extensionsTable.containsKey(extension))
        {
            return (true);
        }
        Enumeration keys = extensionsTable.keys();
        while (keys.hasMoreElements())
        {
            String possibleExtension = (String) keys.nextElement();
            Boolean caseFlag = (Boolean) extensionsTable.get(possibleExtension);
            if ((caseFlag != null) && (caseFlag.equals(Boolean.FALSE))
                    && (possibleExtension.equalsIgnoreCase(extension)))
            {
                return (true);
            }
        }
        return (false);
    }

    /**
     * Description setter.
     * 
     * @param desc
     *            The new description
     */
    public final void setDescription(final String desc)
    {
        this.description = desc;
    }

    /**
     * {@inheritDoc}
     */
    public final String getDescription()
    {
        return (description);
    }
}