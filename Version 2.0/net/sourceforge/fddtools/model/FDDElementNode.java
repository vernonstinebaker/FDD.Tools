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
* Inherit from FDDElement. FDDElementNode construct FDDElement in
* a tree structure by implementing interface MutableTreeNode.
* FDDElementNode acts as base class for Project, MajorFeatureSet,
* FeatureSet, Feature.
*
* @author Kenneth Jiang
*/

package net.sourceforge.fddtools.model;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;


abstract class FDDElementNode extends FDDElement
				implements MutableTreeNode
{

	private Vector subFDDElements = new Vector();
	private Object userObject = null;


	/**
	* Constructors
	*/

	public FDDElementNode( String name, int progress, Date targetMonth, String owner, FDDElement parent )
			throws IllegalArgumentException
	{
		super( name, progress, targetMonth, owner, parent );
	}

	public FDDElementNode( String name, int progress, Date targetMonth, String owner )
	{
		super( name, progress, targetMonth, owner );
	}

	public FDDElementNode()
	{
		super();
	}


	/**
	*   Implementation of the abstract methods in FDDElement
	*/

	public int getSubFDDElementCount()
	{
		return subFDDElements.size();
	}

	public void insertFDDElement( FDDElement child, int index )
			throws IllegalArgumentException
	{
		child.setParentFDDElement( this );
		subFDDElements.insertElementAt( child, index );
	}

	public void insertFDDElement( FDDElement child )
			throws IllegalArgumentException
	{
		child.setParentFDDElement( this );
		subFDDElements.addElement( child );
	}

	public void removeFDDElement( FDDElement child )
	{
		child.setParentFDDElement( null );
		subFDDElements.removeElement( child );
	}

	public void removeFDDElement( int index )
	{
		((FDDElement) subFDDElements.elementAt( index )).setParentFDDElement( null );
		subFDDElements.removeElementAt( index );
	}

	public FDDElement getFDDElementAt( int index )
	{
		return (FDDElement) subFDDElements.elementAt( index );
	}

	public Enumeration getAllSubFDDElements()
	{
		return subFDDElements.elements();
	}


	/**
	* Mutable Tree Nodes methods
	*/

	public void  insert( MutableTreeNode child, int index )
	{
		insertFDDElement((FDDElement)child,index);
	}

	public void remove( int index )
	{
		removeFDDElement( index );
	}

	public int getChildCount()
	{
		return getSubFDDElementCount();
	}

	public TreeNode getChildAt(int index)
	{
		return (TreeNode) getFDDElementAt( index );
	}

	public void remove( MutableTreeNode node )
	{
		removeFDDElement( (FDDElement) node );
	}

	public void  removeFromParent()
	{
		setParent(null);
	}

	public boolean isLeaf()
	{
		return 0 == subFDDElements.size() ;
	}

	public TreeNode getParent()
	{
		return (TreeNode) getParentFDDElement();
	}

	public void   setParent( MutableTreeNode newParent )
	{
		setParentFDDElement( (FDDElement) newParent);
	}

	public int getIndex(TreeNode node)
	{
		return subFDDElements.indexOf( node );
	}

	public Enumeration children()
	{
		return getAllSubFDDElements();
	}

	public void setUserObject(Object object)
	{
		this.userObject = object;
	}

	public String toString()
	{
		return getName();
	}
}