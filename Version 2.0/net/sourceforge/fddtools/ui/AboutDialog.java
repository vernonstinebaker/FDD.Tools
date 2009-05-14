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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import net.sourceforge.fddtools.internationalization.Messages;

class AboutDialog extends JDialog
{
	
	// > Internationalization keys
	
	private static final String TITLE = "AboutDialog.Title";
	
	private static final String JBUTTON_OK_CAPTION = "AboutDialog.JButtonOk.Caption";
	
	private static final String JPANEL_ABOUT_CAPTION = "AboutDialog.JPanelAbout.Caption";
	
	private static final String JPANEL_COPYRIGHT_CAPTION = "AboutDialog.JPanelCopyright.Caption";
	
	// < End internationalization keys
	
    /**
     * Creates an instance of AboutDialog.
     * 
     * @param parent
     *            The parent container
     */
    public AboutDialog(final JFrame parent)
    {
        super(parent, Messages.getInstance().getMessage(TITLE), true);
        getContentPane().setLayout(new BorderLayout());
        JTabbedPane aboutTabbedPane = new JTabbedPane();
        getContentPane().add(aboutTabbedPane, BorderLayout.CENTER);

        JPanel aboutPanel = new JPanel();
        //          JLabel fddIcon = new JLabel(new ImageIcon("fddtools.gif"));
        //          p.add(fddIcon);
        //          aboutTabbedPane.add(p, BorderLayout.WEST);
        String message = "FDD Tools Version 2.0.0\n\n" + "FDD Tools supports Project Tracking using the\n"
                + "Feature Driven Development methodology.\n\n"
                + "Released under the Apache Software License 1.1\n\n" + "Contributors:\n"
                + "\tAndres Acosta\n" + "\tJames Hwong\n" + "\tKenneth Jiang\n" + "\tOuyang Jiezi\n" + "\tVernon Stinebaker\n";
        JTextArea txt = new JTextArea(message);
        txt.setEditable(false);

        JPanel btnPanel = new JPanel();
        JButton btOK = new JButton(Messages.getInstance().getMessage(JBUTTON_OK_CAPTION));
        ActionListener lst = new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                dispose();
            }
        };
        btOK.addActionListener(lst);

        btnPanel.add(btOK);

        aboutPanel.setLayout(new BorderLayout());
        aboutPanel.add(txt, BorderLayout.CENTER);
        aboutPanel.setName(Messages.getInstance().getMessage(JPANEL_ABOUT_CAPTION));

        aboutTabbedPane.add(aboutPanel);

        JPanel copyrightPanel = new JPanel();

        message = "FDD Tools\n\tCopyright (c) 2004 - 2009 Andres Acosta, James Hwong,\n\tKenneth Jiang, Ouyang Jiezi, Vernon Stinebaker\n\tAll rights reserved.\n"
                + "Apache CLI\n\tCopyright (c) 1999-2009 The Apache Software Foundation.\n\tAll rights reserved.\n"
                + "ExtensionFileFilter\n\tCopyright (c) 2001 Marty Hall and Larry Brown\n\t(http://www.corewebprogramming.com)\n\t All rights reserved.\n"
                + "OSXAdapter\n\tCopyright (c) 2005 Apple Computer, Inc.,\n\tAll Rights Reserved";
        txt = new JTextArea(message);
        txt.setEditable(false);
        copyrightPanel.add(txt);
        copyrightPanel.setName(Messages.getInstance().getMessage(JPANEL_COPYRIGHT_CAPTION));
        aboutTabbedPane.add(copyrightPanel);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
    }
}