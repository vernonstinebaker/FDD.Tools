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

package net.sourceforge.fddtools.model;

import java.io.IOException;
import java.util.NoSuchElementException;

import javax.swing.tree.MutableTreeNode;

/**
*  Interface for parser to tokenize the input stream and construct a
*  MutableTreeNode.
*  The class implements this interface is expected to return specific
*  subclass of MutableTreeNode, e,g., Project, MajorFeatureSet, FeatureSet and
*  Feature.
*
* @author Kenneth Jiang
*/
public abstract class TreeNodeTokenizer
{
	private MutableTreeNode currentNode = null;

	public boolean hasMoreNodes() throws IOException
	{
		// We have current Node ready
		if( null != currentNode )
			return true;

		// If not, try to find whether the next one exist
		if( null == (currentNode = findNextNode() ) )
			return false;
		else
			return true;
	}

	public MutableTreeNode nextNode()
			throws NoSuchElementException, IOException
	{
		MutableTreeNode forSave = null;
		//If we have current Node ready, return it and set it to be null again
		if( null != currentNode )
		{
			forSave = currentNode;
			currentNode = null;
			return forSave;
		}
		// Else, if we can find a new node, return it
		else if( null != (forSave = findNextNode()) )
			return forSave;
		// Else, throw NoSuchElementException
		else
			throw new NoSuchElementException( "No more node can be found" );
	}

	abstract protected MutableTreeNode findNextNode() throws IOException;
}
