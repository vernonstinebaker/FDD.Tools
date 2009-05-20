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
/*
 * AspectPanel.java
 *
 * Created on May 19, 2009, 8:23:27 PM
 */
package net.sourceforge.fddtools.ui;

import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.AspectInfo;
import com.nebulon.xml.fddi.MilestoneInfo;
import com.nebulon.xml.fddi.ObjectFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.observablecollections.ObservableList;

/**
 *
 * @author vds
 */
public class AspectInfoPanel extends JPanel
{
    private Aspect aspect = null;
    private JPopupMenu tableEditMenu = null;
    ObservableList<MilestoneInfo> milestoneInfoObservableArrayList = null;

    /** Creates new form AspectPanel */
    public AspectInfoPanel(Aspect aspect)
    {
        this.aspect = aspect;
//        if(aspect.getInfo() != null && aspect.getInfo().getMilestoneInfo() != null)
//        {
//            milestoneInfoObservableArrayList =
//                    ObservableCollections.observableList(aspect.getInfo().getMilestoneInfo());
//        }

        initComponents();
        milestoneInfoTable.getModel().addTableModelListener(new TableModelListener()
        {
            public void tableChanged(TableModelEvent e)
            {
                updateEffort();
            }
        });
    }

    private MilestoneInfo createMilestoneInfo()
    {
        ObjectFactory of = new ObjectFactory();
        MilestoneInfo mi = of.createMilestoneInfo();
        mi.setName("Edit this name.");
        mi.setEffort(BigInteger.ZERO);
        return mi;
    }

