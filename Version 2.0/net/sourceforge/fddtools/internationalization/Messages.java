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
package net.sourceforge.fddtools.internationalization;

import java.util.ResourceBundle;

/**
 * Internationalized text access.
 */
public class Messages
{

    public static final String ERROR_FILE_NOT_FOUND = "FDDFrame.ErrorFileNotFound";
    public static final String ERROR_PARSING_FILE = "FDDFrame.ErrorParsingFile";
    public static final String QUESTION_SAVE_CHANGES = "FDDFrame.QuestionSaveChanges";
    public static final String JOPTIONPANE_SAVEQUESTION_TITLE = "FDDFrame.SaveQuestion.Title";
    public static final String JTREE_ROOTNODE_CAPTION = "FDDFrame.JTreeRootNode.Caption";
    public static final String JFRAME_FDDOPTIONVIEW_TITLE = "FDDFrame.JFrameFDDOptionView.Title";
    public static final String EXTENSIONFILEFILTER_CSV_DESCRIPTION = "FDDFrame.ExtensionFileFilterCSV.Description";
    public static final String EXTENSIONFILEFILTER_FDD_DESCRIPTION = "FDDFrame.ExtensionFileFilterFDD.Description";
    public static final String MAJORFEATURESET_DEFAULT_TEXT = "FDDFrame.MajorFeatureSet.DefaultText";
    public static final String FEATURESET_DEFAULT_TEXT = "FDDFrame.FeatureSet.DefaultText";
    public static final String FEATURE_DEFAULT_TEXT = "FDDFrame.Feature.DefaultText";
    public static final String MENU_ROOT_CAPTION = "FDDFrame.MenuRoot.Caption";
    public static final String MENU_ADDPROGRAM_CAPTION = "FDDFrame.MenuAddProgram.Caption";
    public static final String MENU_EDITPROGRAM_CAPTION = "FDDFrame.MenuEditProgram.Caption";
    public static final String MENU_PROJECT_CAPTION = "FDDFrame.MenuProject.Caption";
    public static final String MENU_ADDPROJECT_CAPTION = "FDDFrame.MenuAddProject.Caption";
    public static final String MENU_EDITPROJECT_CAPTION = "FDDFrame.MenuEditProject.Caption";
    public static final String MENU_DELETEPROJECT_CAPTION = "FDDFrame.MenuDeleteProject.Caption";
    public static final String MENU_ASPECT_CAPTION = "FDDFrame.MenuAspect.Caption";
    public static final String MENU_ADDASPECT_CAPTION = "FDDFrame.MenuAddAspect.Caption";
    public static final String MENU_EDITASPECT_CAPTION = "FDDFrame.MenuEditAspect.Caption";
    public static final String MENU_DELETEASPECT_CAPTION = "FDDFrame.MenuDeleteAspect.Caption";
    public static final String MENU_SUBJECT_CAPTION = "FDDFrame.MenuSubject.Caption";
    public static final String MENU_ADDSUBJECT_CAPTION = "FDDFrame.MenuAddSubject.Caption";
    public static final String MENU_EDITSUBJECT_CAPTION = "FDDFrame.MenuEditSubject.Caption";
    public static final String MENU_DELETESUBJECT_CAPTION = "FDDFrame.MenuDeleteSubject.Caption";
    public static final String MENU_ACTIVITY_CAPTION = "FDDFrame.MenuActivity.Caption";
    public static final String MENU_ADDACTIVITY_CAPTION = "FDDFrame.MenuAddActivity.Caption";
    public static final String MENU_EDITACTIVITY_CAPTION = "FDDFrame.MenuEditActvity.Caption";
    public static final String MENU_DELETEACTIVITY_CAPTION = "FDDFrame.MenuDeleteActivity.Caption";
    public static final String MENU_FEATURE_CAPTION = "FDDFrame.MenuFeature.Caption";
    public static final String MENU_ADDFEATURE_CAPTION = "FDDFrame.MenuAddFeature.Caption";
    public static final String MENU_EDITFEATURE_CAPTION = "FDDFrame.MenuEditFeature.Caption";
    public static final String MENU_DELETEFEATURE_CAPTION = "FDDFrame.MenuDeleteFeature.Caption";
    public static final String MENU_NEW = "FDDFrame.MenuNew.Caption";
    public static final String MENU_OPEN = "FDDFrame.MenuOpen.Caption";
    public static final String MENU_CLOSE = "FDDFrame.MenuClose.Caption";
    public static final String MENU_SAVE = "FDDFrame.MenuSave.Caption";
    public static final String MENU_SAVEAS = "FDDFrame.MenuSaveAs.Caption";
    public static final String MENU_IMPORT = "FDDFrame.MenuImport.Caption";
    public static final String MENU_PAGE_SETUP = "FDDFrame.MenuPageSetup.Caption";
    public static final String MENU_PRINT = "FDDFrame.MenuPrint.Caption";
    public static final String MENU_EXIT = "FDDFrame.MenuExit.Caption";
    public static final String MENU_FILE = "FDDFrame.MenuFile.Caption";
    public static final String MENU_UNDO = "FDDFrame.MenuUndo.Caption";
    public static final String MENU_REDO = "FDDFrame.MenuRedo.Caption";
    public static final String MENU_CUT = "FDDFrame.MenuCut.Caption";
    public static final String MENU_COPY = "FDDFrame.MenuCopy.Caption";
    public static final String MENU_PASTE = "FDDFrame.MenuPaste.Caption";
    public static final String MENU_DELETE = "FDDFrame.MenuDelete.Caption";
    public static final String MENU_OPTIONS = "FDDFrame.MenuOptions.Caption";
    public static final String MENU_EDIT = "FDDFrame.MenuEdit.Caption";
    public static final String MENU_HELP = "FDDFrame.MenuHelp.Caption";
    public static final String MENU_HELP_ABOUT = "FDDFrame.MenuAbout.Caption";
    public static final String MENU_HELP_CONTENT = "FDDFrame.MenuHelpContent.Caption";
    public static final String ERROR_INVALID_CUT = "FDDFrame.ErrorInvalidCut";
    public static final String ERROR_ILLEGAL_ACTION = "FDDFrame.ErrorIllegalAction";
    public static final String QUESTION_ARE_YOU_SURE = "FDDFrame.QuestionAreYouSure";
    public static final String JOPTIONPANE_DELETE_TITLE = "FDDFrame.JOptionPaneDelete.Title";
    public static final String ERROR_INVALID_DELETE = "FDDFrame.ErrorInvalidDelete";
    public static final String JBUTTON_NEW_TOOLTIP = "FDDFrame.JButtonNew.ToolTip";
    public static final String JBUTTON_OPEN_TOOLTIP = "FDDFrame.JButtonOpen.ToolTip";
    public static final String JBUTTON_SAVE_TOOLTIP = "FDDFrame.JButtonSave.ToolTip";
    public static final String JBUTTON_PRINT_TOOLTIP = "FDDFrame.JButtonPrint.ToolTip";
    public static final String JBUTTON_CUT_TOOLTIP = "FDDFrame.JButtonCut.ToolTip";
    public static final String JBUTTON_COPY_TOOLTIP = "FDDFrame.JButtonCopy.ToolTip";
    public static final String JBUTTON_PASTE_TOOLTIP = "FDDFrame.JButtonPaste.ToolTip";
    public static final String JBUTTON_ADD_TOOLTIP = "FDDFrame.JButtonAdd.ToolTip";
    public static final String JBUTTON_DELETE_TOOLTIP = "FDDFrame.JButtonDelete.ToolTip";
    public static final String JBUTTON_EDIT_TOOLTIP = "FDDFrame.JButtonEdit.ToolTip";
    public static final String ERROR_INSTANTIATION_EXCEPTION = "FDDFrame.ErrorInstantiationException";
    public static final String ERROR_ILLEGAL_ACCESS_DURING_COPY = "FDDFrame.ErrorIllegalAccessDuringCopy";
    public static final String PROJECT_DEFAULT_NAME = "FDDSequenceTreeBuilder.Project.DefaultName";
   	public static final String FONTSTYLE_PLAIN = "FDDOptionView.FontStyle.Plain";
	public static final String FONTSTYLE_BOLD = "FDDOptionView.FontStyle.Bold";
	public static final String FONTSTYLE_ITALIC = "FDDOptionView.FontStyle.Italic";
	public static final String FONTSTYLE_BOLD_ITALIC = "FDDOptionView.FontStyle.BoldItalic";
	public static final String JBUTTON_CANCEL_CAPTION = "FDDOptionView.JButtonCancel.Caption";
	public static final String JBUTTON_APPLY_CAPTION = "FDDOptionView.JButtonApply.Caption";
	public static final String JTABBEDPANE_FONT_TITLE = "FDDOptionView.JTabbedPaneFont.Title";
	public static final String JLABEL_FONTFAMILY_CAPTION = "FDDOptionView.JLabelFontFamily.Caption";
	public static final String JLABEL_FONTSIZE_CAPTION = "FDDOptionView.JLabelFontSize.Caption";
	public static final String JLABEL_FONTSTYLE_CAPTION = "FDDOptionView.JLabelFontStyle.Caption";
	public static final String TITLEBORDER_SAMPLETEXT_CAPTION = "FDDOptionView.TitleBorderSampleText.Caption";
    public static final String MENU_SAVE_AS_CAPTION = "FDDCanvasView.MenuSaveAs.Caption";
    public static final String MENU_PRINT_CAPTION = "FDDCanvasView.MenuPrint.Caption";
    public static final String MENU_PROPERTIES_CAPTION = "FDDCanvasView.MenuProperties.Caption";
    public static final String MENU_CAPTION = "FDDCanvasView.Menu.Caption";
    public static final String JOPTIONPANE_TITLE = "FDDCanvasView.JOptionPane.Title";
    public static final String ERROR_IMAGE_FORMAT = "FDDCanvasView.ErrorImageFormat";
    public static final String QUESTION_FILE_EXISTS_OVERRIDE = "FDDCanvasView.QuestionFileExistsOverride";
    public static final String ERROR_SAVING_IMAGE = "FDDCanvasView.ErrorSavingImage";
    
