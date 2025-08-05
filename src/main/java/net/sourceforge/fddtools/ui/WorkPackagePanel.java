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

import com.nebulon.xml.fddi.Project;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import net.sourceforge.fddtools.fddi.extension.WorkPackage;
import net.sourceforge.fddtools.fddi.extension.WorkPackageObjectFactory;
import org.jdesktop.beansbinding.Binding;



public class WorkPackagePanel extends JPanel
{
    private static final String WORKPACKAGE_BINDING = "workPackageBinding";

    private WorkPackageObjectFactory of = new WorkPackageObjectFactory();
    private Project project;

    public WorkPackagePanel(Project projectIn)
    {
        project = projectIn;

        initComponents();

        jScrollPane1.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                jScrollPaneMouseClicked(evt);
            }
        });
    }

    private ActionListener addWorkPackageListener = new ActionListener()
    {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
            WorkPackage wp = of.createWorkPackage();
            wp.setName(java.util.ResourceBundle.getBundle("messages").getString("WorkpackagePanel.EditWorkpackageName"));
            Binding<?, ?, ?, ?> binding = bindingGroup.getBinding(WORKPACKAGE_BINDING);
            binding.unbind();
            project1.getAny().add(wp);
            binding.bind();
        }
    };

    private ActionListener insertWorkPackageListener = new ActionListener()
    {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
            WorkPackage wp = of.createWorkPackage();
            wp.setName(java.util.ResourceBundle.getBundle("messages").getString("WorkpackagePanel.EditWorkpackageName"));
            int index = workPackageTable.getSelectedRow() >= 0 ? workPackageTable.getSelectedRow() : 0;
            Binding<?, ?, ?, ?> binding = bindingGroup.getBinding(WORKPACKAGE_BINDING);
            binding.unbind();
            WorkPackage wpIndex = project1.getWorkPackages().get(index);
            int anyIndex = project1.getAny().indexOf(wpIndex);
            project1.getAny().add(anyIndex, wp);
            binding.bind();
        }
    };

    private ActionListener deleteWorkPackageListener = new ActionListener()
    {

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            int index = workPackageTable.getSelectedRow() >= 0 ? workPackageTable.getSelectedRow() : 0;
            Binding<?, ?, ?, ?> binding = bindingGroup.getBinding(WORKPACKAGE_BINDING);
            binding.unbind();
            WorkPackage wp = project1.getWorkPackages().get(index);
            project1.getAny().remove(wp);
            binding.bind();
        }
    };


    private void jScrollPaneMouseClicked(java.awt.event.MouseEvent evt)
    {
        if(SwingUtilities.isRightMouseButton(evt))
        {
            JPopupMenu addWorkPackageMenu = new JPopupMenu(java.util.ResourceBundle.getBundle("messages").getString("WorkpackagePanel.EditMenu"));
            JMenuItem addItem = new JMenuItem(java.util.ResourceBundle.getBundle("messages").getString("WorkpackagePanel.AddWorkpackage"));
            addWorkPackageMenu.add(addItem);
            addItem.addActionListener(addWorkPackageListener);
            addWorkPackageMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        project1 = project;
        jScrollPane1 = new javax.swing.JScrollPane();
        workPackageTable = new javax.swing.JTable();

        org.jdesktop.beansbinding.ELProperty<Project, java.util.List<WorkPackage>> eLProperty = org.jdesktop.beansbinding.ELProperty.create("${workPackages}");
        org.jdesktop.swingbinding.JTableBinding<WorkPackage, Project, javax.swing.JTable> jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, project1, eLProperty, workPackageTable, "workPackageBinding");
        org.jdesktop.swingbinding.JTableBinding<WorkPackage, Project, javax.swing.JTable>.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${name}"));
        columnBinding.setColumnName("Name");
        columnBinding.setColumnClass(String.class);
        columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${initials}"));
        columnBinding.setColumnName("Initials");
        columnBinding.setColumnClass(String.class);
        bindingGroup.addBinding(jTableBinding);
        jTableBinding.bind();
        workPackageTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                workPackageTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(workPackageTable);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("messages"); // NOI18N
        workPackageTable.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("WorkpackagePanel.NameColumnHeader")); // NOI18N
        workPackageTable.getColumnModel().getColumn(1).setHeaderValue(bundle.getString("WorkpackagePanel.InitialsColumnHeader")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void workPackageTableMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_workPackageTableMouseClicked
    {//GEN-HEADEREND:event_workPackageTableMouseClicked
        if(SwingUtilities.isRightMouseButton(evt))
        {
            JPopupMenu tableEditMenu = new JPopupMenu(java.util.ResourceBundle.getBundle("messages").getString("WorkpackagePanel.EditMenu"));
            JMenuItem addItem = new JMenuItem(java.util.ResourceBundle.getBundle("messages").getString("WorkpackagePanel.AddWorkpackageAtEnd"));
            JMenuItem insertItem = new JMenuItem(java.util.ResourceBundle.getBundle("messages").getString("WorkpackagePanel.InsertWorkpackage"));
            JMenuItem deleteItem = new JMenuItem(java.util.ResourceBundle.getBundle("messages").getString("WorkpackagePanel.DeleteWorkpackage"));
            tableEditMenu.add(addItem);
            tableEditMenu.add(insertItem);
            tableEditMenu.add(deleteItem);
            addItem.addActionListener(addWorkPackageListener);
            insertItem.addActionListener(insertWorkPackageListener);
            deleteItem.addActionListener(deleteWorkPackageListener);
            tableEditMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_workPackageTableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private com.nebulon.xml.fddi.Project project1;
    private javax.swing.JTable workPackageTable;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
