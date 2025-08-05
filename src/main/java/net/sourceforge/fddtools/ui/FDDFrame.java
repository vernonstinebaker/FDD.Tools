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
import net.sourceforge.fddtools.internationalization.Messages;
import com.nebulon.xml.fddi.Program;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Subject;
import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Feature;
import java.util.List;
import java.util.ResourceBundle;
import net.sourceforge.fddtools.model.FDDINode;
import net.sourceforge.fddtools.persistence.FDDCSVImportReader;
import net.sourceforge.fddtools.persistence.FDDXMLImportReader;
import net.sourceforge.fddtools.persistence.FDDIXMLFileReader;
import net.sourceforge.fddtools.persistence.FDDIXMLFileWriter;
import net.sourceforge.fddtools.ui.bridge.DialogBridge;
import net.sourceforge.fddtools.util.DeepCopy;
import net.sourceforge.fddtools.util.FileUtility;

public final class FDDFrame extends JFrame implements FDDOptionListener
{

    private JTree projectTree;
    private FDDINode clipboard;
    private FDDCanvasView fddCanvasView;
    private String currentProject;
    private FDDOptionModel options;
    private JMenuItem fileSave;
    private JMenuItem fileSaveAs;
    private JPopupMenu programMenu;
    private JMenuItem programProgramAdd;
    private JMenuItem programProjectAdd;
    private JPopupMenu projectMenu;
    private JPopupMenu aspectMenu;
    private JPopupMenu subjectMenu;
    private JPopupMenu activityMenu;
    private JPopupMenu featureMenu;
    private static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));
    private boolean modelDirty = false;
    private boolean uniqueNodeVersion = false;

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
            // Use modern macOS integration (Java 9+)
            try {
                System.out.println("Setting up modern macOS handlers...");
                boolean success = ModernMacOSHandler.setupMacOSHandlers(this);
                if (success) {
                    System.out.println("Modern macOS handlers set up successfully");
                } else {
                    System.out.println("Some macOS handlers could not be set");
                }
            } catch (Exception e) {
                System.err.println("Failed to set up macOS handlers: " + e.getMessage());
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

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            openProject();
        }
    };
    private ActionListener fileNewListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            fileNew();
        }
    };
    private ActionListener fileCloseListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            saveChangesDialog();
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

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            String[] extensions =
            {
                "csv", "fdd", "xml"
            };
            String description = Messages.getInstance().getMessage(Messages.EXTENSIONFILEFILTER_FDD_DESCRIPTION);
            HashMap<String[], String> fileTypes = new HashMap<String[], String>();
            fileTypes.put(extensions, description);
            String fileName = ExtensionFileFilter.getFileName(System.getProperty("user.home"), fileTypes,
                    ExtensionFileFilter.LOAD);

            if(fileName != null)
            {
                Project project = null;

                try
                {
                    String fileType = FileUtility.getFileType(fileName);
                    if(fileType != null)
                    {
                        if(fileType.equals("csv"))
                        {
                            project = buildProjectTreeFromCSV(fileName);
                        }
                        else if(fileType.equals("fdd"))
                        {
                            project = buildProjectTreeFromXML(fileName);
                        }

                        closeCurrentProject();
                        newProject(new JTree(new DefaultTreeModel((TreeNode) project)));
                        setTitle("FDD Tools - " + fileName);
                        setVisible(true);
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(FDDFrame.this, Messages.getInstance().getMessage(Messages.ERROR_PARSING_FILE));
                    }
                }
                catch(Exception ioe)
                {
                    JOptionPane.showMessageDialog(FDDFrame.this, Messages.getInstance().getMessage(Messages.ERROR_PARSING_FILE));
                }
            }
        }
    };
    private ActionListener fileSaveListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            persistModel();
            modelDirty = false;
        }
    };
    private ActionListener fileSavaAsListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            String fileName = currentProject;
            currentProject = null;
            persistModel();
            currentProject = fileName;
            modelDirty = false;
        }
    };
    // Unused listener - commented out to remove warning
    // private ActionListener filePageSetupListener = new ActionListener()
    // {
    //     @Override
    //     public void actionPerformed(final ActionEvent e)
    //     {
    //     }
    // };
    private ActionListener filePrintListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            printSelectedElementNode();
        }
    };
    private ActionListener fileExitListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            quit();
        }
    };
    private ActionListener editCutListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            cutSelectedElementNode();
        }
    };
    private ActionListener editCopyListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            copySelectedElementNode();
        }
    };
    private ActionListener editPasteListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            pasetSelectedElementNode();
        }
    };
    private ActionListener editDeleteListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            deleteSelectedElementNode();
        }
    };
    private ActionListener editOptionsListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            options();
        }
    };
    private ActionListener helpAboutListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            about();
        }
    };

    private void fileNew()
    {
        saveChangesDialog();
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
        setTitle("FDD Tools - " + Messages.getInstance().getMessage(Messages.JTREE_ROOTNODE_CAPTION));
        fileSaveAs.setEnabled(true);
        fileSave.setEnabled(true);
        projectTree.setRootVisible(true);
        displayProjectTree(projectTree);
        validate();
        projectTree.setSelectionRow(0);
    }

    private void openProject()
    {
        saveChangesDialog();
        String[] extensions =
        {
            "fddi",
            "xml"
        };
        String fileName = ExtensionFileFilter.getFileName(System.getProperty("user.home"),
                Messages.getInstance().getMessage(Messages.EXTENSIONFILEFILTER_FDD_DESCRIPTION), extensions);

        if(fileName != null)
        {
            closeCurrentProject();

            currentProject = fileName;
            newProject(new JTree(new DefaultTreeModel((TreeNode) FDDIXMLFileReader.read(fileName))));
            setTitle("FDD Tools - " + fileName);
        }
        setVisible(true);
    }

    protected boolean quit()
    {
        saveChangesDialog();
        System.exit(0);
        return true; // This won't be reached, but needed for compilation
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

    protected boolean about()
    {
        System.out.println("DEBUG: about() method called");
        
        // Use JavaFX About dialog through the bridge
        DialogBridge.showAboutDialog(this);
        
        // Return true to indicate we've handled the event
        return true;
    }

    protected void options()
    {
        FDDOptionView optionsView = new FDDOptionView(options,
                Messages.getInstance().getMessage(Messages.JFRAME_FDDOPTIONVIEW_TITLE));
        showComponentInCenter(optionsView, getBounds());
    }

    @Override
    public void optionChanged(final FDDOptionEvent e)
    {
        fddCanvasView.setTextFont(options.getTextFont());
        fddCanvasView.repaint();
    }

    private Project buildProjectTreeFromCSV(String fileName) throws Exception
    {
        return FDDCSVImportReader.read(fileName);
    }

    private Project buildProjectTreeFromXML(String fileName) throws Exception
    {
        return FDDXMLImportReader.read(fileName);
    }

    private void displayProjectTree(final JTree projectTree)
    {
        ActionListener elementAddListener = new ActionListener()
        {

            @Override
            public void actionPerformed(final ActionEvent e)
            {
                addFDDElementNode(e);
            }
        };

        ActionListener elementDeleteListener = new ActionListener()
        {

            @Override
            public void actionPerformed(final ActionEvent e)
            {
                deleteSelectedElementNode();
            }
        };

        ActionListener elementEditListener = new ActionListener()
        {

            @Override
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
        projectTree.setSelectionModel(selectionModel);

        programMenu = new JPopupMenu(Messages.getInstance().getMessage(Messages.MENU_ROOT_CAPTION));
        programProgramAdd = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDPROGRAM_CAPTION));
        programProjectAdd = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDPROJECT_CAPTION));
        JMenuItem programEdit = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITPROGRAM_CAPTION));
        programMenu.add(programProgramAdd);
        programMenu.add(programProjectAdd);
        programMenu.add(programEdit);
        programProgramAdd.addActionListener(elementAddListener);
        programProjectAdd.addActionListener(elementAddListener);
        programEdit.addActionListener(elementEditListener);

        projectMenu = new JPopupMenu(Messages.getInstance().getMessage(Messages.MENU_ROOT_CAPTION));
        JMenuItem projectAdd = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDASPECT_CAPTION));
        JMenuItem projectEdit = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITPROJECT_CAPTION));
        JMenuItem projectDelete = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEPROJECT_CAPTION));
        projectMenu.add(projectAdd);
        projectMenu.add(projectEdit);
        projectMenu.add(projectDelete);
        projectAdd.addActionListener(elementAddListener);
        projectEdit.addActionListener(elementEditListener);
        projectDelete.addActionListener(elementDeleteListener);

        aspectMenu = new JPopupMenu(Messages.getInstance().getMessage(Messages.MENU_ASPECT_CAPTION));
        JMenuItem aspectAdd = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDSUBJECT_CAPTION));
        JMenuItem aspectEdit = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITASPECT_CAPTION));
        JMenuItem aspectDelete = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEASPECT_CAPTION));
        aspectMenu.add(aspectAdd);
        aspectMenu.add(aspectEdit);
        aspectMenu.add(aspectDelete);
        aspectAdd.addActionListener(elementAddListener);
        aspectEdit.addActionListener(elementEditListener);
        aspectDelete.addActionListener(elementDeleteListener);

        subjectMenu = new JPopupMenu(Messages.getInstance().getMessage(Messages.MENU_SUBJECT_CAPTION));
        JMenuItem subjectAddMenuItem = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDACTIVITY_CAPTION));
        JMenuItem subjectEditMenuItem = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITSUBJECT_CAPTION));
        JMenuItem subjectDeleteMenuItem = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETESUBJECT_CAPTION));
        subjectMenu.add(subjectAddMenuItem);
        subjectMenu.add(subjectEditMenuItem);
        subjectMenu.add(subjectDeleteMenuItem);
        subjectAddMenuItem.addActionListener(elementAddListener);
        subjectEditMenuItem.addActionListener(elementEditListener);
        subjectDeleteMenuItem.addActionListener(elementDeleteListener);

        activityMenu = new JPopupMenu(Messages.getInstance().getMessage(Messages.MENU_ACTIVITY_CAPTION));
        JMenuItem activityAddMenuItem = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDFEATURE_CAPTION));
        JMenuItem activityEditMenuItem = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITACTIVITY_CAPTION));
        JMenuItem activityDeleteMenuItem = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEACTIVITY_CAPTION));
        activityMenu.add(activityAddMenuItem);
        activityMenu.add(activityEditMenuItem);
        activityMenu.add(activityDeleteMenuItem);
        activityAddMenuItem.addActionListener(elementAddListener);
        activityEditMenuItem.addActionListener(elementEditListener);
        activityDeleteMenuItem.addActionListener(elementDeleteListener);

        featureMenu = new JPopupMenu(Messages.getInstance().getMessage(Messages.MENU_FEATURE_CAPTION));
        JMenuItem featureDeleteMenuItem = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETEFEATURE_CAPTION));
        JMenuItem featureEditMenuItem = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_EDITFEATURE_CAPTION));
        featureMenu.add(featureEditMenuItem);
        featureMenu.add(featureDeleteMenuItem);
        featureEditMenuItem.addActionListener(elementEditListener);
        featureDeleteMenuItem.addActionListener(elementDeleteListener);

        MouseAdapter mouseAdapter = new MouseAdapter()
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
        projectTree.addMouseListener(mouseAdapter);
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
            String description = Messages.getInstance().getMessage(Messages.EXTENSIONFILEFILTER_FDD_DESCRIPTION);
            HashMap<String[], String> fileTypes = new HashMap<String[], String>();
            fileTypes.put(extensions, description);
            fileName = ExtensionFileFilter.getFileName(System.getProperty("user.home"), fileTypes,
                    ExtensionFileFilter.SAVE);
            this.currentProject = fileName;
        }
        else
        {
            fileName = this.currentProject;
        }

        if(fileName != null)
        {
            if(!FDDIXMLFileWriter.write(projectTree.getModel().getRoot(), fileName))
            {
                JOptionPane.showMessageDialog(this,
                    ResourceBundle.getBundle("messages").getString("FDDFrame.ErrorFileSave"),
                    ResourceBundle.getBundle("messages").getString("FDDFrame.ErrorFileSaveTitle"),
                    JOptionPane.ERROR_MESSAGE);
            }
            this.setTitle("FDD Tools - " + this.currentProject);
            modelDirty = false;
        }
    }

    /**
     * Add menu to Frame
     */
    private void addMenuBar()
    {
        JMenuItem fileNew = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_NEW));
        fileNew.setMnemonic('N');
        fileNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        JMenuItem fileOpen = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_OPEN));
        fileOpen.setMnemonic('O');
        fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        JMenuItem fileClose = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_CLOSE));
        fileClose.setMnemonic('C');

        fileSave = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_SAVE));
        fileSave.setMnemonic(KeyEvent.VK_S);
        fileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        fileSaveAs = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_SAVEAS));
        fileSaveAs.setMnemonic('A');

        JMenuItem fileImport = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_IMPORT));
        fileImport.setMnemonic('I');

        JMenuItem filePageSetup = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_PAGE_SETUP));
        filePageSetup.setMnemonic('u');
        filePageSetup.setEnabled(false);

        JMenuItem filePrint = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_PRINT));
        filePrint.setMnemonic('P');
        filePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        JMenuItem fileExit = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_EXIT));
        fileExit.setMnemonic('x');

        JMenu fileMenu = new JMenu(Messages.getInstance().getMessage(Messages.MENU_FILE));
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

        JMenuItem editUndo = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_UNDO));
        editUndo.setMnemonic('U');
        editUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        editUndo.setEnabled(false);

        JMenuItem editRedo = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_REDO));
        editRedo.setMnemonic('R');
        editRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        editRedo.setEnabled(false);

        JMenuItem editCut = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_CUT));
        editCut.setMnemonic('t');
        editCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        editCut.setEnabled(true);

        JMenuItem editCopy = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_COPY));
        editCopy.setMnemonic('C');
        editCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        editCopy.setEnabled(true);

        JMenuItem editPaste = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_PASTE));
        editPaste.setMnemonic('P');
        editPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        editPaste.setEnabled(true);

        JMenuItem editDelete = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_DELETE));
        editDelete.setMnemonic('D');
        editDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        JMenuItem editOption = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_OPTIONS));
        editOption.setMnemonic('O');

        JMenu editMenu = new JMenu(Messages.getInstance().getMessage(Messages.MENU_EDIT));
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

        JMenuItem helpHelp = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_HELP_CONTENT));
        helpHelp.setMnemonic('H');
        helpHelp.setAccelerator(KeyStroke.getKeyStroke("F1"));
        helpHelp.setEnabled(false);

        JMenuItem helpAbout = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_HELP_ABOUT));
        helpAbout.setMnemonic('A');

        JMenu helpMenu = new JMenu(Messages.getInstance().getMessage(Messages.MENU_HELP));
        helpMenu.setMnemonic('H');

        helpMenu.add(helpHelp);

        // Only add About menu item on non-macOS platforms
        if(!MAC_OS_X)
        {
            helpMenu.addSeparator();
            helpMenu.add(helpAbout);
        }

        JMenuBar menuBar = new JMenuBar();        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        fileOpen.addActionListener(fileOpenListener);
        fileNew.addActionListener(fileNewListener);
        fileClose.addActionListener(fileCloseListener);
        fileImport.addActionListener(fileImportListener);
        fileSave.addActionListener(fileSaveListener);
        fileSaveAs.addActionListener(fileSavaAsListener);
        filePrint.addActionListener(filePrintListener);
        fileExit.addActionListener(fileExitListener);

        editCopy.addActionListener(editCopyListener);
        editCut.addActionListener(editCutListener);
        editPaste.addActionListener(editPasteListener);
        editDelete.addActionListener(editDeleteListener);
        editOption.addActionListener(editOptionsListener);

        helpAbout.addActionListener(helpAboutListener);
    }

    public static void showComponentInCenter(final Component c, final Rectangle parentRect)
    {
        Rectangle rect = c.getBounds();
        c.setBounds((int) (parentRect.getX() + (parentRect.getWidth() - rect.getWidth()) / 2),
                (int) (parentRect.getY() + (parentRect.getHeight() - rect.getHeight()) / 2), (int) (rect.getWidth()), (int) (rect.getHeight()));
        c.setVisible(true);
    }

    private void addFDDElementNode(ActionEvent e)
    {
        FDDINode newNode = null;

        ObjectFactory of = new ObjectFactory();
        FDDINode currentNode = (FDDINode) projectTree.getSelectionPath().getLastPathComponent();

        // Use pattern matching for instanceof (Java 17+)
        switch (currentNode) {
            case Program program -> {
                if (e.getActionCommand().equals(Messages.getInstance().getMessage(Messages.MENU_ADDPROGRAM_CAPTION))) {
                    newNode = of.createProgram();
                } else if (e.getActionCommand().equals(Messages.getInstance().getMessage(Messages.MENU_ADDPROJECT_CAPTION))) {
                    newNode = of.createProject();
                }
            }
            case Project project -> newNode = of.createAspect();
            case Aspect aspect -> newNode = of.createSubject();
            case Subject subject -> newNode = of.createActivity();
            case Activity activity -> newNode = of.createFeature();
            default -> {
                // Handle unexpected node types
                return;
            }
        }
        newNode.setParent(currentNode);

        FDDElementDialog editDialog = new FDDElementDialog(this, newNode);
        showComponentInCenter((Component) editDialog, this.getBounds());

        if(editDialog.getAccept())
        {
            currentNode.add(newNode);
            projectTree.scrollPathToVisible(projectTree.getSelectionPath().pathByAddingChild(newNode));
            projectTree.updateUI();
            fddCanvasView.reflow();
            modelDirty = true;
        }
        fddCanvasView.revalidate();
    }

    private void editSelectedFDDElementNode()
    {
        FDDINode currentNode = (FDDINode) projectTree.getSelectionPath().getLastPathComponent();
        FDDElementDialog editDialog = new FDDElementDialog(this, currentNode);
        showComponentInCenter((Component) editDialog, this.getBounds());
        projectTree.updateUI();
        fddCanvasView.repaint();
        fddCanvasView.revalidate();
        modelDirty = true;
    }

    private void cutSelectedElementNode()
    {
        Object selectedNode = projectTree.getSelectionPath().getLastPathComponent();
        clipboard = (FDDINode) DeepCopy.copy(selectedNode);
        uniqueNodeVersion = true;
        deleteSelectedElementNode();
        projectTree.updateUI();
        fddCanvasView.reflow();
        fddCanvasView.revalidate();
        modelDirty = true;
    }

    private void copySelectedElementNode()
    {
        Object selectedNode = projectTree.getSelectionPath().getLastPathComponent();
        clipboard = (FDDINode) DeepCopy.copy(selectedNode);
        uniqueNodeVersion = false;
        projectTree.updateUI();
        fddCanvasView.reflow();
        fddCanvasView.revalidate();
    }

    private void pasetSelectedElementNode()
    {
        Object selectedNode = projectTree.getSelectionPath().getLastPathComponent();
        FDDINode newNode = (FDDINode) DeepCopy.copy(clipboard);
        if(newNode != null && selectedNode != null)
        {
            try
            {
                if(!uniqueNodeVersion)
                {
                    List<Feature> features = newNode.getFeaturesForNode();
                    for(Feature feature : features)
                    {
                        feature.setSeq(feature.getNextSequence());
                    }
                }
                ((FDDINode) selectedNode).add(newNode);
                uniqueNodeVersion = false;
                newNode.calculateProgress();
                projectTree.updateUI();
                fddCanvasView.reflow();
                fddCanvasView.revalidate();
                modelDirty = true;
            }
            catch(ClassCastException cce)
            {
                JOptionPane.showMessageDialog(this, Messages.getInstance().getMessage(Messages.CANNOT_PASTE_HERE));
            }
        }
    }

    private void deleteSelectedElementNode()
    {
        Object currentNode = projectTree.getSelectionPath().getLastPathComponent();
        if(!(currentNode.equals(projectTree.getModel().getRoot())))
        {
            if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
                    Messages.getInstance().getMessage(Messages.QUESTION_ARE_YOU_SURE),
                    Messages.getInstance().getMessage(Messages.JOPTIONPANE_DELETE_TITLE),
                    JOptionPane.YES_NO_OPTION))
            {
                TreePath parentPath = projectTree.getSelectionPath().getParentPath();
                projectTree.setSelectionPath(parentPath);
                Object parentNode = parentPath.getLastPathComponent();
                ((FDDINode) currentNode).setParent((FDDINode) parentNode);
                ((DefaultTreeModel) projectTree.getModel()).removeNodeFromParent((MutableTreeNode) currentNode);
                projectTree.updateUI();
                fddCanvasView.reflow();
                modelDirty = true;
            }
            fddCanvasView.revalidate();
        }
        else
        {
            JOptionPane.showMessageDialog(this, Messages.getInstance().getMessage(Messages.ERROR_INVALID_DELETE));
        }
    }

    private void showTreeCtxMenu(final Component origin, final int x, final int y)
    {
        TreePath selPath = projectTree.getPathForLocation(x, y);

        if(selPath != null)
        {
            projectTree.setSelectionPath(selPath);
            Object currentElementNode = selPath.getLastPathComponent();

            // Use pattern matching for instanceof (Java 17+)
            switch (currentElementNode) {
                case Program program -> {
                    if (program.getProgram().size() != 0) {
                        programProjectAdd.setEnabled(false);
                    } else {
                        programProjectAdd.setEnabled(true);
                    }
                    if (program.getProject().size() != 0) {
                        programProgramAdd.setEnabled(false);
                    } else {
                        programProgramAdd.setEnabled(true);
                    }
                    programMenu.show(origin, x, y);
                }
                case Project project -> projectMenu.show(origin, x, y);
                case Aspect aspect -> aspectMenu.show(origin, x, y);
                case Subject subject -> subjectMenu.show(origin, x, y);
                case Activity activity -> activityMenu.show(origin, x, y);
                case Feature feature -> featureMenu.show(origin, x, y);
                default -> {
                    // No menu for other node types
                }
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

        newProjectButton.setToolTipText(Messages.getInstance().getMessage(Messages.JBUTTON_NEW_TOOLTIP));
        openProjectButton.setToolTipText(Messages.getInstance().getMessage(Messages.JBUTTON_OPEN_TOOLTIP));
        saveButton.setToolTipText(Messages.getInstance().getMessage(Messages.JBUTTON_SAVE_TOOLTIP));
        printButton.setToolTipText(Messages.getInstance().getMessage(Messages.JBUTTON_PRINT_TOOLTIP));
        cutButton.setToolTipText(Messages.getInstance().getMessage(Messages.JBUTTON_CUT_TOOLTIP));
        copyButton.setToolTipText(Messages.getInstance().getMessage(Messages.JBUTTON_COPY_TOOLTIP));
        pasteButton.setToolTipText(Messages.getInstance().getMessage(Messages.JBUTTON_PASTE_TOOLTIP));

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
        addButton.setToolTipText(Messages.getInstance().getMessage(Messages.JBUTTON_ADD_TOOLTIP));
        delButton.setToolTipText(Messages.getInstance().getMessage(Messages.JBUTTON_DELETE_TOOLTIP));
        editButton.setToolTipText(Messages.getInstance().getMessage(Messages.JBUTTON_EDIT_TOOLTIP));
        bp.add(addButton);
        bp.add(delButton);
        bp.add(editButton);

        ActionListener elementAddListener = new ActionListener()
        {

            @Override
            public void actionPerformed(final ActionEvent e)
            {
                addFDDElementNode(e);
            }
        };

        final JPopupMenu programPopupMenu = new JPopupMenu(Messages.getInstance().getMessage(Messages.MENU_ROOT_CAPTION));
        final JMenuItem programAdd = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDPROGRAM_CAPTION));
        final JMenuItem projectAdd = new JMenuItem(Messages.getInstance().getMessage(Messages.MENU_ADDPROJECT_CAPTION));
        programPopupMenu.add(programAdd);
        programPopupMenu.add(projectAdd);
        programAdd.addActionListener(elementAddListener);
        projectAdd.addActionListener(elementAddListener);

        delButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(final ActionEvent e)
            {
                deleteSelectedElementNode();
            }
        });

        editButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(final ActionEvent e)
            {
                editSelectedFDDElementNode();
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter()
        {

            @Override
            public void mousePressed(final MouseEvent e)
            {
                Object component = projectTree.getSelectionPath().getLastPathComponent();
                if(component instanceof Program)
                {
                    if(((Program) component).getProgram().size() != 0)
                    {
                        projectAdd.setEnabled(false);
                    }
                    else
                    {
                        projectAdd.setEnabled(true);
                    }
                    if(((Program) component).getProject().size() != 0)
                    {
                        programAdd.setEnabled(false);
                    }
                    else
                    {
                        programAdd.setEnabled(true);
                    }

                    programPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
                else
                {
                    addFDDElementNode(null);
                }
            }
        };
        addButton.addMouseListener(mouseAdapter);

        return bp;
    }

    private void saveChangesDialog()
    {
        if((projectTree.getModel() != null) && modelDirty)
        {
            if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(FDDFrame.this,
                    Messages.getInstance().getMessage(Messages.QUESTION_SAVE_CHANGES),
                    Messages.getInstance().getMessage(Messages.JOPTIONPANE_SAVEQUESTION_TITLE),
                    JOptionPane.YES_NO_OPTION))
            {
                persistModel();
                modelDirty = false;
            }
        }
    }
}
