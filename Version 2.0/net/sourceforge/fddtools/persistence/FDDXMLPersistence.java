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

package net.sourceforge.fddtools.persistence;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

import net.sourceforge.fddtools.model.*;

/** 
 *   Manages persistence of an FDD Project to/from XML
 *
 *   The basic format for the XML file is:
 *
 *   <FDDProject>
 *       <Project>
 *           <Name>Project Phase -- usually develop.  Matches the Develop phase in the MS Project file</Name>
 *           <Progress>%complete</Progress>
 *           <Owner>Project Owner</Owner>
 *           <TargetMonth>Target Month for Completion</TargetMonth>
 *           <MajorFeatureSet>
 *               <Name>Major Feature Set Name</Name>
 *               <Progress>%complete</Progress>
 *               <Owner>Chief Programmer</Owner>
 *               <TargetMonth>Target Month for Completion</TargetMonth>
 *               <FeatureSet>
 *                   <Name>Feature Set Name</Name>
 *                   <Progress>%complete</Progress>
 *                   <Owner>Chief Programmer</Owner>
 *                   <TargetMonth>Target Month for Completion</TargetMonth>
 *                   <Feature>
 *                       <Name>Feature Name</Name>
 *                       <Progress>%complete</Progress>
 *                       <Owner>Chief Programmer/Developer</Owner>
 *                       <TargetMonth>Target Month for Completion</TargetMonth>
 *                   </Feature>
 *               </FeatureSet>
 *           </MajorFeatureSet>
 *       </Project>
 *   </FDDProject>
 *  
 */

public class FDDXMLPersistence
{
    private Document document = null;
    
    /** 
     *   Creates a new instance of XMLFile -- initializes XML document
     */
    public FDDXMLPersistence()
    {
        try
        {  
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();
            document = impl.createDocument(null, "FDDProject",  null);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     *    calls to build XML file and then persists to specified filename
     */
    
    public void store(DefaultFDDModel model, String fileName)
    {
        try
        {          
            buildXML(model); 
            PrintWriter out = new PrintWriter(new FileWriter(fileName));            
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(out);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
//            serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"fddtools.dtd");
            serializer.setOutputProperty(OutputKeys.INDENT,"YES");
            serializer.transform(domSource, streamResult); 
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    /**
     *    Formats an XML Element to match a FDDElement details
     */
    
    private Element buildNode(FDDElement fddElement)
    {
        Text textNode = null;
        
        String fddElementType = fddElement.getClass().getName();
        fddElementType = fddElementType.substring(fddElementType.lastIndexOf(".") + 1);
        Element nodeType = document.createElement(fddElementType);
        
        Element nodeName = document.createElement("Name");
        textNode = document.createTextNode(fddElement.getName());
        nodeName.appendChild(textNode);
        nodeType.appendChild(nodeName);
        
        Element featureProgress = document.createElement("Progress");
        textNode = document.createTextNode(new Integer(fddElement.getProgress()).toString());
        featureProgress.appendChild(textNode);
        nodeType.appendChild(featureProgress);
        
        Element featureOwner = document.createElement("Owner");
        textNode = document.createTextNode(fddElement.getOwner());
        featureOwner.appendChild(textNode);
        nodeType.appendChild(featureOwner);
        
        Element featureTargetMonth = document.createElement("TargetMonth");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        textNode = document.createTextNode(formatter.format(fddElement.getTargetMonth()));
        featureTargetMonth.appendChild(textNode);
        nodeType.appendChild(featureTargetMonth);
        
        return nodeType;
    }
    
    /**
     *     Recursively constructs XML Document from FDDModel Elements
     */
    
    private void addNodeToDocument(FDDElement element, Node parent)
    {
        if(element != null)
        {
            Element n = buildNode(element);
            parent.appendChild(n);
            Enumeration childElements = element.getAllSubFDDElements();
            while(childElements.hasMoreElements())
            {
                FDDElement child = (FDDElement)childElements.nextElement();
                addNodeToDocument(child, n);
            }
        }
    }
    
    /**
     *    Starts construction of the XML document from the FDDModel root Element
     */
    
    public void buildXML(DefaultFDDModel model)
    {
        Element root = document.getDocumentElement();
        addNodeToDocument(model.getRootFDDElement(), root);
    }
}
