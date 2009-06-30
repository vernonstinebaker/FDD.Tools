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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import net.sourceforge.fddtools.internationalization.Messages;

public class FDDOptionView extends JFrame
{
	
	
    /**
     * Constants definition
     */
    public final int maxPicWidth = 10000;
    public final int minPicWidth = 100;
    public final int maxPicHeight = 10000;
    public final int minPicHeight = 100;
    public final int maxImageWidth = 500;
    public final int minImageWidth = 30;
    public final int maxImageHeight = 500;
    public final int minImageHeight = 30;


    protected FDDOptionModel effectiveOptions = null;
    protected FDDOptionModel showedOptions = null;


    /**
     * All available font in System
     */
    protected static final Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

    /**
     * define Font style
     */
    private static final HashMap<String, Integer> styleMap = new HashMap<String, Integer>();

    /**
     * The Maximum Font size
     */
    public static final int maxFontSize = 50;


    /**
     * GUI staff
     */
    private JTextField picWidth = null;
    private JTextField picHeight = null;
    private JTextField imageWidth = null;
    private JTextField imageHeight = null;

    private JComboBox fontName = null;
    private JComboBox fontSize = null;
    private JComboBox fontStyle = null;

    private JLabel sampleText = new JLabel( "ABC abc" );

    private JButton applyButton = null;

    public FDDOptionView( FDDOptionModel model, String title )
    {
        super( title );

        styleMap.put(Messages.getInstance().getMessage(Messages.FONTSTYLE_PLAIN), new Integer(Font.PLAIN) );
        styleMap.put(Messages.getInstance().getMessage(Messages.FONTSTYLE_BOLD), new Integer(Font.BOLD) );
        styleMap.put(Messages.getInstance().getMessage(Messages.FONTSTYLE_ITALIC), new Integer(Font.ITALIC) );
        styleMap.put(Messages.getInstance().getMessage(Messages.FONTSTYLE_BOLD_ITALIC), new Integer(Font.ITALIC + Font.BOLD) );

        this.effectiveOptions = model;
        this.showedOptions = (FDDOptionModel) model.clone();

        JPanel bottonPane = bottomBottons();

        //There're 2 tabs now, one for display properties, one for input parsing
        JPanel optionPane = optionItems();
        getContentPane().add( bottonPane, BorderLayout.SOUTH );
        getContentPane().add( optionPane, BorderLayout.CENTER );
        applyButton.setEnabled( false );
        pack();
    }

    protected JPanel bottomBottons()
    {
        JPanel pane = new JPanel();
        pane.setLayout( new BoxLayout( pane, BoxLayout.X_AXIS ) );
        pane.setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );

