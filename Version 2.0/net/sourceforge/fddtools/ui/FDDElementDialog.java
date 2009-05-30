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

import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Feature;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import javax.xml.datatype.DatatypeConfigurationException;
import net.sourceforge.fddtools.internationalization.Messages;
import net.sourceforge.fddtools.model.FDDINode;
import net.miginfocom.swing.MigLayout;
import com.nebulon.xml.fddi.Milestone;
import com.nebulon.xml.fddi.MilestoneInfo;
import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.StatusEnum;
import java.util.GregorianCalendar;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
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
    private JXDatePicker calendarComboBox = new JXDatePicker();
    public boolean accept;
    private FDDINode node;
    private JPanel infoPanel = null;
    private JPanel buttonPanel = null;
    private JPanel progressPanel = null;
    private JTree projectTree = null;

    public FDDElementDialog(JFrame inJFrame, FDDINode inNode, JTree inTree)
    {

        super(inJFrame, Messages.getInstance().getMessage(TITLE), true);
        node = inNode;
        projectTree = inTree;

        calendarComboBox.setFormats(new SimpleDateFormat("yyyy-MM-dd"));

        this.setResizable(false);

        if(inNode.getName() != null)
        {
            nameTextField.setText(inNode.getName());
        }

        infoPanel = buildInfoPanel();

        if(inNode instanceof Feature)
        {
            progressPanel = milestonePanelGroup();
        }
        else if(inNode instanceof Aspect)
        {
            progressPanel = new AspectInfoPanel((Aspect) inNode);
        }
        else
        {
            progressPanel = buildGenericProgressPanel();
        }

        buttonPanel = buildButtonPanel();

        getContentPane().add(infoPanel, BorderLayout.NORTH);
        getContentPane().add(progressPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        pack();
    }

    private JPanel buildButtonPanel()
    {
        JPanel btnPanel = new JPanel();
        JButton okButton = new JButton(Messages.getInstance().getMessage(JBUTTON_OK_CAPTION));
        JButton cancelButton = new JButton(Messages.getInstance().getMessage(JBUTTON_CANCEL_CAPTION));

        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                accept = true;
                node.setName(nameTextField.getText().trim());

                if(node instanceof Feature)
                {
                    Aspect aspect = getAspect(node);
                    ((Feature) node).setInitials(ownerTextField.getText().trim());
                    for(int i = 0; i < ((Feature) node).getMilestone().size(); i++)
                    {
                        Milestone m = ((Feature) node).getMilestone().get(i);
                        String milestoneName = aspect.getInfo().getMilestoneInfo().get(i).getName();
                        try
                        {
                            for(Component component : progressPanel.getComponents())
                            {
                                if(component instanceof JXDatePicker)
                                {
                                    if(component.getName().equals(milestoneName.concat("planned")))
                                    {
                                        GregorianCalendar cal = new GregorianCalendar();
                                        Date plannedDate = ((JXDatePicker) component).getDate();
                                        if(plannedDate != null)
                                        {
                                            cal.setTime(plannedDate);
                                            XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
                                            m.setPlanned(xmlDate);
                                        }
                                    }
                                    if(component.getName().equals(milestoneName.concat("actual")))
                                    {
                                        GregorianCalendar cal = new GregorianCalendar();
                                        Date actualDate = ((JXDatePicker) component).getDate();
                                        if(actualDate != null)
                                        {
                                            cal.setTime(actualDate);
                                            XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
                                            m.setActual(xmlDate);
                                        }
                                        else
                                            m.setActual(null);
                                    }
                                }
                                else if(component instanceof JCheckBox)
                                {
                                    if(component.getName().equals(milestoneName.concat("complete")))
                                    {
                                        if(((JCheckBox) component).isSelected() == true)
                                        {
                                            m.setStatus(StatusEnum.COMPLETE);
                                        }
                                        else
                                        {
                                            m.setStatus(StatusEnum.NOTSTARTED);
                                        }
                                    }
                                }
                            }
                        }
                        catch(DatatypeConfigurationException ex)
                        {
                            Logger.getLogger(FDDElementDialog.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if(node instanceof Feature)
                    {
                        node.calculateProgress();
                        node.calculateTargetDate();
                    }
                }
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

    private JPanel buildInfoPanel()
    {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new MigLayout());
        infoPanel.add(new JLabel(Messages.getInstance().getMessage(JPANEL_INFO_TITLE)));
        infoPanel.add(new JSeparator(), "span 2, wrap");
        if(node instanceof Activity || node instanceof Feature)
        {
            infoPanel.add(new JLabel(Messages.getInstance().getMessage(JLABEL_NAME_CAPTION)), "split");
            infoPanel.add(nameTextField, "span 2");
            infoPanel.add(new JLabel(Messages.getInstance().getMessage(JLABEL_OWNER_CAPTION)));
            if(node instanceof Activity)
            {
                ownerTextField.setText(((Activity) node).getInitials());
            }
            else if(node instanceof Feature)
            {
                ownerTextField.setText(((Feature) node).getInitials());
            }
            infoPanel.add(ownerTextField, "wrap");
        }
        else
        {
            infoPanel.add(new JLabel(Messages.getInstance().getMessage(JLABEL_NAME_CAPTION)), "split");
            infoPanel.add(nameTextField, "growx, wrap");
        }

        return infoPanel;
    }

    private JPanel buildGenericProgressPanel()
    {
        JPanel genericProgressPanel = new JPanel();
        genericProgressPanel.setLayout(new GridLayout(2, 1));
        genericProgressPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED),
                Messages.getInstance().getMessage(JPANEL_PROGRESS_TITLE)));

        JPanel targetDatePanel = new JPanel();
        targetDatePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        targetDatePanel.add(new Label(Messages.getInstance().getMessage(JLABEL_TARGETDATE_CAPTION)));

        DateFormat format = DateFormat.getDateInstance();

        if(node.getTargetDate() != null)
        {
            targetDatePanel.add(new Label(format.format(node.getTargetDate())));
        }
        else
        {
            targetDatePanel.add(new Label("TBD"));
        }

        JPanel percentCompletePanel = new JPanel();
        percentCompletePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        percentCompletePanel.add(new Label(Messages.getInstance().getMessage(JLABEL_PERCENTCOMPLETE_CAPTION)));
        if(node.getProgress() != null)
            percentCompletePanel.add(new Label(Integer.toString(node.getProgress().getCompletion()) + "%"));
        else
            percentCompletePanel.add(new Label(Integer.toString(0)));

        genericProgressPanel.add(targetDatePanel);
        genericProgressPanel.add(percentCompletePanel);

        return genericProgressPanel;
    }

    public JPanel milestonePanelGroup()
    {
        String[] dateStr = {"MM/dd/yyyy", "MM-dd-yyyy"};
        JPanel milestonePanelGroup = new JPanel();
        milestonePanelGroup.setLayout(new MigLayout("", "[left][center][center][center]"));
        milestonePanelGroup.add(new JLabel("Milestone"), "width 200!");
        milestonePanelGroup.add(new JLabel("Planned"), "width 150!");
        milestonePanelGroup.add(new JLabel("Actual"), "width 150!");
        milestonePanelGroup.add(new JLabel("Complete"), "width 75!, wrap");

        Aspect aspect = getAspect(node);

        if(((Feature) node).getMilestone().size() == 0)
        {
            ObjectFactory of = new ObjectFactory();
            for(MilestoneInfo m : aspect.getInfo().getMilestoneInfo())
            {
                ((Feature) node).getMilestone().add(of.createMilestone());
            }
        }

        for(int i = 0; i < ((Feature) node).getMilestone().size(); i++)
        {
            String milestoneName = aspect.getInfo().getMilestoneInfo().get(i).getName();
            Milestone m = ((Feature) node).getMilestone().get(i);

            JLabel label = new JLabel(milestoneName);
            JXDatePicker planned = new JXDatePicker();
            planned.setFormats(dateStr);
            planned.setName(milestoneName.concat("planned"));
            JXDatePicker actual = new JXDatePicker();
            actual.setFormats(dateStr);
            actual.setName(milestoneName.concat("actual"));
            JCheckBox complete = new JCheckBox();
            complete.setName(milestoneName.concat("complete"));
            if(m.getPlanned() != null)
            {
                planned.setDate(m.getPlanned().toGregorianCalendar().getTime());
            }
            else
            {
                planned.setDate(new Date());
            }
            if(m.getActual() != null)
            {
                actual.setDate(m.getActual().toGregorianCalendar().getTime());
            }
            if(m.getStatus() != null)
            {
                complete.setSelected((m.getStatus() == StatusEnum.COMPLETE) ? true : false);
            }
            milestonePanelGroup.add(label);
            milestonePanelGroup.add(planned, "align left, growx");
            milestonePanelGroup.add(actual, "align left, growx");
            milestonePanelGroup.add(complete, "wrap");
        }
        return milestonePanelGroup;
    }

    public Aspect getAspect(FDDINode localNode)
    {
        TreeNode[] path = ((DefaultTreeModel) projectTree.getModel()).getPathToRoot(localNode);

        for(Object pathNode : path)
        {
            if(pathNode instanceof Aspect)
            {
                return (Aspect) pathNode;
            }
        }
        return null;
    }

}
