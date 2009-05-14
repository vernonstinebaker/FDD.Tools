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
 * Implement the interface TreeNodeTokenizer to convert lines in MS Project
 * .csv file into objects of Project, MajorFeatureSet FeatureSet or Feature
 */
package net.sourceforge.fddtools.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.tree.MutableTreeNode;

import net.sourceforge.fddtools.model.FDDElement;
import net.sourceforge.fddtools.model.TreeNodeTokenizer;

public class FDDCSVTokenizer extends TreeNodeTokenizer
{
    public final static int QUOTE_CHAR = (int) '"';
    public final static int FIELD_SEPARATOR = (int) ',';
    public final static int SPACE_BAR = (int) ' ';
    public final static int DATE_SEPARATOR = (int) '/';
    public final static String[] LEVEL_VS_CLASSNAME = 
                                                      {
                                                          "net.sourceforge.fddtools.model.Project",
                                                          "net.sourceforge.fddtools.model.MajorFeatureSet",
                                                          "net.sourceforge.fddtools.model.FeatureSet",
                                                          "net.sourceforge.fddtools.model.Feature"
                                                      };
    private BufferedReader source = null;
    private String ROOT_NAME = "Develop";

    /** Level in csv file of root whose name match the specified one */
    private int rootLevel = 0;

    /**
     * This member variable is to deal with new CVS format. Since FDD tree is
     * just part of the input file, we need a flag whether root has been found
     * or not.
     */
    private boolean rootFound = false;
    private boolean treeOver = false;

    /**
     * Constructors
     *
     * @param source TODO: Document this parameter!
     */
    public FDDCSVTokenizer(Reader source)
    {
        this.source = new BufferedReader(source);
        rootFound = false;
        treeOver = false;
    }

    public FDDCSVTokenizer(Reader source, String rootName)
    {
        this(source);

        if (null != rootName)
        {
            this.ROOT_NAME = rootName;
        }
    }

    protected MutableTreeNode findNextNode() throws IOException
    {
        if (null == source)
        {
            return null;
        }

        if (treeOver)
        {
            return null;
        }

        String currentLine = null;
        int validLines = 0;

        while (null != (currentLine = source.readLine())) //there's still line remaining
        {
            try
            {
                Integer.parseInt(currentLine.substring(0, 1));
            }
            catch (NumberFormatException badFormat) //If line doesn't begin with numbers, we silently ingore it
            {
                continue;
            }
            
            if (!isValidLine(currentLine))
            {
                throw new IOException();
            }
            else
            {
                validLines++;
            }

            String cleanLine = replaceSeparatorInQuotation(currentLine);
            StringTokenizer lineParser = new StringTokenizer(cleanLine, ",");

                // At last, we begin to construct a node from a line
                int outlineLevel = Integer.parseInt(lineParser.nextToken().trim());
                String name = lineParser.nextToken().trim();

                // Following code is added to adpat new CVS format
                if (!rootFound)
                {
                    if (ROOT_NAME.equalsIgnoreCase(name)) // root is found
                    {
                        rootLevel = outlineLevel; // assign the root level
                        rootFound = true;
                    }
                    else
                    {
                        continue;
                    }
                }
                else
                {
                    if (rootLevel >= outlineLevel)
                    {
                        treeOver = true;

                        return null;
                    }

                    // Control node's level to meet LEVEL_VS_CLASSNAME bound
                    if (outlineLevel >= (rootLevel + LEVEL_VS_CLASSNAME.length))
                    {
                        continue;
                    }
                }

                String progressInPercent = lineParser.nextToken().trim();
                int progress = Integer.parseInt(progressInPercent.substring(0,
                                                                            progressInPercent.length() -
                                                                            1));

                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
                ParsePosition pos = new ParsePosition(0);
                Date targetMonth = formatter.parse(lineParser.nextToken().trim()
                                                             .substring(4), pos);

                String owner = null;

                if (lineParser.hasMoreTokens()) //Owner name is optional
                {
                    owner = lineParser.nextToken().trim();
                }
                else
                {
                    owner = "";
                }

                MutableTreeNode node = null;
                try
                {
                    node = (MutableTreeNode) Class.forName(LEVEL_VS_CLASSNAME[outlineLevel -
                                                                           rootLevel])
                                                                  .newInstance();
                }
                catch (InstantiationException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (ClassNotFoundException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ((FDDElement) node).setName(name);
                ((FDDElement) node).setProgress(progress);
                ((FDDElement) node).setTargetMonth(targetMonth);
                ((FDDElement) node).setOwner(owner);

                return node;
            }
        
        if (0 == validLines)
        {
            throw new IOException();
        }


        return null; //end of file
    }

    /**
     * @param currentLine
     * @return
     */
    private boolean isValidLine(String currentLine)
    {
        String cleanLine = replaceSeparatorInQuotation(currentLine);
        StringTokenizer lineParser = new StringTokenizer(cleanLine, ",");
        if (lineParser.countTokens()>=3)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * replace all the ',' inside pair of '"' with space
     *
     * @param input TODO: Document this parameter!
     *
     * @return TODO: Document this return value!
     */
    private String replaceSeparatorInQuotation(String input)
    {
        StringBuffer buffer = new StringBuffer(input);
        int openQuotePos = -1;
        int closeQuotePos = -1;

        while (-1 != (openQuotePos = input.indexOf(QUOTE_CHAR, closeQuotePos +
                                                       1)))
        {
            if (-1 == (closeQuotePos = input.indexOf(QUOTE_CHAR,
                                                         openQuotePos + 1)))
            {
                break;
            }

            int comaInQuotePos = input.indexOf(FIELD_SEPARATOR, openQuotePos +
                                               1);

            while ((openQuotePos < comaInQuotePos) &&
                       (comaInQuotePos < closeQuotePos))
            {
                buffer.setCharAt(comaInQuotePos, (char) SPACE_BAR);
                comaInQuotePos = input.indexOf(FIELD_SEPARATOR,
                                               comaInQuotePos + 1);
            }
        }

        return new String(buffer);
    }
}
