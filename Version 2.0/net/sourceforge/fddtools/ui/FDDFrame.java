/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package net.sourceforge.fddtools.ui;

import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Program;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;

import java.util.Date;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.MatteBorder;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.cli.CommandLine;

import net.sourceforge.fddtools.internationalization.Messages;
import net.sourceforge.fddtools.model.DefaultFDDModel;
import net.sourceforge.fddtools.model.FDDElement;
import net.sourceforge.fddtools.model.Feature;
import net.sourceforge.fddtools.model.FeatureSet;
import net.sourceforge.fddtools.model.MajorFeatureSet;
import net.sourceforge.fddtools.model.Project;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.persistence.FDDXMLPersistence;
import net.sourceforge.fddtools.persistence.FDDXMLTokenizer;
import net.sourceforge.fddtools.persistence.FDDCSVTokenizer;
import net.sourceforge.fddtools.persistence.FDDIXMLFileReader;

/**
 * This is the main excutable class. Usage: java FDDFrame [options]
 * inputfile.csv
 *
 * @author Kenneth Jiang with extensive updates by James Hwong and Vernon Stinebaker
 */
public final class FDDFrame extends JFrame implements FDDOptionListener
{
    // > Internationalization keys
    private static final String ERROR_FILE_NOT_FOUND = "FDDFrame.ErrorFileNotFound";
    private static final String ERROR_PARSING_FILE = "FDDFrame.ErrorParsingFile";
    private static final String QUESTION_SAVE_CHANGES = "FDDFrame.QuestionSaveChanges";
    private static final String JOPTIONPANE_SAVEQUESTION_TITLE = "FDDFrame.SaveQuestion.Title";
    private static final String JTREE_ROOTNODE_CAPTION = "FDDFrame.JTreeRootNode.Caption";
    private static final String JFRAME_FDDOPTIONVIEW_TITLE = "FDDFrame.JFrameFDDOptionView.Title";
    private static final String EXTENSIONFILEFILTER_CSV_DESCRIPTION = "FDDFrame.ExtensionFileFilterCSV.Description";
    private static final String EXTENSIONFILEFILTER_FDD_DESCRIPTION = "FDDFrame.ExtensionFileFilterFDD.Description";
    private static final String MAJORFEATURESET_DEFAULT_TEXT = "FDDFrame.MajorFeatureSet.DefaultText";
    private static final String FEATURESET_DEFAULT_TEXT = "FDDFrame.FeatureSet.DefaultText";
    private static final String FEATURE_DEFAULT_TEXT = "FDDFrame.Feature.DefaultText";
    private static final String MENU_ROOT_CAPTION = "FDDFrame.MenuRoot.Caption";
    private static final String MENU_ADDMAJORFEATURESET_CAPTION = "FDDFrame.MenuAddMajorFeatureSet.Caption";
    private static final String MENU_EDITMAJORFEATURESET_CAPTION = "FDDFrame.MenuEditMajorFeatureSet.Caption";
    private static final String MENU_MAJORFEATURESET_CAPTION = "FDDFrame.MenuMajorFeatureSet.Caption";
    private static final String MENU_ADDFEATURESET_CAPTION = "FDDFrame.MenuAddFeatureSet.Caption";
    private static final String MENU_DELETEFEATURESET_CAPTION = "FDDFrame.MenuDeleteFeatureSet.Caption";
    private static final String MENU_EDITFEATURESET_CAPTION = "FDDFrame.MenuEditFeatureSet.Caption";
    private static final String MENU_FEATURESET_CAPTION = "FDDFrame.MenuFeatureSet.Caption";
    private static final String MENU_ADDFEATURE_CAPTION = "FDDFrame.MenuAddFeature.Caption";
    private static final String MENU_DELETEFEATURE_CAPTION = "FDDFrame.MenuDeleteFeature.Caption";
    private static final String MENU_EDITFEATURE_CAPTION = "FDDFrame.MenuEditFeature.Caption";
    private static final String MENU_FEATURE_CAPTION = "FDDFrame.MenuFeature.Caption";
    private static final String MENU_NEW = "FDDFrame.MenuNew.Caption";
    private static final String MENU_OPEN = "FDDFrame.MenuOpen.Caption";
    private static final String MENU_CLOSE = "FDDFrame.MenuClose.Caption";
    private static final String MENU_SAVE = "FDDFrame.MenuSave.Caption";
    private static final String MENU_SAVEAS = "FDDFrame.MenuSaveAs.Caption";
    private static final String MENU_IMPORT = "FDDFrame.MenuImport.Caption";
    private static final String MENU_PAGE_SETUP = "FDDFrame.MenuPageSetup.Caption";
    private static final String MENU_PRINT = "FDDFrame.MenuPrint.Caption";
    private static final String MENU_EXIT = "FDDFrame.MenuExit.Caption";
    private static final String MENU_FILE = "FDDFrame.MenuFile.Caption";
    private static final String MENU_UNDO = "FDDFrame.MenuUndo.Caption";
    private static final String MENU_REDO = "FDDFrame.MenuRedo.Caption";
    private static final String MENU_CUT = "FDDFrame.MenuCut.Caption";
    private static final String MENU_COPY = "FDDFrame.MenuCopy.Caption";
    private static final String MENU_PASTE = "FDDFrame.MenuPaste.Caption";
    private static final String MENU_DELETE = "FDDFrame.MenuDelete.Caption";
    private static final String MENU_OPTIONS = "FDDFrame.MenuOptions.Caption";
    private static final String MENU_EDIT = "FDDFrame.MenuEdit.Caption";
    private static final String MENU_HELP = "FDDFrame.MenuHelp.Caption";
    private static final String MENU_HELP_ABOUT = "FDDFrame.MenuAbout.Caption";
    private static final String MENU_HELP_CONTENT = "FDDFrame.MenuHelpContent.Caption";
    private static final String ERROR_INVALID_CUT = "FDDFrame.ErrorInvalidCut";
    private static final String ERROR_ILLEGAL_ACTION = "FDDFrame.ErrorIllegalAction";
    private static final String QUESTION_ARE_YOU_SURE = "FDDFrame.QuestionAreYouSure";
    private static final String JOPTIONPANE_DELETE_TITLE = "FDDFrame.JOptionPaneDelete.Title";
    private static final String ERROR_INVALID_DELETE = "FDDFrame.ErrorInvalidDelete";
    private static final String JBUTTON_NEW_TOOLTIP = "FDDFrame.JButtonNew.ToolTip";
    private static final String JBUTTON_OPEN_TOOLTIP = "FDDFrame.JButtonOpen.ToolTip";
    private static final String JBUTTON_SAVE_TOOLTIP = "FDDFrame.JButtonSave.ToolTip";
    private static final String JBUTTON_PRINT_TOOLTIP = "FDDFrame.JButtonPrint.ToolTip";
    private static final String JBUTTON_CUT_TOOLTIP = "FDDFrame.JButtonCut.ToolTip";
    private static final String JBUTTON_COPY_TOOLTIP = "FDDFrame.JButtonCopy.ToolTip";
    private static final String JBUTTON_PASTE_TOOLTIP = "FDDFrame.JButtonPaste.ToolTip";
    private static final String JBUTTON_ADD_TOOLTIP = "FDDFrame.JButtonAdd.ToolTip";
    private static final String JBUTTON_DELETE_TOOLTIP = "FDDFrame.JButtonDelete.ToolTip";
    private static final String JBUTTON_EDIT_TOOLTIP = "FDDFrame.JButtonEdit.ToolTip";
    private static final String ERROR_INSTANTIATION_EXCEPTION = "FDDFrame.ErrorInstantiationException";
    private static final String ERROR_ILLEGAL_ACCESS_DURING_COPY = "FDDFrame.ErrorIllegalAccessDuringCopy";
    // < End internationalization keys
    private JTree fddProjectTree;
    private FDDElement clipboard;
    private FDDCanvasView fddCanvasView;
    private String currentProject;
    private FDDOptionModel options;
    private JMenuItem fileSave;
    private JMenuItem fileSaveAs;
    private JPopupMenu projectMenu;
    private JPopupMenu mfsMenu;
    private JPopupMenu fsMenu;
    private JPopupMenu fMenu;
    public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

