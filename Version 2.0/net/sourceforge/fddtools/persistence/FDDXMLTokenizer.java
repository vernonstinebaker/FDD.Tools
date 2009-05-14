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
 * Implement the interface TreeNodeTokenizer to convert lines in MS Project .csv
 * file into objects of Project, MajorFeatureSet FeatureSet or Feature
 */
package net.sourceforge.fddtools.persistence;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;

import javax.swing.tree.MutableTreeNode;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import net.sourceforge.fddtools.model.FDDElement;
import net.sourceforge.fddtools.model.TreeNodeTokenizer;

public class FDDXMLTokenizer extends TreeNodeTokenizer
{
    private Document document = null;
    private ArrayList elementList = new ArrayList();
    private int offset = 0;

    /**
     * Constructors
     * 
     * @param source
     *                  TODO: Document this parameter!
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */

    public FDDXMLTokenizer(String fileName)
            throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(fileName);
        buildList();
    }

    /**
     * walk throught the Tree building a NodeList to be returned (in document
     * order) by findNextNode.
     */

    private void buildList()
    {
        NodeList project = document.getElementsByTagName("Project");
        Element projectElement = (Element) project.item(0);
        elementList.add(projectElement);
        NodeList mfs = projectElement.getElementsByTagName("MajorFeatureSet");
        for (int i1 = 0; i1 < mfs.getLength(); i1++)
        {
            Element majorFeatureSet = (Element) mfs.item(i1);
            elementList.add(majorFeatureSet);
            NodeList featureSets = majorFeatureSet
                    .getElementsByTagName("FeatureSet");
            for (int i2 = 0; i2 < featureSets.getLength(); i2++)
            {
                Element featureSet = (Element) featureSets.item(i2);
                elementList.add(featureSet);
                NodeList features = featureSet.getElementsByTagName("Feature");
                for (int i3 = 0; i3 < features.getLength(); i3++)
                {
                    Element feature = (Element) features.item(i3);
                    elementList.add(feature);
                }
            }
        }
    }

    protected MutableTreeNode findNextNode() throws IOException
    {
        MutableTreeNode fddNode = null;
        String name = null;
        int progress = 0;
        Date targetMonth = new Date();
        String owner = null;
        String className = "net.sourceforge.fddtools.model.";

        if (offset < elementList.size())
        {
            try
            {
                Element element = (Element) elementList.get(offset++);
                className += element.getTagName();

                NodeList nameNode = element.getElementsByTagName("Name");
                Element nameElement = (Element) nameNode.item(0);
                if (nameElement.hasChildNodes())
                    name = nameElement.getFirstChild().getNodeValue();

                NodeList progressNode = element
                        .getElementsByTagName("Progress");
                Element progressElement = (Element) progressNode.item(0);
                if (progressElement.hasChildNodes())
                    progress = Integer.parseInt(progressElement.getFirstChild()
                            .getNodeValue());

                NodeList ownerNode = element.getElementsByTagName("Owner");
                Element ownerElement = (Element) ownerNode.item(0);
                if (ownerElement.hasChildNodes())
                    owner = ownerElement.getFirstChild().getNodeValue();
                else
                    owner = "";

                NodeList targetMonthNode = element
                        .getElementsByTagName("TargetMonth");
                Element targetMonthElement = (Element) targetMonthNode.item(0);
                if (targetMonthElement.hasChildNodes())
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    targetMonth = formatter.parse(targetMonthElement.getFirstChild().getNodeValue());
                }

                fddNode = (MutableTreeNode) Class.forName(className)
                        .newInstance();
                ((FDDElement) fddNode).setName(name);
                ((FDDElement) fddNode).setProgress(progress);
                ((FDDElement) fddNode).setTargetMonth(targetMonth);
                ((FDDElement) fddNode).setOwner(owner);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return fddNode;
    }
}