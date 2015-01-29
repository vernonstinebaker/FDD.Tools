/**
 * A FileFilter that lets you specify which file extensions will be displayed.
 * Also includes a static getFileName method that users can call to pop up a
 * JFileChooser for a set of file extensions.
 * <P>
 * Adapted from Sun SwingSet demo. Taken from Core Web Programming from Prentice
 * Hall and Sun Microsystems Press, http://www.corewebprogramming.com/. &copy;
 * 2001 Marty Hall and Larry Brown; may be freely used or adapted.
 */

package net.sourceforge.fddtools.ui;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter
{
    public static final int LOAD = 0;
    public static final int SAVE = 1;
    private String description;
    private boolean allowDirectories;
    private Hashtable<String, Boolean> extensionsTable = new Hashtable<String, Boolean>();
    private boolean allowAll = false;

    public ExtensionFileFilter(final boolean isDirectoriesAllowed)
    {
        this.allowDirectories = isDirectoriesAllowed;
    }

    public ExtensionFileFilter()
    {
        this(true);
    }

    public static String getFileName(final String initialDirectory, final String description,
            final String extension)
    {
        String[] extensions = new String[]
        {extension};
        return (getFileName(initialDirectory, description, extensions, LOAD));
    }

    public static String getFileName(final String initialDirectory, final String description,
            final String extension, final int mode)
    {
        String[] extensions = new String[]
        {extension};
        return (getFileName(initialDirectory, description, extensions, mode));
    }

    public static String getFileName(final String initialDirectory, final String description,
            final String[] extensions)
    {
        return (getFileName(initialDirectory, description, extensions, LOAD));
    }

    public static String getFileName(final String initialDirectory, final String description,
            final String[] extensions, final int mode)
    {
        HashMap<String[], String> fileTypes = new HashMap<String[], String>();
        fileTypes.put(extensions, description);
        return getFileName(initialDirectory, fileTypes, mode);
    }

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

    public final void setDescription(final String desc)
    {
        this.description = desc;
    }

    public final String getDescription()
    {
        return (description);
    }
}