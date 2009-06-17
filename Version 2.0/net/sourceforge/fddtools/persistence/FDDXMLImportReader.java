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

import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.AspectInfo;
import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.Milestone;
import com.nebulon.xml.fddi.MilestoneInfo;
import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.StatusEnum;
import com.nebulon.xml.fddi.Subject;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class FDDXMLImportReader
{

    private ObjectFactory of = new ObjectFactory();
    private Project fddiProject = of.createProject();
    private Document document = null;

    public FDDXMLImportReader(String fileName)
            throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(fileName);
        buildProject();
    }

    private void buildProject()
    {
        NodeList project = document.getElementsByTagName("Project");
        Element projectElement = (Element) project.item(0);

        fddiProject.setName(getName(projectElement));

        Aspect aspect = of.createAspect();
        aspect.setName("Development");
        aspect.setStandardMilestones();
        fddiProject.getAspect().add(aspect);
        aspect.setParent(fddiProject);


        NodeList mfs = projectElement.getElementsByTagName("MajorFeatureSet");
        for(int i1 = 0; i1 < mfs.getLength(); i1++)
        {
            Subject subject = of.createSubject();
            Element majorFeatureSet = (Element) mfs.item(i1);
            subject.setName(getName(majorFeatureSet));

            aspect.getSubject().add(subject);
            subject.setParent(aspect);

            NodeList featureSets = majorFeatureSet.getElementsByTagName("FeatureSet");
            for(int i2 = 0; i2 < featureSets.getLength(); i2++)
            {
                Activity activity = of.createActivity();
                Element featureSet = (Element) featureSets.item(i2);
                activity.setName(getName(featureSet));
                activity.setInitials(getInitials(featureSet));
                subject.getActivity().add(activity);
                activity.setParent(subject);

                NodeList features = featureSet.getElementsByTagName("Feature");
                for(int i3 = 0; i3 < features.getLength(); i3++)
                {
                    Feature fddiFeature = of.createFeature();
                    Element feature = (Element) features.item(i3);
                    fddiFeature.setName(getName(feature));
                    fddiFeature.setInitials(getInitials(feature));
                    Milestone domainWalkthrough = of.createMilestone();
                    Milestone design = of.createMilestone();
                    Milestone designInspection = of.createMilestone();
                    Milestone code = of.createMilestone();
                    Milestone codeInspection = of.createMilestone();
                    Milestone promoteToBuild = of.createMilestone();

                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(getTargetDate(feature));
                    XMLGregorianCalendar xmlDate = null;
                    try
                    {
                        xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
                    } catch(DatatypeConfigurationException ex)
                    {
                        Logger.getLogger(FDDXMLImportReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    domainWalkthrough.setPlanned(xmlDate);
                    design.setPlanned(xmlDate);
                    designInspection.setPlanned(xmlDate);
                    code.setPlanned(xmlDate);
                    codeInspection.setPlanned(xmlDate);
                    promoteToBuild.setPlanned(xmlDate);

                    domainWalkthrough.setStatus((getProgress(feature) >= 1) ? StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
                    design.setStatus((getProgress(feature) >= 41) ? StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
                    designInspection.setStatus((getProgress(feature) >= 44) ? StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
                    code.setStatus((getProgress(feature) >= 89) ? StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
                    codeInspection.setStatus((getProgress(feature) >= 99) ? StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
                    promoteToBuild.setStatus((getProgress(feature) == 100) ? StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
                    fddiFeature.getMilestone().add(domainWalkthrough);
                    fddiFeature.getMilestone().add(design);
                    fddiFeature.getMilestone().add(designInspection);
                    fddiFeature.getMilestone().add(code);
                    fddiFeature.getMilestone().add(codeInspection);
                    fddiFeature.getMilestone().add(promoteToBuild);
                    activity.getFeature().add(fddiFeature);
                    fddiFeature.setParent(activity);
                }
            }
        }
    }

    public Project getRoot()
    {
        return fddiProject;
    }

    private String getName(Element fddNode)
    {
        String name = null;

        NodeList nameNode = fddNode.getElementsByTagName("Name");
        Element nameElement = (Element) nameNode.item(0);
        if(nameElement.hasChildNodes())
        {
            name = nameElement.getFirstChild().getNodeValue();
        }
        return name;
    }

    private String getInitials(Element fddNode)
    {
        String owner = null;
        NodeList ownerNode = fddNode.getElementsByTagName("Owner");
        Element ownerElement = (Element) ownerNode.item(0);
        if(ownerElement.hasChildNodes())
        {
            owner = ownerElement.getFirstChild().getNodeValue();
        }
        return owner;
    }

    private int getProgress(Element fddNode)
    {
        int progress = 0;

        NodeList progressNode = fddNode.getElementsByTagName("Progress");
        Element progressElement = (Element) progressNode.item(0);
        if(progressElement.hasChildNodes())
        {
            progress = Integer.parseInt(progressElement.getFirstChild().getNodeValue());
        }
        return progress;
    }

    private Date getTargetDate(Element fddNode)
    {
        Date targetMonth = null;
        NodeList targetMonthNode = fddNode.getElementsByTagName("TargetMonth");
        Element targetMonthElement = (Element) targetMonthNode.item(0);
        if(targetMonthElement.hasChildNodes())
        {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy");
            try
            {
                targetMonth = formatter.parse(targetMonthElement.getFirstChild().getNodeValue());
            } catch(ParseException ex)
            {
                Logger.getLogger(FDDXMLImportReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return targetMonth;
    }
}