    public static final String ABOUT_TITLE = "AboutDialog.Title";
	public static final String JBUTTON_OK_CAPTION = "AboutDialog.JButtonOk.Caption";
	public static final String JPANEL_ABOUT_CAPTION = "AboutDialog.JPanelAbout.Caption";
	public static final String JPANEL_COPYRIGHT_CAPTION = "AboutDialog.JPanelCopyright.Caption";

    public static final String EDIT_MILESTONE_INFO = "AspectInfoPanel.EditMilestoneInfo";
    public static final String ASPECTINFO_EDIT_MENU = "AspectInfoPanel.EditMenu";
    public static final String ASPECTINFO_ADD_ITEM = "AspectInfoPanel.AddItem";
    public static final String ASPECTINFO_INSERT_ITEM = "AspectInfoPanel.InsertItem";
    public static final String ASPECTINFO_DELETE_ITEM = "AspectInfoPanel.DeleteItem";
    public static final String ASPECTINFO_ADD_DEFAULT_ITEMS = "AspectInfoPanel.AddDefaultItems";

    /*
     * STATIC ATTRIBUTES
     */
    private static final String FILE_BASE = "messages";
    private static Messages instance = null;
    private static ResourceBundle resource = null;

    /*
     * STATIC METHODS
     */
    /**
     * @return Singleton instance.
     */
    public static Messages getInstance()
    {
        if(instance == null)
        {
            instance = new Messages();
        }

        return instance;
    }

    /*
     * CONSTRUCTORS
     */
    /**
     * Default constructor.
     */
    private Messages()
    {
        resource = ResourceBundle.getBundle(FILE_BASE);
    }

    /*
     * PUBLIC MESSAGES
     */
    /**
     * Get text associated with key in current language.
     * @param key Text key.
     * @return Text in the current language.
     */
    public String getMessage(String key)
    {
        String intText;

        try
        {
            intText = resource.getString(key);
        } catch(Exception e)
        {
            intText = key + '!';
        }
        return intText;
    }
}
