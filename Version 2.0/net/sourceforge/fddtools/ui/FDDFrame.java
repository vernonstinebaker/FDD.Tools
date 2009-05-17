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
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Subject;
import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.persistence.FDDXMLTokenizer;
import net.sourceforge.fddtools.persistence.FDDCSVTokenizer;
import net.sourceforge.fddtools.persistence.FDDIXMLFileReader;
import net.sourceforge.fddtools.persistence.FDDIXMLFileWriter;

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
    private static final String MENU_ADDPROGRAM_CAPTION = "FDDFrame.MenuAddProgram.Caption";
    private static final String MENU_EDITPROGRAM_CAPTION = "FDDFrame.MenuEditProgram.Caption";
    private static final String MENU_PROJECT_CAPTION = "FDDFrame.MenuProject.Caption";
    private static final String MENU_ADDPROJECT_CAPTION = "FDDFrame.MenuAddProject.Caption";
    private static final String MENU_EDITPROJECT_CAPTION = "FDDFrame.MenuEditProject.Caption";
    private static final String MENU_DELETEPROJECT_CAPTION = "FDDFrame.MenuDeleteProject.Caption";
    private static final String MENU_ASPECT_CAPTION = "FDDFrame.MenuAspect.Caption";
    private static final String MENU_ADDASPECT_CAPTION = "FDDFrame.MenuAddAspect.Caption";
    private static final String MENU_EDITASPECT_CAPTION = "FDDFrame.MenuEditAspect.Caption";
    private static final String MENU_DELETEASPECT_CAPTION = "FDDFrame.MenuDeleteAspect.Caption";
    private static final String MENU_SUBJECT_CAPTION = "FDDFrame.MenuSubject.Caption";
    private static final String MENU_ADDSUBJECT_CAPTION = "FDDFrame.MenuAddSubject.Caption";
    private static final String MENU_EDITSUBJECT_CAPTION = "FDDFrame.MenuEditSubject.Caption";
    private static final String MENU_DELETESUBJECT_CAPTION = "FDDFrame.MenuDeleteSubject.Caption";
    private static final String MENU_ACTIVITY_CAPTION = "FDDFrame.MenuActivity.Caption";
    private static final String MENU_ADDACTIVITY_CAPTION = "FDDFrame.MenuAddActivity.Caption";
    private static final String MENU_EDITACTIVITY_CAPTION = "FDDFrame.MenuEditActvity.Caption";
    private static final String MENU_DELETEACTIVITY_CAPTION = "FDDFrame.MenuDeleteActivity.Caption";
    private static final String MENU_FEATURE_CAPTION = "FDDFrame.MenuFeature.Caption";
    private static final String MENU_ADDFEATURE_CAPTION = "FDDFrame.MenuAddFeature.Caption";
    private static final String MENU_EDITFEATURE_CAPTION = "FDDFrame.MenuEditFeature.Caption";
    private static final String MENU_DELETEFEATURE_CAPTION = "FDDFrame.MenuDeleteFeature.Caption";
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
    private JTree projectTree;
    private FDDINode clipboard;
    private FDDCanvasView fddCanvasView;
    private String currentProject;
    private FDDOptionModel options;
    private JMenuItem fileSave;
    private JMenuItem fileSaveAs;
    private JPopupMenu programMenu;
    private JPopupMenu projectMenu;
    private JPopupMenu aspectMenu;
    private JPopupMenu subjectMenu;
    private JPopupMenu activityMenu;
    private JPopupMenu featureMenu;
    public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));
    private boolean modelDirty = false;

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
            if((projectTree.getModel() != null) && modelDirty)
            {
                if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(FDDFrame.this,
                        Messages.getInstance().getMessage(QUESTION_SAVE_CHANGES),
                        Messages.getInstance().getMessage(JOPTIONPANE_SAVEQUESTION_TITLE),
                        JOptionPane.YES_NO_OPTION))
                {
                    persistModel();
                }
            }
            if(projectTree != null)
            {
                closeCurrentProject();
                fileSaveAs.setEnabled(false);
                fileSave.setEnabled(false);
                setTitle("FDD Tools");
                newProject();
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
            persistModel();
//            persistModel(((DefaultFDDModel) projectTree.getModel()));
        }
    };

    private ActionListener fileSavaAsListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            String fileName = currentProject;
            currentProject = null;
            persistModel();
            currentProject = fileName;
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
            printSelectedElementNode();
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
            cutSelectedElementNode();
        }
    };
    private ActionListener editCopyListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            copySelectedElementNode();
        }
    };
    private ActionListener editPasteListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            pasetSelectedElementNode();
        }
    };
    private ActionListener editDeleteListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            deleteSelectedElementNode();
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
        if((projectTree != null) && modelDirty)
        {
            int userChoice = JOptionPane.showConfirmDialog(FDDFrame.this,
                    Messages.getInstance().getMessage(QUESTION_SAVE_CHANGES),
                    Messages.getInstance().getMessage(JOPTIONPANE_SAVEQUESTION_TITLE),
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if(userChoice == JOptionPane.YES_OPTION)
            {
                persistModel();
//                persistModel(((DefaultFDDModel) projectTree.getModel()));
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

    //@todo create on-open wizard dialog (select existing file or select root for new file)
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
        setTitle("FDD Tools - " + Messages.getInstance().getMessage(JTREE_ROOTNODE_CAPTION));
        fileSaveAs.setEnabled(true);
        fileSave.setEnabled(true);
        projectTree.setRootVisible(true);
        displayProjectTree(projectTree);
        validate();
        projectTree.setSelectionRow(0);
    }

    private void openProject()
    {
        String[] extensions =
        {
            "xml"
        };
        String fileName = ExtensionFileFilter.getFileName(System.getProperty("user.dir"),
                Messages.getInstance().getMessage(EXTENSIONFILEFILTER_FDD_DESCRIPTION), extensions);

        {
            closeCurrentProject();

            currentProject = fileName;
            FDDIXMLFileReader reader = new FDDIXMLFileReader(fileName);

            newProject(new JTree(new DefaultTreeModel((TreeNode) reader.getRootNode())));
            //newProject(projectTree);
            setTitle("FDD Tools - " + fileName);
        }
        setVisible(true);
    }

    protected void quit()
    {
        if((projectTree != null) && modelDirty)
        {
            int userChoice = JOptionPane.showConfirmDialog(FDDFrame.this,
                    Messages.getInstance().getMessage(QUESTION_SAVE_CHANGES),
                    Messages.getInstance().getMessage(JOPTIONPANE_SAVEQUESTION_TITLE),
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if(userChoice == JOptionPane.YES_OPTION)
            {
                persistModel();
                System.exit(0);
            }
            else if(userChoice == JOptionPane.NO_OPTION)
            {
                System.exit(0);
            }
            else
            {
                setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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

        projectTree = null;
        fddCanvasView = null;
        currentProject = null;

        this.repaint();
    }

    private void printSelectedElementNode()
    {
        fddCanvasView.printImage();
    }

    protected void about()
    {
        AboutDialog about = new AboutDialog(FDDFrame.this);
        showComponentInCenter(about, getBounds());
    }

    protected void options()
    {
        FDDOptionView optionsView = new FDDOptionView(options,
                Messages.getInstance().getMessage(JFRAME_FDDOPTIONVIEW_TITLE));
        showComponentInCenter(optionsView, getBounds());
    }

    public void optionChanged(final FDDOptionEvent e)
    {
        fddCanvasView.setTextFont(options.getTextFont());
        fddCanvasView.repaint();
    }

    /**
     * Construct the projectTree model from .csv input file.
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
     * Construct the projectTree model from .xml or .fdd input file.
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

    private void displayProjectTree(final JTree projectTree)
    {
        final ObjectFactory of = new ObjectFactory();

        ActionListener projectAddListener = new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                Object currentNode = projectTree.getSelectionPath().getLastPathComponent();
                Project project = of.createProject();
                project.setName(Messages.getInstance().getMessage(MAJORFEATURESET_DEFAULT_TEXT));
            }
        };

        ActionListener elementAddListener = new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                addFDDElementNode();
            }
        };

        ActionListener elementDeleteListener = new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                deleteSelectedElementNode();
            }
        };

        ActionListener elementEditListener = new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                editSelectedFDDElementNode();
            }
        };

        JSplitPane mainJSplitPane = new JSplitPane();

        this.projectTree = projectTree;

        JScrollPane treePane = new JScrollPane(this.projectTree);
        treePane.setWheelScrollingEnabled(true);

        this.fddCanvasView = new FDDCanvasView((FDDINode) this.projectTree.getModel().getRoot(), this.options.getTextFont());
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
        this.projectTree.setSelectionModel(selectionModel);

        programMenu = new JPopupMenu(Messages.getInstance().getMessage(MENU_ROOT_CAPTION));
        JMenuItem programProgramAdd = new JMenuItem(Messages.getInstance().getMessage(MENU_ADDPROGRAM_CAPTION));
        JMenuItem programProjectAdd = new JMenuItem(Messages.getInstance().getMessage(MENU_ADDPROJECT_CAPTION));
        JMenuItem programEdit = new JMenuItem(Messages.getInstance().getMessage(MENU_EDITPROGRAM_CAPTION));
        programMenu.add(programProgramAdd);
        programMenu.add(programProjectAdd);
        programMenu.add(programEdit);
        programProgramAdd.addActionListener(elementAddListener);
        programEdit.addActionListener(elementEditListener);

        projectMenu = new JPopupMenu(Messages.getInstance().getMessage(MENU_ROOT_CAPTION));
        JMenuItem projectAdd = new JMenuItem(Messages.getInstance().getMessage(MENU_ADDASPECT_CAPTION));
        JMenuItem projectEdit = new JMenuItem(Messages.getInstance().getMessage(MENU_EDITPROJECT_CAPTION));
        JMenuItem projectDelete = new JMenuItem(Messages.getInstance().getMessage(MENU_DELETEPROJECT_CAPTION));
        projectMenu.add(projectAdd);
        projectMenu.add(projectEdit);
        projectMenu.add(projectDelete);
        projectAdd.addActionListener(elementAddListener);
        projectEdit.addActionListener(elementEditListener);
        projectDelete.addActionListener(elementDeleteListener);

        aspectMenu = new JPopupMenu(Messages.getInstance().getMessage(MENU_ASPECT_CAPTION));
        JMenuItem aspectAdd = new JMenuItem(Messages.getInstance().getMessage(MENU_ADDSUBJECT_CAPTION));
        JMenuItem aspectEdit = new JMenuItem(Messages.getInstance().getMessage(MENU_EDITASPECT_CAPTION));
        JMenuItem aspectDelete = new JMenuItem(Messages.getInstance().getMessage(MENU_DELETEASPECT_CAPTION));
        aspectMenu.add(aspectAdd);
        aspectMenu.add(aspectEdit);
        aspectMenu.add(aspectDelete);
        aspectAdd.addActionListener(elementAddListener);
        aspectEdit.addActionListener(elementEditListener);
        aspectDelete.addActionListener(elementDeleteListener);

        subjectMenu = new JPopupMenu(Messages.getInstance().getMessage(MENU_SUBJECT_CAPTION));
        JMenuItem subjectAddMenuItem = new JMenuItem(Messages.getInstance().getMessage(MENU_ADDACTIVITY_CAPTION));
        JMenuItem subjectEditMenuItem = new JMenuItem(Messages.getInstance().getMessage(MENU_EDITSUBJECT_CAPTION));
        JMenuItem subjectDeleteMenuItem = new JMenuItem(Messages.getInstance().getMessage(MENU_DELETESUBJECT_CAPTION));
        subjectMenu.add(subjectAddMenuItem);
        subjectMenu.add(subjectEditMenuItem);
        subjectMenu.add(subjectDeleteMenuItem);
        subjectAddMenuItem.addActionListener(elementAddListener);
        subjectEditMenuItem.addActionListener(elementEditListener);
        subjectDeleteMenuItem.addActionListener(elementDeleteListener);

        activityMenu = new JPopupMenu(Messages.getInstance().getMessage(MENU_ACTIVITY_CAPTION));
        JMenuItem activityAddMenuItem = new JMenuItem(Messages.getInstance().getMessage(MENU_ADDFEATURE_CAPTION));
        JMenuItem activityEditMenuItem = new JMenuItem(Messages.getInstance().getMessage(MENU_EDITACTIVITY_CAPTION));
        JMenuItem activityDeleteMenuItem = new JMenuItem(Messages.getInstance().getMessage(MENU_DELETEACTIVITY_CAPTION));
        activityMenu.add(activityAddMenuItem);
        activityMenu.add(activityEditMenuItem);
        activityMenu.add(activityDeleteMenuItem);
        activityAddMenuItem.addActionListener(elementAddListener);
        activityEditMenuItem.addActionListener(elementEditListener);
        activityDeleteMenuItem.addActionListener(elementDeleteListener);

        featureMenu = new JPopupMenu(Messages.getInstance().getMessage(MENU_FEATURE_CAPTION));
        JMenuItem featureDeleteMenuItem = new JMenuItem(Messages.getInstance().getMessage(MENU_DELETEFEATURE_CAPTION));
        JMenuItem featureEditMenuItem = new JMenuItem(Messages.getInstance().getMessage(MENU_EDITFEATURE_CAPTION));
        featureMenu.add(featureEditMenuItem);
        featureMenu.add(featureDeleteMenuItem);
        featureEditMenuItem.addActionListener(elementEditListener);
        featureDeleteMenuItem.addActionListener(elementDeleteListener);

        MouseAdapter mouseAdapter = new java.awt.event.MouseAdapter()
        {
            @Override
            public void mousePressed(final MouseEvent e)
            {
                if(e.isPopupTrigger())
                {
                    showTreeCtxMenu(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e)
            {
                mousePressed(e);
            }
        };
        this.projectTree.addMouseListener(mouseAdapter);
    }

    private void persistModel()
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
            FDDIXMLFileWriter.write(projectTree.getModel().getRoot(), fileName);
//            FDDXMLPersistence xf = new FDDXMLPersistence();
//            xf.store(fddModel, fileName);
            modelDirty = false;
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

    public static void showComponentInCenter(final Component c, final Rectangle parentRect)
    {
        Rectangle rect = c.getBounds();
        c.setBounds((int) (parentRect.getX() + (parentRect.getWidth() - rect.getWidth()) / 2),
                (int) (parentRect.getY() + (parentRect.getHeight() - rect.getHeight()) / 2), (int) (rect.getWidth()), (int) (rect.getHeight()));
        c.setVisible(true);
    }

    private void addFDDElementNode()
    {
        FDDINode newNode = null;
        
        ObjectFactory of = new ObjectFactory();
        FDDINode currentNode = (FDDINode) projectTree.getSelectionPath().getLastPathComponent();

        if(currentNode instanceof Program)
        {
            newNode = of.createProject();
        }
        else if(currentNode instanceof Project)
        {
            newNode = of.createAspect();
        }
        else if(currentNode instanceof Aspect)
        {
            newNode = of.createSubject();
        }
        else if(currentNode instanceof Subject)
        {
            newNode = of.createActivity();
        }
        else if(currentNode instanceof Activity)
        {
            newNode = of.createFeature();
        }

        FDDElementDialog editDialog = new FDDElementDialog(this, newNode);
        showComponentInCenter((Component) editDialog, this.getBounds());

        if(editDialog.accept)
        {
            currentNode.add(newNode);
            TreeNode[] node = ((DefaultTreeModel) projectTree.getModel()).getPathToRoot(newNode);
            projectTree.scrollPathToVisible(projectTree.getSelectionPath().pathByAddingChild(newNode));
            projectTree.updateUI();
            modelDirty = true;
            fddCanvasView.reflow();
        }
        fddCanvasView.revalidate();
    }

    private void editSelectedFDDElementNode()
    {
        FDDINode currentNode = (FDDINode) projectTree.getSelectionPath().getLastPathComponent();
        FDDElementDialog editDialog = new FDDElementDialog(this, currentNode);
        showComponentInCenter((Component) editDialog, this.getBounds());
        projectTree.updateUI();
        modelDirty = true;
        fddCanvasView.repaint();
        fddCanvasView.revalidate();
    }

    private void cutSelectedElementNode()
    {
        Object selectedNode = projectTree.getSelectionPath().getLastPathComponent();
        clipboard = (FDDINode) selectedNode;
        deleteSelectedElementNode();
        projectTree.updateUI();
        modelDirty = true;
        fddCanvasView.reflow();
        fddCanvasView.revalidate();
    }

    private void copySelectedElementNode()
    {
        clipboard = (FDDINode) projectTree.getSelectionPath().getLastPathComponent();
        projectTree.updateUI();
        fddCanvasView.reflow();
        fddCanvasView.revalidate();
    }

    private void pasetSelectedElementNode()
    {
        Object selectedNode = projectTree.getSelectionPath().getLastPathComponent();
        if(clipboard != null && selectedNode != null)
        {
            try
            {
                ((FDDINode) selectedNode).add(clipboard);
                projectTree.updateUI();
                modelDirty = true;
                fddCanvasView.reflow();
                fddCanvasView.revalidate();
            }
            catch(ClassCastException cce)
            {
                String elementClass[] = clipboard.getClass().getName().split("\\.");
                String targetClass[] = selectedNode.getClass().getName().split("\\.");
                JOptionPane.showMessageDialog(this, "Invalid target location.\nCannot paste " + elementClass[elementClass.length - 1] + " below " + targetClass[targetClass.length - 1]);
            }
        }
    }

    private void deleteSelectedElementNode()
    {
        Object currentNode = projectTree.getSelectionPath().getLastPathComponent();
        //@todo should be able to delete Program as long as it's not the root
        if(!(currentNode instanceof Program))
        {
            if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
                    Messages.getInstance().getMessage(QUESTION_ARE_YOU_SURE),
                    Messages.getInstance().getMessage(JOPTIONPANE_DELETE_TITLE),
                    JOptionPane.YES_NO_OPTION))
            {
                TreePath parentPath = projectTree.getSelectionPath().getParentPath();
                projectTree.setSelectionPath(parentPath);
                Object parentNode = parentPath.getLastPathComponent();
                ((FDDINode) currentNode).setParent((FDDINode) parentNode);
                ((DefaultTreeModel) projectTree.getModel()).removeNodeFromParent((MutableTreeNode) currentNode);
                projectTree.updateUI();
                modelDirty = true;
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
        TreePath selPath = projectTree.getPathForLocation(x, y);

        if(selPath != null)
        {
            projectTree.setSelectionPath(selPath);
            Object currentElementNode = selPath.getLastPathComponent();

            if(currentElementNode instanceof Program)
            {
                programMenu.show(origin, x, y);
            }
            if(currentElementNode instanceof Project)
            {
                projectMenu.show(origin, x, y);
            }
            if(currentElementNode instanceof Aspect)
            {
                aspectMenu.show(origin, x, y);
            }
            else if(currentElementNode instanceof Subject)
            {
                subjectMenu.show(origin, x, y);
            }
            else if(currentElementNode instanceof Activity)
            {
                activityMenu.show(origin, x, y);
            }
            else if(currentElementNode instanceof Feature)
            {
                featureMenu.show(origin, x, y);
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

        JButton newProjectButton = new JButton(new ImageIcon(getClass().getResource("images/document-new.png")));
        JButton openProjectButton = new JButton(new ImageIcon(getClass().getResource("images/document-open.png")));
        JButton saveButton = new JButton(new ImageIcon(getClass().getResource("images/document-save.png")));
        JButton printButton = new JButton(new ImageIcon(getClass().getResource("images/document-print.png")));
        JButton cutButton = new JButton(new ImageIcon(getClass().getResource("images/edit-cut.png")));
        JButton copyButton = new JButton(new ImageIcon(getClass().getResource("images/edit-copy.png")));
        JButton pasteButton = new JButton(new ImageIcon(getClass().getResource("images/edit-paste.png")));

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
        JButton addButton = new JButton(new ImageIcon(getClass().getResource("images/list-add.png")));
        JButton delButton = new JButton(new ImageIcon(getClass().getResource("images/list-remove.png")));
        JButton editButton = new JButton(new ImageIcon(getClass().getResource("images/accessories-text-editor.png")));
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
                addFDDElementNode();
            }
        });

        delButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                deleteSelectedElementNode();
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
}