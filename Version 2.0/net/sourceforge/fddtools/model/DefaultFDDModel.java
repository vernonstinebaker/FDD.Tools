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
* DefaultFDDModel is the default implementation of FDDModel, inheriting from
* DefautlTreeModel to utilize its facilitiy methods to handle TreeNode.
*
* @author Kenneth Jiang
*/

package net.sourceforge.fddtools.model;

import java.util.Date;
import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;


public class DefaultFDDModel extends DefaultTreeModel
				implements FDDModel
{

	/**
	*  Constructors
	*/

	public DefaultFDDModel( FDDElement root )
	{
		super( (TreeNode) root );
	}

	public boolean dirty = false;
	
	/**
	*  methods of FDDModel
	*/
	public FDDElement getRootFDDElement()
	{
		return (FDDElement) getRoot();
	}

	public Enumeration getChildFDDElements( FDDElement parent )
	{
		return parent.getAllSubFDDElements();
	}

	public String getFDDElementName( FDDElement element )
	{
		return element.getName();
	}

	public int getFDDElementProgress( FDDElement element )
	{
		return element.getProgress();
	}

	public Date getFDDElementTargetMonth( FDDElement element )
	{
		return element.getTargetMonth();
	}

	public String getFDDElementOwner( FDDElement element )
	{
		return element.getOwner();
	}

	public int getFDDElementChildCount( FDDElement element )
	{
		return element.getSubFDDElementCount();
	}


	public void setRootFDDElement( FDDElement root )
	{
		// Call DefaultTreeModel.setRoot and reload the whole tree to indicate change
		setRoot( (TreeNode) root );
		reload( (TreeNode) root );
	}

	public void addChildFDDElement( FDDElement child, FDDElement parent )
			throws IllegalArgumentException
	{
		// Call DefaultTreeModel.insertNodeInto to avoid call nodeInserted explicitly
		insertNodeInto( (MutableTreeNode) child, (MutableTreeNode) parent, getChildCount( parent ) );

		//Call recalcProgress and recalcTargetMonth to adjust the progress of all affected nodes
		recalcProgress( parent );
		recalcTargetMonth( parent );
	}


	public void setFDDElementName( FDDElement element, String name )
	{
		element.setName( name );

		// Call DefaultTreeModel.nodeChanged to indicate change
		nodeChanged( (TreeNode) element );
	}

	public void setFDDElementProgress( FDDElement element, int progress )
	{
		element.setProgress( progress );

		// Call DefaultTreeModel.nodeChanged to indicate change
		nodeChanged( (TreeNode) element );

		//Call recalcProgress to adjust the progress of all affected nodes
		recalcProgress( element );
	}

	public void setFDDElementTargetMonth( FDDElement element, Date targetMonth )
	{
		element.setTargetMonth( targetMonth );

		// Call DefaultTreeModel.nodeChanged to indicate change
		nodeChanged( (TreeNode) element );

		//Call recalcTarget to ajust the targetMonth of all affected
	}

	public void setFDDElementOwner( FDDElement element, String owner )
	{
		element.setOwner( owner );

		// Call DefaultTreeModel.nodeChanged to indicate change
		nodeChanged( (TreeNode) element );
	}


	/**
	 *  methods for recalculate the progress or tagetMonth of affected
	 *  nodes when one node is changed
	 */

	 protected void recalcProgress( FDDElement element )
	 {
		TreeNode[] pathToRoot = getPathToRoot( (TreeNode) element );
		for( int i = (pathToRoot.length - 2); i >= 0; i -- )
		{
			FDDElement parent = (FDDElement) pathToRoot[i];
			int totalProgress = 0;
			for( Enumeration children = parent.getAllSubFDDElements(); children.hasMoreElements(); )
				totalProgress += ( (FDDElement) children.nextElement() ).getProgress() ;
			parent.setProgress( (int) totalProgress / (parent.getSubFDDElementCount()) );
			nodeChanged( (TreeNode) parent );
		}
	}

	protected void recalcTargetMonth( FDDElement element )
	{
		TreeNode[] pathToRoot = getPathToRoot( (TreeNode) element );
		for( int i = (pathToRoot.length - 2); i >= 0; i -- )
		{
			FDDElement parent = (FDDElement) pathToRoot[i];
			Enumeration children = parent.getAllSubFDDElements();

			//Because parent has at least one child (the element itself), it's safe to do the following
			Date lastestTargetMonth = ( (FDDElement) children.nextElement() ).getTargetMonth();
			for( ; children.hasMoreElements(); )
			{
				FDDElement currentElement = (FDDElement) children.nextElement();
				if( lastestTargetMonth.before( currentElement.getTargetMonth() ) )
					lastestTargetMonth = currentElement.getTargetMonth();
			}
			parent.setTargetMonth( lastestTargetMonth );
			nodeChanged( (TreeNode) parent );
		}
	}

}