        JButton okButton = new JButton( "OK" );
        okButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    confirmChange();
                    FDDOptionView.this.dispose();
                    //setVisible( false );
                }
            } );

        JButton cancelButton = new JButton(Messages.getInstance().getMessage(Messages.JBUTTON_CANCEL_CAPTION));
        cancelButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    setVisible( false );
                }
            } );

        this.applyButton = new JButton(Messages.getInstance().getMessage(Messages.JBUTTON_APPLY_CAPTION));
        applyButton.addActionListener(
            new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    confirmChange();
                }
            } );

        pane.add( Box.createHorizontalGlue() );
        pane.add( okButton );
        pane.add( Box.createRigidArea( new Dimension(10, 0) ) );
        pane.add( cancelButton );
        pane.add( Box.createRigidArea( new Dimension(10, 0) ) );
        pane.add( applyButton );

        return pane ;
    }

    private void confirmChange()
    {
        applyButton.setEnabled( false );
        effectiveOptions.valueChangeTo( showedOptions );
    }

    protected JPanel optionItems()
    {

        // Add all tabs together
        JTabbedPane tabbedPane = new JTabbedPane();
//        tabbedPane.add( "Dimension", dimItems() );
        tabbedPane.add(Messages.getInstance().getMessage(Messages.JTABBEDPANE_FONT_TITLE), fontItems() );
        JPanel pane = new JPanel();
        pane.add( tabbedPane );

        return pane;
    }

    protected JPanel dimItems()
    {
      /**
       * Picture's dimension
       */
        picWidth = new JTextField(4);
        picWidth.setText( String.valueOf( effectiveOptions.getPicSize().getWidth() ) );
        NumberValidator picWidthValidator = new NumberValidator( picWidth, "Picture width", maxPicWidth, minPicWidth );
        picWidth.addActionListener( picWidthValidator );
        picHeight = new JTextField(4);
        picHeight.setText( String.valueOf( effectiveOptions.getPicSize().getHeight() ) );
        NumberValidator picHeightValidator = new NumberValidator( picHeight, "Picture height", maxPicHeight, minPicHeight );
        picHeight.addActionListener( picHeightValidator );
        JPanel picTextPane = new JPanel();
        picTextPane.setLayout( new GridLayout(0, 1) );
        picTextPane.add( picWidth );
        picTextPane.add( picHeight );

        JPanel picLabelPane = new JPanel();
        picLabelPane.setLayout( new GridLayout(0, 1) );
        picLabelPane.add( new JLabel( "Width" ) );
        picLabelPane.add( new JLabel( "Height" ) );

        JPanel picDimPane = new JPanel();
        picDimPane.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), "Picture size") );
        picDimPane.add( picLabelPane, BorderLayout.CENTER );
        picDimPane.add( picTextPane, BorderLayout.EAST );

        /**
         * Image's dimension
         */
        imageWidth = new JTextField(3);
        imageWidth.setText( String.valueOf( effectiveOptions.getImageSize().getWidth() ) );
        NumberValidator imageWidthValidator = new NumberValidator( imageWidth, "Image width", maxImageWidth, minImageWidth );
        imageWidth.addActionListener( imageWidthValidator );
        imageHeight = new JTextField(4);
        imageHeight.setText( String.valueOf( effectiveOptions.getImageSize().getHeight() ) );
        NumberValidator imageHeightValidator = new NumberValidator( imageHeight, "Image height", maxImageHeight, minImageHeight );
        imageHeight.addActionListener( imageHeightValidator );
        JPanel imageTextPane = new JPanel();
        imageTextPane.setLayout( new GridLayout(0, 1) );
        imageTextPane.add( imageWidth );
        imageTextPane.add( imageHeight );

        JPanel imageLabelPane = new JPanel();
        imageLabelPane.setLayout( new GridLayout(0, 1) );
        imageLabelPane.add( new JLabel( "Width" ) );
        imageLabelPane.add( new JLabel( "Height" ) );

        JPanel imageDimPane = new JPanel();
        imageDimPane.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), "Image size") );
        imageDimPane.add( imageLabelPane, BorderLayout.CENTER );
        imageDimPane.add( imageTextPane, BorderLayout.EAST );

        // Add into display optionsModel tab
        JPanel disOptionsPane = new JPanel();
        disOptionsPane.setLayout(new BoxLayout( disOptionsPane, BoxLayout.X_AXIS));
        disOptionsPane.add( picDimPane );
        disOptionsPane.add( imageDimPane );

        return disOptionsPane;
    }

    protected JPanel fontItems()
    {
        FontChangeListener fcl = new FontChangeListener();

        JPanel pane = new JPanel();
        pane.setLayout( new BorderLayout() );

        JPanel fontPane = new JPanel();
        fontPane.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), "Font" ) );
        fontPane.setLayout( new BoxLayout( fontPane, BoxLayout.Y_AXIS ) );

        fontPane.add(new JLabel(Messages.getInstance().getMessage(Messages.JLABEL_FONTFAMILY_CAPTION)));
        // Add Font names to ComboBox
        Vector<String> names = new Vector<String>();
        for( int i = 0; i < allFonts.length; i ++ )
            names.add( allFonts[i].getFontName() );
        fontName = new JComboBox( names );
        fontName.addActionListener( fcl );
        fontPane.add( fontName );

        fontPane.add(new JLabel(Messages.getInstance().getMessage(Messages.JLABEL_FONTSIZE_CAPTION)));
        // Add all possible font size to ComboBox
        Vector<Integer> sizes = new Vector<Integer>();
        for( int i = 0; i < maxFontSize; i++ )
            sizes.add( new Integer(i+1) );
        fontSize = new JComboBox( sizes );
        fontSize.addActionListener( fcl );
        fontPane.add( fontSize );

        fontPane.add(new JLabel(Messages.getInstance().getMessage(Messages.JLABEL_FONTSTYLE_CAPTION)));
        // Add all possible font styles to ComboBox
        fontStyle = new JComboBox(new Vector<String>( styleMap.keySet() ) );
        fontStyle.addActionListener( fcl );
        fontPane.add( fontStyle );

        JPanel samplePane = new JPanel();
        samplePane.setLayout( new BorderLayout() );
        samplePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getInstance().getMessage(Messages.TITLEBORDER_SAMPLETEXT_CAPTION)));
        samplePane.add( sampleText, BorderLayout.CENTER );
        sampleText.setPreferredSize( new Dimension( 50, 50 ) );
        sampleText.setHorizontalAlignment(JLabel.CENTER);

        pane.add( fontPane, BorderLayout.NORTH );
        pane.add( samplePane, BorderLayout.CENTER );
        fontName.setSelectedItem( effectiveOptions.getTextFont().getFontName() );
        fontSize.setSelectedItem( new Integer( effectiveOptions.getTextFont().getSize() ) );

        Iterator keys = styleMap.keySet().iterator() ;
        while( keys.hasNext() )
        {
            String theStyle = (String) keys.next();
            if( effectiveOptions.getTextFont().getStyle() == ((Integer) ( styleMap.get( theStyle ) )).intValue() )
                fontStyle.setSelectedItem( theStyle );
        }

        return pane;
    }

    private class FontChangeListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            Font tmpFont =  allFonts[ fontName.getSelectedIndex() ];
            int style = ((Integer) styleMap.get( fontStyle.getSelectedItem() )).intValue() ;
            float size = (float) ( (Integer) fontSize.getSelectedItem() ).intValue();
            showedOptions.setTextFont( tmpFont.deriveFont( style, size ) );
            sampleText.setFont( showedOptions.getTextFont() );
            sampleText.repaint();

            applyButton.setEnabled( true );
        }
    }

    /**
     * NumberChecker: check whether the text in textfield is all digits and
     *   in specified range.
     */

    private class NumberValidator
            implements ActionListener
    {
        private int largest = Integer.MAX_VALUE;
        private int smallest = Integer.MIN_VALUE;

        private JTextComponent textComp = null;
        private String compName = null;

        public NumberValidator( JTextComponent textComp, String compName, int largest, int smallest )
        {
            this.textComp = textComp;
            this.largest = largest;
            this.smallest = smallest;
            this.compName = compName;
        }

        public NumberValidator( JTextComponent textComp, String compName )
        {
            this( textComp, compName, Integer.MAX_VALUE, Integer.MIN_VALUE );
        }

        public boolean validate()
        {
            String text = textComp.getText() ;
            int value = 0;
            try
            {
                value = Integer.parseInt( text.trim() );
            }
            catch( NumberFormatException e )
            {
                JOptionPane.showConfirmDialog(
                    textComp,
                    compName + " should be numbers ",
                    "Option Dialog",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE ) ;

                textComp.grabFocus();
                return false;
            }

            if( value > largest || value < smallest )
            {
                JOptionPane.showConfirmDialog(
                    textComp,
                    compName + " should be int the range of " + largest + " and " + smallest,
                    "Option Dialog",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE ) ;

                textComp.grabFocus();
                return false;
            }

            return true ;
        }

        public void actionPerformed( ActionEvent e )
        {
            if( validate() )
                applyButton.setEnabled( true );
        }
    }
}


