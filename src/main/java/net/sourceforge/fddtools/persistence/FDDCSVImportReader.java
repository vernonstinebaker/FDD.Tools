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

import com.opencsv.CSVReader;


import com.opencsv.exceptions.CsvValidationException;
import com.nebulon.xml.fddi.Activity;
import com.nebulon.xml.fddi.Aspect;
import com.nebulon.xml.fddi.Feature;
import com.nebulon.xml.fddi.Milestone;
import com.nebulon.xml.fddi.ObjectFactory;
import com.nebulon.xml.fddi.Project;
import com.nebulon.xml.fddi.StatusEnum;
import com.nebulon.xml.fddi.Subject;


import java.io.FileReader;
import java.io.IOException;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.GregorianCalendar;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.sourceforge.fddtools.internationalization.Messages;

/**
 * Implement the interface TreeNodeTokenizer to convert lines in MS Project
 * .csv file into objects of Project, MajorFeatureSet FeatureSet or Feature
 */
public class FDDCSVImportReader
{
    private static final String ROOT_NAME = "Develop";

    private static Project project = null;
    private static int rootLevel = 0;
    private static boolean rootFound = false;

    private FDDCSVImportReader()
    {
        //Insure class cannot be instantiated except through static method
    }

    public static Project read(String fileName) throws IOException, CsvValidationException
    {
        try (var reader = new CSVReader(new FileReader(fileName)))
        {
            return buildProject(reader);
        }
    }

    private static Project buildProject(CSVReader reader) throws IOException, CsvValidationException
    {
        ObjectFactory of = new ObjectFactory();
        Aspect aspect = null;
        Subject subject = null;
        Activity activity = null;
        Feature feature = null;

        int validLines = 0;
        String[] nextLine;
        while((nextLine = reader.readNext()) != null)
        {
                if(nextLine[0].isEmpty() || !Character.isDigit(nextLine[0].charAt(0)))
                {
                    continue;
                }

                if(nextLine.length < 3)
                {
                    throw new IOException();
                }
                else
                {
                    validLines++;
                }

                int outlineLevel = Integer.parseInt(nextLine[0]);
                String elementName = nextLine[1];

                // Following code is added to adpat new CVS format
                if(!rootFound)
                {
                    if(elementName.equalsIgnoreCase(ROOT_NAME)) // root is found
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
                    if(rootLevel >= outlineLevel)
                    {
                        break;
                    }

                    if(outlineLevel >= (rootLevel + 4))
                    {
                        continue;
                    }
                }

                String progressInPercent = nextLine[2];
                int progress = Integer.parseInt(progressInPercent.substring(0,
                        progressInPercent.length() - 1));

                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
                ParsePosition pos = new ParsePosition(0);
                Date targetMonth = formatter.parse(nextLine[3].substring(4), pos);

                String owner = null;

                if(nextLine.length > 4) //Owner elementName is optional
                {
                    owner = nextLine[4];
                }

                switch(outlineLevel - rootLevel)
                {
                    case 0:
                        project = of.createProject();
                        project.setName(Messages.getInstance().getMessage(Messages.PROJECT_DEFAULT_NAME));
                        aspect = of.createAspect();
                        aspect.setName(elementName);
                        aspect.setStandardMilestones();
                        project.getAspect().add(aspect);
                        aspect.setParentNode(project);
                        break;

                    case 1:
                        subject = of.createSubject();
                        subject.setName(elementName);
                        subject.setPrefix("<Edit Prefix>");
                        aspect.getSubject().add(subject);
                        subject.setParentNode(aspect);
                        break;

                    case 2:
                        activity = of.createActivity();
                        activity.setName(elementName);
                        if(owner != null && !owner.isEmpty())
                        {
                            String[] s = owner.split("[^\\w]");
                            activity.setInitials(s[0]);
                        }
                        subject.getActivity().add(activity);
                        activity.setParentNode(subject);
                        break;
                        
                    case 3:
                        feature = of.createFeature();
                        feature.setName(elementName);
                        feature.setInitials(owner);

                        Milestone domainWalkthrough = of.createMilestone();
                        Milestone design = of.createMilestone();
                        Milestone designInspection = of.createMilestone();
                        Milestone code = of.createMilestone();
                        Milestone codeInspection = of.createMilestone();
                        Milestone promoteToBuild = of.createMilestone();

                        GregorianCalendar cal = new GregorianCalendar();
                        cal.setTime(targetMonth);
                        XMLGregorianCalendar xmlDate = null;
                        try
                        {
                            xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
                        }
                        catch(DatatypeConfigurationException ex)
                        {
                            LoggerFactory.getLogger(FDDXMLImportReader.class).error("Error creating XML date", ex);
                        }
                        feature.setTargetDate(targetMonth);
                        domainWalkthrough.setPlanned(xmlDate);
                        design.setPlanned(xmlDate);
                        designInspection.setPlanned(xmlDate);
                        code.setPlanned(xmlDate);
                        codeInspection.setPlanned(xmlDate);
                        promoteToBuild.setPlanned(xmlDate);

                        domainWalkthrough.setStatus((progress >= 1) ? StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
                        design.setStatus((progress >= 41) ? StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
                        designInspection.setStatus((progress >= 44) ? StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
                        code.setStatus((progress >= 89) ? StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
                        codeInspection.setStatus((progress >= 99) ? StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
                        promoteToBuild.setStatus((progress == 100) ? StatusEnum.COMPLETE : StatusEnum.NOTSTARTED);
                        feature.getMilestone().add(domainWalkthrough);
                        feature.getMilestone().add(design);
                        feature.getMilestone().add(designInspection);
                        feature.getMilestone().add(code);
                        feature.getMilestone().add(codeInspection);
                        feature.getMilestone().add(promoteToBuild);

                        activity.getFeature().add(feature);
                        feature.setParentNode(activity);
                        break;
                }

                if(validLines == 0)
                {
                    throw new IOException();
                }
            }

        return project;
    }
}
