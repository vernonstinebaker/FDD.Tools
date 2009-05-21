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

import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Feature;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import net.sourceforge.fddtools.internationalization.Messages;
import net.sourceforge.fddtools.model.FDDINode;
import org.jdesktop.swingx.JXDatePicker;

public class FDDElementDialog extends JDialog
{
    // > Internationalization keys
    private static final String TITLE = "FDDElementDialog.Title";
    private static final String JBUTTON_OK_CAPTION = "FDDElementDialog.JButtonOk.Caption";
    private static final String JBUTTON_CANCEL_CAPTION = "FDDElementDialog.JButtonCancel.Caption";
    private static final String JPANEL_INFO_TITLE = "FDDElementDialog.JPanelInfo.Title";
    private static final String JLABEL_NAME_CAPTION = "FDDElementDialog.JLabelName.Caption";
    private static final String JLABEL_OWNER_CAPTION = "FDDElementDialog.JLabelOwner.Caption";
    private static final String JPANEL_PROGRESS_TITLE = "FDDElementDialog.JPanelProgress.Title";
    private static final String JPANEL_DATE_TITLE = "FDDElementDialog.JPanelDate.Title";
    private static final String JPANEL_DESIGNBYFEATURE_TITLE = "FDDElementDialog.JPanelDesignByFeature.Title";
    private static final String JPANEL_BUILDBYFEATURE_TITLE = "FDDElementDialog.JPanelBuildByFeature.Title";
    private static final String JLABEL_DOMAINWALKTHROUGH_CAPTION = "FDDElementDialog.JLabelDomainWalkthrough.Caption";
    private static final String JLABEL_DESIGN_CAPTION = "FDDElementDialog.JLabelDesign.Caption";
    private static final String JLABEL_DESIGNINSPECTION_CAPTION = "FDDElementDialog.JLabelDesignInspection.Caption";
    private static final String JLABEL_CODE_CAPTION = "FDDElementDialog.JLabelCode.Caption";
    private static final String JLABEL_CODEINSPECTION_CAPTION = "FDDElementDialog.JLabelCodeInspection.Caption";
    private static final String JLABEL_PROMOTETOBUILD_CAPTION = "FDDElementDialog.JLabelPromoteToBuild.Caption";
    private static final String JLABEL_TARGETDATE_CAPTION = "FDDElementDialog.JLabelTargetDate.Caption";
    private static final String JLABEL_PERCENTCOMPLETE_CAPTION = "FDDElementDialog.JLabelPercentComplete.Caption";
    // < End internationalization keys
    private JTextField nameTextField = new JTextField(25);
    private JTextField ownerTextField = new JTextField(2);
    private Date targetDate = null;
    private int percentComplete = 0;
    private JXDatePicker calendarComboBox = new JXDatePicker();
    private JCheckBox domainWalkthroughCheckBox = null;
    private JCheckBox designCheckBox = null;
    private JCheckBox designInspectionCheckBox = null;
    private JCheckBox codeCheckBox = null;
    private JCheckBox codeInspectionCheckBox = null;
    private JCheckBox buildCheckBox = null;
    public boolean accept;
    private FDDINode node;
    private JPanel infoPanel = null;
    private JPanel buttonPanel = null;
    private JPanel progressPanel = null;