    /**
     * Creates a new FDDFrame object.
     *
     * @param commandLine
     *            Command line parameters.
     */
    public FDDFrame(final CommandLine commandLine)
    {
        this();

        if(commandLine.hasOption("s"))
        {
            this.options.setRootSectionName(commandLine.getOptionValue("s"));
        }

        try
        {
            if(commandLine.getArgs().length != 0)
            {
                FileReader csvReader = new FileReader(commandLine.getArgs()[0]);
                buildProjectTreeFromCSV();
            }
        }
        catch(FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(this,
                    Messages.getInstance().getMessage(ERROR_FILE_NOT_FOUND) +
                    '(' + commandLine.getArgs()[0] + ')');
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(this, Messages.getInstance().getMessage(ERROR_PARSING_FILE));
        }
    }

    /**
     * Creates an instance of FDDFrame.
     */
    public FDDFrame()
    {
        setDefaultLookAndFeelDecorated(true);
        macOSXRegistration();
        setVisible(false);
        addMenuBar();
        options = new FDDOptionModel();
        options.addFDDOptionListener(this);
        addWindowListener(this.windowAdapter);
        newProject();
    }

    public void macOSXRegistration()
    {
        if(MAC_OS_X)
        {
            try
            {
                Class osxAdapter = ClassLoader.getSystemClassLoader().loadClass("net.sourceforge.fddtools.ui.OSXAdapter");

                Class[] defArgs =
                {
                    FDDFrame.class
                };
                Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);
                if(registerMethod != null)
                {
                    Object[] args =
                    {
                        this
                    };
                    registerMethod.invoke(osxAdapter, args);
                }
                defArgs[0] = boolean.class;
                Method prefsEnableMethod = osxAdapter.getDeclaredMethod("enablePrefs", defArgs);
                if(prefsEnableMethod != null)
                {
                    Object args[] =
                    {
                        Boolean.TRUE
                    };
                    prefsEnableMethod.invoke(osxAdapter, args);
                }
            }
            catch(NoClassDefFoundError e)
            {
                System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
            }
            catch(ClassNotFoundException e)
            {
                System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
            }
            catch(Exception e)
            {
                System.err.println("Exception while loading the OSXAdapter:");
                e.printStackTrace();
            }
        }
    }
    private WindowAdapter windowAdapter = new WindowAdapter()
    {
        @Override
        public void windowClosing(final WindowEvent e)
        {
            quit();
        }
    };
    private ActionListener fileOpenListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            openProject();
        }
    };
    private ActionListener fileNewListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            fileNew();
        }
    };
    private ActionListener fileCloseListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            if((((DefaultFDDModel) FDDFrame.this.fddProjectTree.getModel()) != null) && ((DefaultFDDModel) FDDFrame.this.fddProjectTree.getModel()).dirty)
            {
                if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(FDDFrame.this,
                        Messages.getInstance().getMessage(QUESTION_SAVE_CHANGES),
                        Messages.getInstance().getMessage(JOPTIONPANE_SAVEQUESTION_TITLE),
                        JOptionPane.YES_NO_OPTION))
                {
                    persistModel(((DefaultFDDModel) FDDFrame.this.fddProjectTree.getModel()));
                }
            }
            if(null != FDDFrame.this.fddProjectTree)
            {
                closeCurrentProject();
                FDDFrame.this.fileSaveAs.setEnabled(false);
                FDDFrame.this.fileSave.setEnabled(false);
                FDDFrame.this.setTitle("FDD Tools");
            }
        }
    };
    private ActionListener fileImportListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            JTree projectTree = buildProjectTreeFromCSV();
            if(null != projectTree)
            {
                closeCurrentProject();
                newProject(projectTree);
                setVisible(true);
            }
        }
    };
    private ActionListener fileSaveListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            persistModel(((DefaultFDDModel) FDDFrame.this.fddProjectTree.getModel()));
        }
    };
    private ActionListener fileSavaAsListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            String fileName = currentProject;
            currentProject = null;
            persistModel(((DefaultFDDModel) FDDFrame.this.fddProjectTree.getModel()));
        //currentProject = fileName;
        }
    };
    private ActionListener filePageSetupListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
        }
    };
    private ActionListener filePrintListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            printCurrentNode();
        }
    };
    private ActionListener fileExitListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            quit();
        }
    };
    private ActionListener editUndoListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
        }
    };
    private ActionListener editRedoListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
        }
    };
    private ActionListener editCutListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            cutSelectedFDDElementNode();
        }
    };
    private ActionListener editCopyListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            copySelectedFDDElementNode();
        }
    };
    private ActionListener editPasteListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            pasteFDDElementNode();
        }
    };
    private ActionListener editDeleteListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            deleteSelectedFDDElementNode();
        }
    };
    private ActionListener editOptionsListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            options();
        }
    };
    private ActionListener helpHelpListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
        }
    };
    private ActionListener helpAboutListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            about();
        }
    };

    private void fileNew()
    {
        if((FDDFrame.this.fddProjectTree != null) && ((DefaultFDDModel) FDDFrame.this.fddProjectTree.getModel()).dirty)
        {
            int userChoice = JOptionPane.showConfirmDialog(FDDFrame.this,
                    Messages.getInstance().getMessage(QUESTION_SAVE_CHANGES),
                    Messages.getInstance().getMessage(JOPTIONPANE_SAVEQUESTION_TITLE),
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if(userChoice == JOptionPane.YES_OPTION)
            {
                persistModel(((DefaultFDDModel) FDDFrame.this.fddProjectTree.getModel()));
            }
            else if(userChoice == JOptionPane.CANCEL_OPTION)
            {
                return;
            }
        }
        closeCurrentProject();
        newProject();
        setVisible(true);
    }

    private void newProject()
    {
        ObjectFactory factory = new ObjectFactory();
        Program program = factory.createProgram();
        program.setName("New Program");
        JTree tree = new JTree(new DefaultTreeModel((TreeNode) program));
        newProject(tree);
    }

    private void newProject(JTree projectTree)
    {
        FDDFrame.this.setTitle("FDD Tools - " + Messages.getInstance().getMessage(JTREE_ROOTNODE_CAPTION));
        FDDFrame.this.fileSaveAs.setEnabled(true);
        FDDFrame.this.fileSave.setEnabled(true);
//        FDDFrame.this.fddProjectTree.getSelectionModel().setSelectionPath(
//            new TreePath(((DefaultFDDModel) FDDFrame.this.fddProjectTree.getModel()).getRoot()));
        projectTree.setRootVisible(true);
        displayProjectTree(projectTree);
    }

    private void openProject()
    {
        String[] extensions =
        {
            "xml"
        };
        String fileName = ExtensionFileFilter.getFileName(System.getProperty("user.dir"),
                Messages.getInstance().getMessage(EXTENSIONFILEFILTER_FDD_DESCRIPTION), extensions);

//        JTree projectTree = buildProjectTreeFromXML();
//        if (null != projectTree)
        {
            closeCurrentProject();

            FDDFrame.this.currentProject = fileName;
            FDDIXMLFileReader reader = new FDDIXMLFileReader(fileName);

            newProject(new JTree(new DefaultTreeModel((TreeNode) reader.getRootNode())));
            //newProject(projectTree);
            FDDFrame.this.setTitle("FDD Tools - " + fileName);
        }
        setVisible(true);
    }

    protected void quit()
    {
        if((FDDFrame.this.fddProjectTree != null) && ((DefaultFDDModel) FDDFrame.this.fddProjectTree.getModel()).dirty)
        {
            int userChoice = JOptionPane.showConfirmDialog(FDDFrame.this,
                    Messages.getInstance().getMessage(QUESTION_SAVE_CHANGES),
                    Messages.getInstance().getMessage(JOPTIONPANE_SAVEQUESTION_TITLE),
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if(userChoice == JOptionPane.YES_OPTION)
            {
                persistModel(((DefaultFDDModel) FDDFrame.this.fddProjectTree.getModel()));
                System.exit(0);
            }
            else if(userChoice == JOptionPane.NO_OPTION)
            {
                System.exit(0);
            }
            else
            {
                FDDFrame.this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            }
        }
        else
        {
            System.exit(0);
        }
    }

    private void closeCurrentProject()
    {
        Component[] components = this.getContentPane().getComponents();
        for(int i = 0; i < components.length; i++)
        {
            components[i] = null;
        }
        this.getContentPane().removeAll();

        fddProjectTree = null;
        fddCanvasView = null;
        currentProject = null;

        this.repaint();
    }

    private void printCurrentNode()
    {
        fddCanvasView.printImage();
    }

    protected void about()
    {
        AboutDialog about = new AboutDialog(FDDFrame.this);
        showComponentInCenter(about, FDDFrame.this.getBounds());
    }

    protected void options()
    {
        FDDOptionView optionsView = new FDDOptionView(options,
                Messages.getInstance().getMessage(JFRAME_FDDOPTIONVIEW_TITLE));
        showComponentInCenter(optionsView, FDDFrame.this.getBounds());
    }

    public void optionChanged(final FDDOptionEvent e)
    {
        fddCanvasView.setTextFont(options.getTextFont());
        fddCanvasView.repaint();
    }

    /**
     * Construct the fddProjectTree model from .csv input file.
     *
     * @param input
     *            Input file.
     */
    private JTree buildProjectTreeFromCSV()
    {
        JTree resultTree = null;
        FileReader csvReader = null;

        String[] extensions =
        {
            "csv"
        };
        String fileName = ExtensionFileFilter.getFileName(System.getProperty("user.dir"),
                Messages.getInstance().getMessage(EXTENSIONFILEFILTER_CSV_DESCRIPTION), extensions);

        if(fileName != null)
        {
            try
            {
                csvReader = new FileReader(fileName);
            }
            catch(FileNotFoundException e)
            {
                JOptionPane.showMessageDialog(this,
                        Messages.getInstance().getMessage(ERROR_FILE_NOT_FOUND));
            }

            try
            {
                FDDCSVTokenizer parser = new FDDCSVTokenizer(csvReader, this.options.getRootSectionName());
                FDDSequenceTreeBuilder builder = new FDDSequenceTreeBuilder();

                resultTree = builder.buildTree(parser);
                if(null == resultTree)
                {
                    JOptionPane.showMessageDialog(this,
                            Messages.getInstance().getMessage(ERROR_PARSING_FILE));
                }
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(this,
                        Messages.getInstance().getMessage(ERROR_PARSING_FILE));
            }
        }
        return resultTree;
    }

    /**
     * Construct the fddProjectTree model from .xml or .fdd input file.
     */
    private JTree buildProjectTreeFromXML()
    {
        JTree resultTree = null;
        String[] extensions =
        {
            "fdd", "xml"
        };
        String description = Messages.getInstance().getMessage(EXTENSIONFILEFILTER_FDD_DESCRIPTION);
        HashMap fileTypes = new HashMap();
        fileTypes.put(extensions, description);
        String fileName = ExtensionFileFilter.getFileName(System.getProperty("user.dir"), fileTypes,
                ExtensionFileFilter.LOAD);

        if(fileName != null)
        {
            try
            {
                FDDXMLTokenizer parser = new FDDXMLTokenizer(fileName);
                FDDSequenceTreeBuilder builder = new FDDSequenceTreeBuilder();

                resultTree = builder.buildTree(parser);
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(this,
                        Messages.getInstance().getMessage(ERROR_PARSING_FILE));
            }

            this.currentProject = fileName;
        }

        return resultTree;
    }

    /**
     * Create visual components for the whole FDD model.
     */
    private void displayProjectTree(final JTree projectTree)
    {
        ActionListener projectAddListener = new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                Object currentNode = fddProjectTree.getSelectionPath().getLastPathComponent();
                MajorFeatureSet newMajorFeatureSet = new MajorFeatureSet(
                        Messages.getInstance().getMessage(MAJORFEATURESET_DEFAULT_TEXT),
                        0, new Date(), "", (FDDElement) currentNode);
                addFDDElementNode(newMajorFeatureSet, (FDDElement) currentNode);
            }
        };

        ActionListener mfsAddListener = new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                Object currentNode = FDDFrame.this.fddProjectTree.getSelectionPath().getLastPathComponent();
                FeatureSet newFeatureSet = new FeatureSet(
                        Messages.getInstance().getMessage(FEATURESET_DEFAULT_TEXT),
                        0, new Date(), "", (FDDElement) currentNode);
                addFDDElementNode(newFeatureSet, (FDDElement) currentNode);
            }
        };

        ActionListener fsAddListener = new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                Object currentNode = FDDFrame.this.fddProjectTree.getSelectionPath().getLastPathComponent();
                Feature newFeature = new Feature(Messages.getInstance().getMessage(FEATURE_DEFAULT_TEXT),
                        0, new Date(), "", (FDDElement) currentNode);
                addFDDElementNode(newFeature, (FDDElement) currentNode);
            }
        };

        ActionListener nodeDeleteListener = new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                deleteSelectedFDDElementNode();
            }
        };

        ActionListener nodeEditListener = new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                editSelectedFDDElementNode();
            }
        };

        JSplitPane mainJSplitPane = new JSplitPane();

        this.fddProjectTree = projectTree;

        JScrollPane treePane = new JScrollPane(this.fddProjectTree);
        treePane.setWheelScrollingEnabled(true);

        this.fddCanvasView = new FDDCanvasView((FDDINode) fddProjectTree.getModel().getRoot(), this.options.getTextFont());
        JScrollPane fddViewPane = new JScrollPane(this.fddCanvasView);
        this.fddCanvasView.setOuterScrollPane(fddViewPane);
        fddViewPane.setWheelScrollingEnabled(true);
        fddViewPane.addComponentListener(this.fddCanvasView);

        mainJSplitPane.setLeftComponent(treePane);
        mainJSplitPane.setRightComponent(fddViewPane);
        mainJSplitPane.setOneTouchExpandable(true);
        getContentPane().add(menuToolBar(), BorderLayout.NORTH);
        getContentPane().add(mainJSplitPane, BorderLayout.CENTER);
        getContentPane().add(actionButtonPanel(), BorderLayout.SOUTH);

        DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
        selectionModel.addTreeSelectionListener(fddCanvasView);
        selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        fddProjectTree.setSelectionModel(selectionModel);

        projectMenu = new JPopupMenu(Messages.getInstance().getMessage(MENU_ROOT_CAPTION));
        JMenuItem projectAdd = new JMenuItem(Messages.getInstance().getMessage(MENU_ADDMAJORFEATURESET_CAPTION));
        JMenuItem projectEdit = new JMenuItem(Messages.getInstance().getMessage(MENU_EDITMAJORFEATURESET_CAPTION));
        projectMenu.add(projectAdd);
        projectMenu.add(projectEdit);
        projectAdd.addActionListener(projectAddListener);
        projectEdit.addActionListener(nodeEditListener);

        mfsMenu = new JPopupMenu(Messages.getInstance().getMessage(MENU_MAJORFEATURESET_CAPTION));
        JMenuItem mfsAdd = new JMenuItem(Messages.getInstance().getMessage(MENU_ADDFEATURESET_CAPTION));
        JMenuItem mfsDel = new JMenuItem(Messages.getInstance().getMessage(MENU_DELETEFEATURESET_CAPTION));
        JMenuItem mfsEdit = new JMenuItem(Messages.getInstance().getMessage(MENU_EDITFEATURESET_CAPTION));
        mfsMenu.add(mfsAdd);
        mfsMenu.add(mfsDel);
        mfsMenu.add(mfsEdit);
        mfsAdd.addActionListener(mfsAddListener);
        mfsDel.addActionListener(nodeDeleteListener);
        mfsEdit.addActionListener(nodeEditListener);

        fsMenu = new JPopupMenu(Messages.getInstance().getMessage(MENU_FEATURESET_CAPTION));
        JMenuItem fsAdd = new JMenuItem(Messages.getInstance().getMessage(MENU_ADDFEATURE_CAPTION));
        JMenuItem fsDel = new JMenuItem(Messages.getInstance().getMessage(MENU_DELETEFEATURE_CAPTION));
        JMenuItem fsEdit = new JMenuItem(Messages.getInstance().getMessage(MENU_EDITFEATURE_CAPTION));
        fsMenu.add(fsAdd);
        fsMenu.add(fsDel);
        fsMenu.add(fsEdit);
        fsAdd.addActionListener(fsAddListener);
        fsDel.addActionListener(nodeDeleteListener);
        fsEdit.addActionListener(nodeEditListener);

        fMenu = new JPopupMenu(Messages.getInstance().getMessage(MENU_FEATURE_CAPTION));
        JMenuItem fDel = new JMenuItem(Messages.getInstance().getMessage(MENU_DELETEFEATURE_CAPTION));
        JMenuItem fEdit = new JMenuItem(Messages.getInstance().getMessage(MENU_EDITFEATURE_CAPTION));
        fMenu.add(fDel);
        fMenu.add(fEdit);
        fDel.addActionListener(nodeDeleteListener);
        fEdit.addActionListener(nodeEditListener);

        MouseAdapter mouseAdapter = new java.awt.event.MouseAdapter()
        {
            public void mousePressed(final MouseEvent e)
            {
                if(e.isPopupTrigger())
                {
                    showTreeCtxMenu(e.getComponent(), e.getX(), e.getY());
                }
            }

            public void mouseReleased(final MouseEvent e)
            {
                mousePressed(e);
            }
        };
        this.fddProjectTree.addMouseListener(mouseAdapter);
    }

    private void persistModel(final DefaultFDDModel fddModel)
    {
        String fileName = null;

        if(null == this.currentProject)
        {
            String[] extensions =
            {
                "fdd", "xml"
            };
            String description = Messages.getInstance().getMessage(EXTENSIONFILEFILTER_FDD_DESCRIPTION);
            HashMap fileTypes = new HashMap();
            fileTypes.put(extensions, description);
            fileName = ExtensionFileFilter.getFileName(System.getProperty("user.dir"), fileTypes,
                    ExtensionFileFilter.SAVE);
            this.currentProject = fileName;
        }
        else
        {
            fileName = this.currentProject;
        }

        if(fileName != null)
        {
            FDDXMLPersistence xf = new FDDXMLPersistence();
            xf.store(fddModel, fileName);
            fddModel.dirty = false;
            this.setTitle("FDD Tools - " + this.currentProject);
        }
    }

    /**
     * Add menu to Frame
     */
    private void addMenuBar()
    {
        JMenuItem fileNew = new JMenuItem(Messages.getInstance().getMessage(MENU_NEW));
        fileNew.setMnemonic('N');
        fileNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        JMenuItem fileOpen = new JMenuItem(Messages.getInstance().getMessage(MENU_OPEN));
        fileOpen.setMnemonic('O');
        fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        JMenuItem fileClose = new JMenuItem(Messages.getInstance().getMessage(MENU_CLOSE));
        fileClose.setMnemonic('C');

        fileSave = new JMenuItem(Messages.getInstance().getMessage(MENU_SAVE));
        fileSave.setMnemonic(KeyEvent.VK_S);
        fileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        fileSaveAs = new JMenuItem(Messages.getInstance().getMessage(MENU_SAVEAS));
        fileSaveAs.setMnemonic('A');

        JMenuItem fileImport = new JMenuItem(Messages.getInstance().getMessage(MENU_IMPORT));
        fileImport.setMnemonic('I');

        JMenuItem filePageSetup = new JMenuItem(Messages.getInstance().getMessage(MENU_PAGE_SETUP));
        filePageSetup.setMnemonic('u');
        filePageSetup.setEnabled(false);

        JMenuItem filePrint = new JMenuItem(Messages.getInstance().getMessage(MENU_PRINT));
        filePrint.setMnemonic('P');
        filePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        JMenuItem fileExit = new JMenuItem(Messages.getInstance().getMessage(MENU_EXIT));
        fileExit.setMnemonic('x');

        JMenu fileMenu = new JMenu(Messages.getInstance().getMessage(MENU_FILE));
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(fileNew);
        fileMenu.add(fileOpen);
        fileMenu.add(fileClose);
        fileMenu.addSeparator();
        fileMenu.add(fileSave);
        fileMenu.add(fileSaveAs);
        fileMenu.add(fileImport);
        fileMenu.addSeparator();
        fileMenu.add(filePageSetup);
        fileMenu.add(filePrint);
        if(!MAC_OS_X)
        {
            fileMenu.addSeparator();
            fileMenu.add(fileExit);
        }

        JMenuItem editUndo = new JMenuItem(Messages.getInstance().getMessage(MENU_UNDO));
        editUndo.setMnemonic('U');
        editUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        editUndo.setEnabled(false);

        JMenuItem editRedo = new JMenuItem(Messages.getInstance().getMessage(MENU_REDO));
        editRedo.setMnemonic('R');
        editRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        editRedo.setEnabled(false);

        JMenuItem editCut = new JMenuItem(Messages.getInstance().getMessage(MENU_CUT));
        editCut.setMnemonic('t');
        editCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        editCut.setEnabled(true);

        JMenuItem editCopy = new JMenuItem(Messages.getInstance().getMessage(MENU_COPY));
        editCopy.setMnemonic('C');
        editCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        editCopy.setEnabled(true);

        JMenuItem editPaste = new JMenuItem(Messages.getInstance().getMessage(MENU_PASTE));
        editPaste.setMnemonic('P');
        editPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        editPaste.setEnabled(true);

        JMenuItem editDelete = new JMenuItem(Messages.getInstance().getMessage(MENU_DELETE));
        editDelete.setMnemonic('D');
        editDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        JMenuItem editOption = new JMenuItem(Messages.getInstance().getMessage(MENU_OPTIONS));
        editOption.setMnemonic('O');

        JMenu editMenu = new JMenu(Messages.getInstance().getMessage(MENU_EDIT));
        editMenu.setMnemonic('E');

        editMenu.add(editUndo);
        editMenu.add(editRedo);
        editMenu.addSeparator();
        editMenu.add(editCut);
        editMenu.add(editCopy);
        editMenu.add(editPaste);
        editMenu.addSeparator();
        editMenu.add(editDelete);

        if(!MAC_OS_X)
        {
            editMenu.addSeparator();
            editMenu.add(editOption);
        }

        JMenuItem helpHelp = new JMenuItem(Messages.getInstance().getMessage(MENU_HELP_CONTENT));
        helpHelp.setMnemonic('H');
        helpHelp.setAccelerator(KeyStroke.getKeyStroke("F1"));
        helpHelp.setEnabled(false);

        JMenuItem helpAbout = new JMenuItem(Messages.getInstance().getMessage(MENU_HELP_ABOUT));
        helpAbout.setMnemonic('A');

        JMenu helpMenu = new JMenu(Messages.getInstance().getMessage(MENU_HELP));
        helpMenu.setMnemonic('H');

        helpMenu.add(helpHelp);

        if(!MAC_OS_X)
        {
            helpMenu.add(helpAbout);
        }

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);

        this.setJMenuBar(menuBar);

        fileOpen.addActionListener(this.fileOpenListener);
        fileNew.addActionListener(this.fileNewListener);
        fileClose.addActionListener(this.fileCloseListener);
        fileImport.addActionListener(this.fileImportListener);
        fileSave.addActionListener(this.fileSaveListener);
        fileSaveAs.addActionListener(this.fileSavaAsListener);
        filePrint.addActionListener(this.filePrintListener);
        fileExit.addActionListener(this.fileExitListener);

        editCopy.addActionListener(this.editCopyListener);
        editCut.addActionListener(this.editCutListener);
        editPaste.addActionListener(this.editPasteListener);
        editDelete.addActionListener(this.editDeleteListener);
        editOption.addActionListener(this.editOptionsListener);

        helpAbout.addActionListener(this.helpAboutListener);
    }

    /**
     * method implement FDDOptionListener.
     *
     * @param e
     *            Option event object.
     */
    /**
     * @param c
     *            The component to be displayed.
     * @param parentRect
     *            The rectangle of which the component will be displayed in
     *            center
     */
    public static void showComponentInCenter(final Component c, final Rectangle parentRect)
    {
        Rectangle rect = c.getBounds();
        c.setBounds((int) (parentRect.getX() + (parentRect.getWidth() - rect.getWidth()) / 2),
                (int) (parentRect.getY() + (parentRect.getHeight() - rect.getHeight()) / 2), (int) (rect.getWidth()), (int) (rect.getHeight()));
        c.setVisible(true);
    }

    private void addFDDElementNode(final FDDElement newNode, final FDDElement parentNode)
    {
        FDDElementDialog editDialog = new FDDElementDialog(this, newNode);
        showComponentInCenter((Component) editDialog, this.getBounds());
//        editDialog.pack();
//        editDialog.setVisible(true);

        if(editDialog.accept)
        {
            ((DefaultFDDModel) FDDFrame.this.fddProjectTree.getModel()).insertNodeInto(
                    (MutableTreeNode) newNode, (MutableTreeNode) parentNode, ((MutableTreeNode) parentNode).getChildCount());
            fddProjectTree.scrollPathToVisible(fddProjectTree.getSelectionPath().pathByAddingChild(newNode));
            ((DefaultFDDModel) fddProjectTree.getModel()).dirty = true;
            fddCanvasView.reflow();
        }
        fddCanvasView.revalidate();
    }

    private void editSelectedFDDElementNode()
    {
        FDDElement currentNode = (FDDElement) fddProjectTree.getSelectionPath().getLastPathComponent();
        FDDElementDialog editDialog = new FDDElementDialog(this, currentNode);
        showComponentInCenter((Component) editDialog, this.getBounds());
        ((DefaultFDDModel) fddProjectTree.getModel()).dirty = true;
        fddCanvasView.repaint();
        fddCanvasView.revalidate();
    }

    private void cutSelectedFDDElementNode()
    {
        Object currentNode = fddProjectTree.getSelectionPath().getLastPathComponent();
        if(!(currentNode instanceof Project))
        {
            Object parentNode = fddProjectTree.getSelectionPath().getParentPath().getLastPathComponent();
            clipboard = (FDDElement) currentNode;
            TreePath parentPath = fddProjectTree.getSelectionPath().getParentPath();
            fddProjectTree.setSelectionPath(parentPath);
            ((DefaultFDDModel) fddProjectTree.getModel()).removeNodeFromParent((MutableTreeNode) currentNode);
            ((DefaultFDDModel) fddProjectTree.getModel()).dirty = true;
            fddCanvasView.reflow();
            fddCanvasView.revalidate();
        }
        else
        {
            JOptionPane.showMessageDialog(this, Messages.getInstance().getMessage(ERROR_INVALID_CUT));
        }
    }

    private void copySelectedFDDElementNode()
    {
        clipboard = (FDDElement) fddProjectTree.getSelectionPath().getLastPathComponent();
        fddCanvasView.reflow();
        fddCanvasView.revalidate();
    }

    private void pasteFDDElementNode()
    {
        if(clipboard != null)
        {
            Object parentNode = fddProjectTree.getSelectionPath().getLastPathComponent();
            try
            {
                FDDElement branch = copyBranch(clipboard);
                ((DefaultFDDModel) fddProjectTree.getModel()).addChildFDDElement(branch, (FDDElement) parentNode);
                fddProjectTree.setSelectionPath(fddProjectTree.getSelectionPath());
                ((DefaultFDDModel) fddProjectTree.getModel()).dirty = true;
                fddCanvasView.reflow();
                fddCanvasView.revalidate();
            }
            catch(IllegalArgumentException iae)
            {
                String elementName = clipboard.getClass().getName().substring(clipboard.getClass().getName().lastIndexOf(".") + 1);
                String parentName = parentNode.getClass().getName().substring(parentNode.getClass().getName().lastIndexOf(".") + 1);
                JOptionPane.showMessageDialog(this, Messages.getInstance().getMessage(ERROR_ILLEGAL_ACTION));
            // FIXME "Illegal action. Cannot insert " + elementName + " into " + parentName + ".");
            }
        }
    }

    private void deleteSelectedFDDElementNode()
    {
        Object currentNode = fddProjectTree.getSelectionPath().getLastPathComponent();
        if(!(currentNode instanceof Project))
        {
            if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
                    Messages.getInstance().getMessage(QUESTION_ARE_YOU_SURE),
                    Messages.getInstance().getMessage(JOPTIONPANE_DELETE_TITLE),
                    JOptionPane.YES_NO_OPTION))
            {
                TreePath parentPath = fddProjectTree.getSelectionPath().getParentPath();
                ((DefaultFDDModel) fddProjectTree.getModel()).removeNodeFromParent((MutableTreeNode) currentNode);
                fddProjectTree.setSelectionPath(parentPath);
                ((DefaultFDDModel) fddProjectTree.getModel()).dirty = true;
                fddCanvasView.reflow();
            }
            fddCanvasView.revalidate();
        }
        else
        {
            JOptionPane.showMessageDialog(this, Messages.getInstance().getMessage(ERROR_INVALID_DELETE));
        }
    }

    private void showTreeCtxMenu(final Component origin, final int x, final int y)
    {
        TreePath selPath = fddProjectTree.getPathForLocation(x, y);

        if(selPath != null)
        {
            fddProjectTree.setSelectionPath(selPath);
            Object currentElementNode = selPath.getLastPathComponent();

            if(currentElementNode instanceof Project)
            {
                projectMenu.show(origin, x, y);
            }
            else if(currentElementNode instanceof MajorFeatureSet)
            {
                mfsMenu.show(origin, x, y);
            }
            else if(currentElementNode instanceof FeatureSet)
            {
                fsMenu.show(origin, x, y);
            }
            else if(currentElementNode instanceof Feature)
            {
                fMenu.show(origin, x, y);
            }
        }
    }

    private JToolBar menuToolBar()
    {
        JToolBar mtb = new JToolBar();

        // The default toolbar handle on the Mac isn't very attractive
        // so this is a kludge to make things look a bit better.
        // A future release may consider a custom border
        if(MAC_OS_X)
        {
            mtb.setBorder(new MatteBorder(0, 4, 0, 0, Color.lightGray));
        }

        JButton newProjectButton = new JButton(new ImageIcon(getClass().getResource("images/new.png")));
        JButton openProjectButton = new JButton(new ImageIcon(getClass().getResource("images/open.png")));
        JButton saveButton = new JButton(new ImageIcon(getClass().getResource("images/save.png")));
        JButton printButton = new JButton(new ImageIcon(getClass().getResource("images/print.png")));
        JButton cutButton = new JButton(new ImageIcon(getClass().getResource("images/cut.png")));
        JButton copyButton = new JButton(new ImageIcon(getClass().getResource("images/copy.png")));
        JButton pasteButton = new JButton(new ImageIcon(getClass().getResource("images/paste.png")));

        newProjectButton.setToolTipText(Messages.getInstance().getMessage(JBUTTON_NEW_TOOLTIP));
        openProjectButton.setToolTipText(Messages.getInstance().getMessage(JBUTTON_OPEN_TOOLTIP));
        saveButton.setToolTipText(Messages.getInstance().getMessage(JBUTTON_SAVE_TOOLTIP));
        printButton.setToolTipText(Messages.getInstance().getMessage(JBUTTON_PRINT_TOOLTIP));
        cutButton.setToolTipText(Messages.getInstance().getMessage(JBUTTON_CUT_TOOLTIP));
        copyButton.setToolTipText(Messages.getInstance().getMessage(JBUTTON_COPY_TOOLTIP));
        pasteButton.setToolTipText(Messages.getInstance().getMessage(JBUTTON_PASTE_TOOLTIP));

        mtb.add(newProjectButton);
        mtb.add(openProjectButton);
        mtb.add(saveButton);
        mtb.addSeparator();
        mtb.add(printButton);
        mtb.addSeparator();
        mtb.add(cutButton);
        mtb.add(copyButton);
        mtb.add(pasteButton);
        mtb.addSeparator();

        newProjectButton.addActionListener(fileNewListener);
        openProjectButton.addActionListener(fileOpenListener);
        saveButton.addActionListener(fileSaveListener);
        printButton.addActionListener(filePrintListener);
        cutButton.addActionListener(editCutListener);
        copyButton.addActionListener(editCopyListener);
        pasteButton.addActionListener(editPasteListener);

        return mtb;
    }

    private JPanel actionButtonPanel()
    {
        JPanel bp = new JPanel();
        bp.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JButton addButton = new JButton(new ImageIcon(getClass().getResource("images/addButton.png")));
        JButton delButton = new JButton(new ImageIcon(getClass().getResource("images/delButton.png")));
        JButton editButton = new JButton(new ImageIcon(getClass().getResource("images/editButton.png")));
        addButton.setToolTipText(Messages.getInstance().getMessage(JBUTTON_ADD_TOOLTIP));
        delButton.setToolTipText(Messages.getInstance().getMessage(JBUTTON_DELETE_TOOLTIP));
        editButton.setToolTipText(Messages.getInstance().getMessage(JBUTTON_EDIT_TOOLTIP));
        bp.add(addButton);
        bp.add(delButton);
        bp.add(editButton);

        addButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                Object currentNode = fddProjectTree.getSelectionPath().getLastPathComponent();
                if(currentNode instanceof Project)
                {
                    MajorFeatureSet newMajorFeatureSet = new MajorFeatureSet(
                            Messages.getInstance().getMessage(MAJORFEATURESET_DEFAULT_TEXT),
                            0, new Date(), "", (FDDElement) currentNode);
                    addFDDElementNode(newMajorFeatureSet, (FDDElement) currentNode);
                }
                else if(currentNode instanceof MajorFeatureSet)
                {
                    FeatureSet newFeatureSet = new FeatureSet(
                            Messages.getInstance().getMessage(FEATURESET_DEFAULT_TEXT),
                            0, new Date(), "", (FDDElement) currentNode);
                    addFDDElementNode(newFeatureSet, (FDDElement) currentNode);
                }
                else if(currentNode instanceof FeatureSet)
                {
                    Feature newFeature = new Feature(
                            Messages.getInstance().getMessage(FEATURE_DEFAULT_TEXT),
                            0, new Date(), "", (FDDElement) currentNode);
                    addFDDElementNode(newFeature, (FDDElement) currentNode);
                }
            }
        });

        delButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                deleteSelectedFDDElementNode();
            }
        });

        editButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                editSelectedFDDElementNode();
            }
        });

        return bp;
    }

    private FDDElement copyBranch(FDDElement branchRootElement)
    {
        MutableTreeNode branchRootNode = null;
        try
        {
            branchRootNode = (MutableTreeNode) branchRootElement.getClass().newInstance();
            ((FDDElement) branchRootNode).setName(branchRootElement.getName());
            ((FDDElement) branchRootNode).setProgress(branchRootElement.getProgress());
            ((FDDElement) branchRootNode).setTargetMonth(branchRootElement.getTargetMonth());
            ((FDDElement) branchRootNode).setOwner(branchRootElement.getOwner());
            addChildren((FDDElement) branchRootNode, branchRootElement);
        }
        catch(InstantiationException ie)
        {
            JOptionPane.showMessageDialog(this, Messages.getInstance().getMessage(ERROR_INSTANTIATION_EXCEPTION) +
                    " " + branchRootElement.getClass().getName());
        }
        catch(IllegalAccessException iae)
        {
            JOptionPane.showMessageDialog(this, Messages.getInstance().getMessage(ERROR_ILLEGAL_ACCESS_DURING_COPY));
        }

        return (FDDElement) branchRootNode;
    }

    private void addChildren(FDDElement fddParentNode, FDDElement branchNode) throws InstantiationException, IllegalAccessException
    {
        for(int i = 0; i < branchNode.getSubFDDElementCount(); i++)
        {
            FDDElement childElement = branchNode.getFDDElementAt(i);
            {
                MutableTreeNode fddChildNode = (MutableTreeNode) childElement.getClass().newInstance();
                ((FDDElement) fddChildNode).setName(((FDDElement) childElement).getName());
                ((FDDElement) fddChildNode).setProgress(((FDDElement) childElement).getProgress());
                ((FDDElement) fddChildNode).setTargetMonth(((FDDElement) childElement).getTargetMonth());
                ((FDDElement) fddChildNode).setOwner(((FDDElement) childElement).getOwner());
                ((MutableTreeNode) fddParentNode).insert(fddChildNode, i);
                addChildren((FDDElement) fddChildNode, childElement);
            }
        }
    }
}