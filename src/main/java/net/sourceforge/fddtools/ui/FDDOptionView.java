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
/**
 * Description: View and Control corresponding to FDDOptionModel
 * @author Kenneth Jiang  3/13/2001   created
 * @version 1.0
 */
package net.sourceforge.fddtools.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import net.sourceforge.fddtools.internationalization.Messages;

public class FDDOptionView extends JFrame
{

    public static final int MAX_FONT_SIZE = 50;
    private FDDOptionModel effectiveOptions = null;
    private FDDOptionModel showedOptions = null;
    private static final Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    private static final Map<String, Integer> styleMap = new HashMap<String, Integer>();
    private JComboBox fontName = null;
    private JComboBox fontSize = null;
    private JComboBox fontStyle = null;
    private JLabel sampleText = new JLabel("ABC abc");
    private JButton applyButton = null;

    public FDDOptionView(FDDOptionModel model, String title)
    {
        super(title);

        styleMap.put(Messages.getInstance().getMessage(Messages.FONTSTYLE_PLAIN), new Integer(Font.PLAIN));
        styleMap.put(Messages.getInstance().getMessage(Messages.FONTSTYLE_BOLD), new Integer(Font.BOLD));
        styleMap.put(Messages.getInstance().getMessage(Messages.FONTSTYLE_ITALIC), new Integer(Font.ITALIC));
        styleMap.put(Messages.getInstance().getMessage(Messages.FONTSTYLE_BOLD_ITALIC), new Integer(Font.ITALIC + Font.BOLD));

        this.effectiveOptions = model;
        this.showedOptions = (FDDOptionModel) model.clone();

        JPanel bottonPane = bottomBottons();

        JPanel optionPane = optionItems();
        getContentPane().add(bottonPane, BorderLayout.SOUTH);
        getContentPane().add(optionPane, BorderLayout.CENTER);
        applyButton.setEnabled(false);
        pack();
    }

    protected JPanel bottomBottons()
    {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        pane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton okButton = new JButton(Messages.getInstance().getMessage(Messages.JBUTTON_OK_CAPTION));
        okButton.addActionListener(
                new ActionListener()
                {

                    public void actionPerformed(ActionEvent e)
                    {
                        confirmChange();
                        FDDOptionView.this.dispose();
                    }
                });

        JButton cancelButton = new JButton(Messages.getInstance().getMessage(Messages.JBUTTON_CANCEL_CAPTION));
        cancelButton.addActionListener(
                new ActionListener()
                {

                    public void actionPerformed(ActionEvent e)
                    {
                        setVisible(false);
                    }
                });

        this.applyButton = new JButton(Messages.getInstance().getMessage(Messages.JBUTTON_APPLY_CAPTION));
        applyButton.addActionListener(
                new ActionListener()
                {

                    public void actionPerformed(ActionEvent e)
                    {
                        confirmChange();
                    }
                });

        pane.add(Box.createHorizontalGlue());
        pane.add(okButton);
        pane.add(Box.createRigidArea(new Dimension(10, 0)));
        pane.add(cancelButton);
        pane.add(Box.createRigidArea(new Dimension(10, 0)));
        pane.add(applyButton);

        return pane;
    }

    private void confirmChange()
    {
        applyButton.setEnabled(false);
        effectiveOptions.valueChangeTo(showedOptions);
    }

    protected JPanel optionItems()
    {

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(Messages.getInstance().getMessage(Messages.JTABBEDPANE_FONT_TITLE), fontItems());
        JPanel pane = new JPanel();
        pane.add(tabbedPane);

        return pane;
    }

    protected JPanel fontItems()
    {
        FontChangeListener fcl = new FontChangeListener();

        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

        JPanel fontPane = new JPanel();
        fontPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Font"));
        fontPane.setLayout(new BoxLayout(fontPane, BoxLayout.Y_AXIS));

        fontPane.add(new JLabel(Messages.getInstance().getMessage(Messages.JLABEL_FONTFAMILY_CAPTION)));
        // Add Font names to ComboBox
        Vector<String> names = new Vector<String>();
        for(int i = 0; i < allFonts.length; i++)
        {
            names.add(allFonts[i].getFontName());
        }
        fontName = new JComboBox(names);
        fontName.addActionListener(fcl);
        fontPane.add(fontName);

        fontPane.add(new JLabel(Messages.getInstance().getMessage(Messages.JLABEL_FONTSIZE_CAPTION)));
        // Add all possible font size to ComboBox
        Vector<Integer> sizes = new Vector<Integer>();
        for(int i = 0; i < MAX_FONT_SIZE; i++)
        {
            sizes.add(new Integer(i + 1));
        }
        fontSize = new JComboBox(sizes);
        fontSize.addActionListener(fcl);
        fontPane.add(fontSize);

        fontPane.add(new JLabel(Messages.getInstance().getMessage(Messages.JLABEL_FONTSTYLE_CAPTION)));
        // Add all possible font styles to ComboBox
        fontStyle = new JComboBox(new Vector<String>(styleMap.keySet()));
        fontStyle.addActionListener(fcl);
        fontPane.add(fontStyle);

        JPanel samplePane = new JPanel();
        samplePane.setLayout(new BorderLayout());
        samplePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getInstance().getMessage(Messages.TITLEBORDER_SAMPLETEXT_CAPTION)));
        samplePane.add(sampleText, BorderLayout.CENTER);
        sampleText.setPreferredSize(new Dimension(50, 50));
        sampleText.setHorizontalAlignment(JLabel.CENTER);

        pane.add(fontPane, BorderLayout.NORTH);
        pane.add(samplePane, BorderLayout.CENTER);
        fontName.setSelectedItem(effectiveOptions.getTextFont().getFontName());
        fontSize.setSelectedItem(new Integer(effectiveOptions.getTextFont().getSize()));

        Iterator keys = styleMap.keySet().iterator();
        while(keys.hasNext())
        {
            String theStyle = (String) keys.next();
            if(effectiveOptions.getTextFont().getStyle() == ((Integer) (styleMap.get(theStyle))).intValue())
            {
                fontStyle.setSelectedItem(theStyle);
            }
        }

        return pane;
    }

    private class FontChangeListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            Font tmpFont = allFonts[fontName.getSelectedIndex()];
            int style = ((Integer) styleMap.get(fontStyle.getSelectedItem())).intValue();
            float size = (float) ((Integer) fontSize.getSelectedItem()).intValue();
            showedOptions.setTextFont(tmpFont.deriveFont(style, size));
            sampleText.setFont(showedOptions.getTextFont());
            sampleText.repaint();

            applyButton.setEnabled(true);
        }
    }
}