    public FDDElementDialog(JFrame parent, FDDINode node)
    {

        super(parent, Messages.getInstance().getMessage(TITLE), true);
        this.node = node;

        calendarComboBox.setFormats(new SimpleDateFormat("yyyy-MM-dd"));

        this.setResizable(false);

        if(node.getName() != null)
        {
            nameTextField.setText(node.getName());
        }

/*
        if(element.getOwner() != null)
        {
            ownerTextField.setText(element.getOwner());
        }

        if(element.getTargetMonth() != null)
        {
            targetDate = element.getTargetMonth();
        }

        percentComplete = element.getProgress();
 */

        infoPanel = infoPanel();
        buttonPanel = buttonPanel();
        progressPanel = genericProgressPanel();


        if(node instanceof Feature)
        {
            progressPanel =  new FeaturePanel();
        }
        else if(node instanceof Aspect)
        {
            progressPanel = new AspectInfoPanel((Aspect) node);
        }

        getContentPane().add(infoPanel, BorderLayout.NORTH);
        getContentPane().add(progressPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        pack();
    }

    private JPanel buttonPanel()
    {
        JPanel btnPanel = new JPanel();
        JButton okButton = new JButton(Messages.getInstance().getMessage(JBUTTON_OK_CAPTION));
        JButton cancelButton = new JButton(Messages.getInstance().getMessage(JBUTTON_CANCEL_CAPTION));

        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                FDDElementDialog.this.accept = true;
                node.setName(nameTextField.getText().trim());

                if(node instanceof Feature)
                {
                    ((Feature) node).setInitials(ownerTextField.getText().trim());
                }

/*
                FDDElementDialog.this.element.setOwner(FDDElementDialog.this.ownerTextField.getText().trim());

                if(FDDElementDialog.this.element instanceof net.sourceforge.fddtools.model.Feature)
                {
                    FDDElementDialog.this.element.setTargetMonth(calendarComboBox.getDate());
                    //  FDDElementDialog.this.calendarComboBox.getCalendar().getTime());

                    if(FDDElementDialog.this.buildCheckBox.isSelected())
                    {
                        FDDElementDialog.this.element.setProgress(100);
                    }
                    else if(FDDElementDialog.this.codeInspectionCheckBox.isSelected())
                    {
                        FDDElementDialog.this.element.setProgress(99);
                    }
                    else if(FDDElementDialog.this.codeCheckBox.isSelected())
                    {
                        FDDElementDialog.this.element.setProgress(89);
                    }
                    else if(FDDElementDialog.this.designInspectionCheckBox.isSelected())
                    {
                        FDDElementDialog.this.element.setProgress(44);
                    }
                    else if(FDDElementDialog.this.designCheckBox.isSelected())
                    {
                        FDDElementDialog.this.element.setProgress(41);
                    }
                    else if(FDDElementDialog.this.domainWalkthroughCheckBox.isSelected())
                    {
                        FDDElementDialog.this.element.setProgress(1);
                    }
                    else
                    {
                        FDDElementDialog.this.element.setProgress(0);
                    }
                }
*/
                dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                accept = false;
                dispose();
            }
        });
        btnPanel.add(okButton);
        btnPanel.add(cancelButton);

        return btnPanel;
    }

    private JPanel infoPanel()
    {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(2, 1));
        infoPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                Messages.getInstance().getMessage(JPANEL_INFO_TITLE)));
        JPanel namePanel = new JPanel();
        JLabel nameLabel = new JLabel(Messages.getInstance().getMessage(JLABEL_NAME_CAPTION));
        namePanel.add(nameLabel);
        namePanel.add(nameTextField);
        JPanel ownerPanel = new JPanel();
        ownerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel ownerLabel = new JLabel(Messages.getInstance().getMessage(JLABEL_OWNER_CAPTION));
        ownerPanel.add(ownerLabel);
        ownerPanel.add(ownerTextField);

        infoPanel.add(namePanel);
        infoPanel.add(ownerPanel);

        return infoPanel;
    }

    private JPanel featureProgressPanel()
    {
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new GridLayout(3, 1));
        progressPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                Messages.getInstance().getMessage(JPANEL_PROGRESS_TITLE)));

        JPanel targetDatePanel = new JPanel();
        targetDatePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        targetDatePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                Messages.getInstance().getMessage(JPANEL_DATE_TITLE)));

        if(targetDate != null)
        {
//            Calendar cal = calendarComboBox.getCalendar();
//            cal.setTime(targetDate);
//            calendarComboBox.setCalendar(cal);
            calendarComboBox.setDate(targetDate);
        }
        else
        {

//            Calendar cal = calendarComboBox.getCalendar();
//            cal.setTime(new Date());
//            calendarComboBox.setCalendar(cal);
            calendarComboBox.setDate(new Date());
        }

        targetDatePanel.add(calendarComboBox);

        JPanel dbfPanel = new JPanel();
        JPanel bbfPanel = new JPanel();
        dbfPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                Messages.getInstance().getMessage(JPANEL_DESIGNBYFEATURE_TITLE)));
        bbfPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                Messages.getInstance().getMessage(JPANEL_BUILDBYFEATURE_TITLE)));

        JLabel domainWalkthroughLabel = new JLabel(
                Messages.getInstance().getMessage(JLABEL_DOMAINWALKTHROUGH_CAPTION),
                JLabel.CENTER);
        JLabel designLabel = new JLabel(
                Messages.getInstance().getMessage(JLABEL_DESIGN_CAPTION),
                JLabel.CENTER);
        JLabel designInspectionLabel = new JLabel(
                Messages.getInstance().getMessage(JLABEL_DESIGNINSPECTION_CAPTION),
                JLabel.CENTER);
        JLabel codeLabel = new JLabel(
                Messages.getInstance().getMessage(JLABEL_CODE_CAPTION),
                JLabel.CENTER);
        JLabel codeInspectionLabel = new JLabel(
                Messages.getInstance().getMessage(JLABEL_CODEINSPECTION_CAPTION),
                JLabel.CENTER);
        JLabel buildLabel = new JLabel(
                Messages.getInstance().getMessage(JLABEL_PROMOTETOBUILD_CAPTION),
                JLabel.CENTER);

        domainWalkthroughCheckBox = new JCheckBox();
        domainWalkthroughCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
        designCheckBox = new JCheckBox();
        designCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
        designInspectionCheckBox = new JCheckBox();
        designInspectionCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
        codeCheckBox = new JCheckBox();
        codeCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
        codeInspectionCheckBox = new JCheckBox();
        codeInspectionCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
        buildCheckBox = new JCheckBox();
        buildCheckBox.setHorizontalAlignment(JCheckBox.CENTER);

        dbfPanel.setLayout(new GridLayout(2, 3));
        bbfPanel.setLayout(new GridLayout(2, 3));

        dbfPanel.add(domainWalkthroughLabel);
        dbfPanel.add(designLabel);
        dbfPanel.add(designInspectionLabel);
        dbfPanel.add(domainWalkthroughCheckBox);
        dbfPanel.add(designCheckBox);
        dbfPanel.add(designInspectionCheckBox);

        bbfPanel.add(codeLabel);
        bbfPanel.add(codeInspectionLabel);
        bbfPanel.add(buildLabel);
        bbfPanel.add(codeCheckBox);
        bbfPanel.add(codeInspectionCheckBox);
        bbfPanel.add(buildCheckBox);

        setCheckBoxPercentComplete();
        progressPanel.add(targetDatePanel);
        progressPanel.add(dbfPanel);
        progressPanel.add(bbfPanel);

        return progressPanel;
    }

    private void setCheckBoxPercentComplete()
    {
        if(percentComplete >= FDDOptionModel.domainWalkthroughPercent)
        {
            domainWalkthroughCheckBox.setSelected(true);
        }

        if(percentComplete > (FDDOptionModel.domainWalkthroughPercent +
                FDDOptionModel.designPercent))
        {
            designCheckBox.setSelected(true);
        }

        if(percentComplete >= (FDDOptionModel.domainWalkthroughPercent +
                FDDOptionModel.designPercent +
                FDDOptionModel.designInspectionPercent))
        {
            designInspectionCheckBox.setSelected(true);
        }

        if(percentComplete >= (FDDOptionModel.domainWalkthroughPercent +
                FDDOptionModel.designPercent +
                FDDOptionModel.designInspectionPercent +
                FDDOptionModel.codePercent))
        {
            codeCheckBox.setSelected(true);
        }

        if(percentComplete >= (FDDOptionModel.domainWalkthroughPercent +
                FDDOptionModel.designPercent +
                FDDOptionModel.designInspectionPercent +
                FDDOptionModel.codePercent +
                FDDOptionModel.codeInspectionPercent))
        {
            codeInspectionCheckBox.setSelected(true);
        }

        if(percentComplete >= (FDDOptionModel.domainWalkthroughPercent +
                FDDOptionModel.designPercent +
                FDDOptionModel.designInspectionPercent +
                FDDOptionModel.codePercent +
                FDDOptionModel.codeInspectionPercent +
                FDDOptionModel.buildPercent))
        {
            buildCheckBox.setSelected(true);
        }
    }

    private JPanel genericProgressPanel()
    {
        JPanel genericProgressPanel = new JPanel();
        genericProgressPanel.setLayout(new GridLayout(2, 1));
        genericProgressPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                Messages.getInstance().getMessage(JPANEL_PROGRESS_TITLE)));

        JPanel targetDatePanel = new JPanel();
        targetDatePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        targetDatePanel.add(new Label(Messages.getInstance().getMessage(JLABEL_TARGETDATE_CAPTION)));

        DateFormat format = DateFormat.getDateInstance();

        if(targetDate != null)
        {
            targetDatePanel.add(new Label(format.format(targetDate)));
        }
        else
        {
            targetDatePanel.add(new Label(format.format(new Date())));
        }

        JPanel percentCompletePanel = new JPanel();
        percentCompletePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        percentCompletePanel.add(new Label(Messages.getInstance().getMessage(JLABEL_PERCENTCOMPLETE_CAPTION)));
        percentCompletePanel.add(new Label(new Integer(percentComplete).toString()));

        genericProgressPanel.add(targetDatePanel);
        genericProgressPanel.add(percentCompletePanel);

        return genericProgressPanel;
    }
}