    ActionListener addMilestoneListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            MilestoneInfo mi = createMilestoneInfo();
            Binding binding = bindingGroup.getBinding("milestoneInfoBinding");
            binding.unbind();
            aspect.getInfo().getMilestoneInfo().add(mi);
            binding.bind();
            updateUI();
        }

    };
    ActionListener insertMilestoneListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            MilestoneInfo mi = createMilestoneInfo();
            int index = milestoneInfoTable.getSelectedRow() > 0 ? milestoneInfoTable.getSelectedRow() : 0;
            Binding binding = bindingGroup.getBinding("milestoneInfoBinding");
            binding.unbind();
            aspect.getInfo().getMilestoneInfo().add(index, mi);
            binding.bind();
            AspectInfoPanel.this.updateUI();
        }

    };
    ActionListener deleteMilestoneListener = new ActionListener()
    {
        public void actionPerformed(final ActionEvent e)
        {
            MilestoneInfo mi = createMilestoneInfo();
            int index = milestoneInfoTable.getSelectedRow();
            Binding binding = bindingGroup.getBinding("milestoneInfoBinding");
            binding.unbind();
            aspect.getInfo().getMilestoneInfo().remove(index);
            binding.bind();
            AspectInfoPanel.this.updateUI();
        }

    };

    public Binding findBinding(BindingGroup bindingGroup, Object source, Object target)
    {
        Binding foundBinding = null;
        for(Binding binding : bindingGroup.getBindings())
        {
            if((source == null || binding.getSourceObject() == source) &&
                    (target == null || binding.getTargetObject() == target))
            {
                foundBinding = binding;
            }
        }
        return foundBinding;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        aspectInfo = aspect.getInfo();
        subjectNameLabel = new javax.swing.JLabel();
        activityNameLabel = new javax.swing.JLabel();
        featureNameLabel = new javax.swing.JLabel();
        milestoneNameLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        milestoneInfoTable = new javax.swing.JTable();
        subjectNameTextField = new javax.swing.JTextField();
        activityNameTextField = new javax.swing.JTextField();
        featureNameTextField = new javax.swing.JTextField();
        milestoneNameTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Aspect Information", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP));

        subjectNameLabel.setText("Subject:");

        activityNameLabel.setText("Activity:");

        featureNameLabel.setText("Feature:");

        milestoneNameLabel.setText("Milestone:");

        milestoneInfoTable.setColumnSelectionAllowed(true);

        org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${milestoneInfo}");
        org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, aspectInfo, eLProperty, milestoneInfoTable, "milestoneInfoBinding");
        org.jdesktop.swingbinding.JTableBinding.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${name}"));
        columnBinding.setColumnName("Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${effort}"));
        columnBinding.setColumnName("Effort");
        columnBinding.setColumnClass(java.math.BigInteger.class);
        bindingGroup.addBinding(jTableBinding);
        jTableBinding.bind();
        milestoneInfoTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                milestoneInfoTableMouseClicked(evt);
            }
        });
        milestoneInfoTable.addHierarchyListener(new java.awt.event.HierarchyListener() {
            public void hierarchyChanged(java.awt.event.HierarchyEvent evt) {
                milestoneInfoTableHierarchyChanged(evt);
            }
        });
        milestoneInfoTable.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                milestoneInfoTableVetoableChange(evt);
            }
        });
        jScrollPane1.setViewportView(milestoneInfoTable);
        milestoneInfoTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, aspectInfo, org.jdesktop.beansbinding.ELProperty.create("${subjectName}"), subjectNameTextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        subjectNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                subjectNameTextFieldFocusLost(evt);
            }
        });

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, aspectInfo, org.jdesktop.beansbinding.ELProperty.create("${activityName}"), activityNameTextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        activityNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                activityNameTextFieldFocusLost(evt);
            }
        });

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, aspectInfo, org.jdesktop.beansbinding.ELProperty.create("${featureName}"), featureNameTextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        featureNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                featureNameTextFieldFocusLost(evt);
            }
        });

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, aspectInfo, org.jdesktop.beansbinding.ELProperty.create("${milestoneName}"), milestoneNameTextField, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        milestoneNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                milestoneNameTextFieldFocusLost(evt);
            }
        });

        jLabel1.setText("0");

        jLabel2.setText("Total effort:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(subjectNameLabel)
                            .addComponent(featureNameLabel)
                            .addComponent(milestoneNameLabel)
                            .addComponent(activityNameLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(activityNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE)
                            .addComponent(featureNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE)
                            .addComponent(milestoneNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE)
                            .addComponent(subjectNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(subjectNameLabel)
                    .addComponent(subjectNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(activityNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(activityNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(featureNameLabel)
                    .addComponent(featureNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(milestoneNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(milestoneNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addContainerGap())
        );

        jLabel1.getAccessibleContext().setAccessibleName("totalLabel");

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents
    private void milestoneInfoTableMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_milestoneInfoTableMouseClicked
    {//GEN-HEADEREND:event_milestoneInfoTableMouseClicked
        if(SwingUtilities.isRightMouseButton(evt))
        {
            /*
            try
            {
            Robot robot = new Robot();
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            }
            catch(AWTException ae)
            {
            System.out.println(ae);
            }
             */
            tableEditMenu = new JPopupMenu("Edit Menu");
            JMenuItem addItem = new JMenuItem("Add New Milestone (at end of list)");
            JMenuItem insertItem = new JMenuItem("Insert New Milestone (above this location)");
            JMenuItem deleteItem = new JMenuItem("Delete this Milestone");
            tableEditMenu.add(addItem);
            tableEditMenu.add(insertItem);
            tableEditMenu.add(deleteItem);
            addItem.addActionListener(addMilestoneListener);
            insertItem.addActionListener(insertMilestoneListener);
            deleteItem.addActionListener(deleteMilestoneListener);
            tableEditMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_milestoneInfoTableMouseClicked

    private void subjectNameTextFieldFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_subjectNameTextFieldFocusLost
    {//GEN-HEADEREND:event_subjectNameTextFieldFocusLost
        if(!(subjectNameTextField.getText().trim().equals("")))
        {
            if(aspect.getInfo() == null)
            {
                ObjectFactory of = new ObjectFactory();
                AspectInfo info = of.createAspectInfo();
                aspect.setInfo(info);
            }
            aspect.getInfo().setSubjectName(subjectNameTextField.getText().trim());
        }
    }//GEN-LAST:event_subjectNameTextFieldFocusLost

    private void activityNameTextFieldFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_activityNameTextFieldFocusLost
    {//GEN-HEADEREND:event_activityNameTextFieldFocusLost
        if(!(activityNameTextField.getText().trim().equals("")))
        {
            if(aspect.getInfo() == null)
            {
                ObjectFactory of = new ObjectFactory();
                AspectInfo info = of.createAspectInfo();
                aspect.setInfo(info);
            }
            aspect.getInfo().setActivityName(activityNameTextField.getText().trim());
        }
    }//GEN-LAST:event_activityNameTextFieldFocusLost

    private void featureNameTextFieldFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_featureNameTextFieldFocusLost
    {//GEN-HEADEREND:event_featureNameTextFieldFocusLost
        if(!(featureNameTextField.getText().trim().equals("")))
        {
            if(aspect.getInfo() == null)
            {
                ObjectFactory of = new ObjectFactory();
                AspectInfo info = of.createAspectInfo();
                aspect.setInfo(info);
            }
            aspect.getInfo().setFeatureName(featureNameTextField.getText().trim());
        }
    }//GEN-LAST:event_featureNameTextFieldFocusLost

    private void milestoneNameTextFieldFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_milestoneNameTextFieldFocusLost
    {//GEN-HEADEREND:event_milestoneNameTextFieldFocusLost
        if(!(milestoneNameTextField.getText().trim().equals("")))
        {
            if(aspect.getInfo() == null)
            {
                ObjectFactory of = new ObjectFactory();
                AspectInfo info = of.createAspectInfo();
                aspect.setInfo(info);
            }
            aspect.getInfo().setMilestoneName(milestoneNameTextField.getText().trim());
        }
    }//GEN-LAST:event_milestoneNameTextFieldFocusLost

    private void milestoneInfoTableHierarchyChanged(java.awt.event.HierarchyEvent evt)//GEN-FIRST:event_milestoneInfoTableHierarchyChanged
    {//GEN-HEADEREND:event_milestoneInfoTableHierarchyChanged
        updateEffort();
    }//GEN-LAST:event_milestoneInfoTableHierarchyChanged

    private void milestoneInfoTableVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException//GEN-FIRST:event_milestoneInfoTableVetoableChange
    {//GEN-HEADEREND:event_milestoneInfoTableVetoableChange
        updateEffort();
    }//GEN-LAST:event_milestoneInfoTableVetoableChange

    private void updateEffort()
    {
        BigInteger total = new BigInteger("0");
        for(MilestoneInfo m : aspect.getInfo().getMilestoneInfo())
        {
            total = total.add(m.getEffort());
            jLabel1.setText(total.toString().trim());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel activityNameLabel;
    private javax.swing.JTextField activityNameTextField;
    private com.nebulon.xml.fddi.AspectInfo aspectInfo;
    private javax.swing.JLabel featureNameLabel;
    private javax.swing.JTextField featureNameTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable milestoneInfoTable;
    private javax.swing.JLabel milestoneNameLabel;
    private javax.swing.JTextField milestoneNameTextField;
    private javax.swing.JLabel subjectNameLabel;
    private javax.swing.JTextField subjectNameTextField;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
