/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xalan.transformer;

import java.util.Vector;

import org.apache.xpath.axes.LocPathIterator;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.QName;
import org.apache.xalan.templates.KeyDeclaration;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.DescendantOrSelfWalker;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.XPath;

import org.w3c.dom.Node;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import javax.xml.transform.TransformerException;

/**
 * <meta name="usage" content="internal"/>
 * NEEDSDOC Class KeyWalker <needs-comment/>
 */
public class KeyWalker extends DescendantOrSelfWalker
{

  /**
   * Construct a KeyWalker using a LocPathIterator.
   *
   * NEEDSDOC @param locPathIterator
   */
  public KeyWalker(LocPathIterator locPathIterator)
  {
    super(locPathIterator);
  }

  /**
   *  Set the root node of the TreeWalker.
   *
   * NEEDSDOC @param root
   */
  public void setRoot(Node root)
  {

    m_attrs = null;
    m_foundAttrs = false;
    m_attrPos = 0;

    super.setRoot(root);
  }

  /** NEEDSDOC Field m_attrs          */
  NamedNodeMap m_attrs;

  /** NEEDSDOC Field m_foundAttrs          */
  boolean m_foundAttrs;

  /** NEEDSDOC Field m_attrPos          */
  int m_attrPos;

  /** NEEDSDOC Field m_lookupKey          */
  String m_lookupKey;

  /**
   * Get the next node in document order on the axes.
   *
   * NEEDSDOC ($objectName$) @return
   */
  protected Node getNextNode()
  {

    if (!m_foundAttrs)
    {
      m_attrs = getCurrentNode().getAttributes();
      m_foundAttrs = true;
    }

    if (null != m_attrs)
    {
      if (m_attrPos < m_attrs.getLength())
      {
        return m_attrs.item(m_attrPos++);
      }
      else
      {
        m_attrs = null;
      }
    }

    Node next = super.getNextNode();

    if (null != next)
      m_foundAttrs = false;

    return next;
  }

  /**
   *  Test whether a specified node is visible in the logical view of a
   * TreeWalker or NodeIterator. This function will be called by the
   * implementation of TreeWalker and NodeIterator; it is not intended to
   * be called directly from user code.
   * @param n  The node to check to see if it passes the filter or not.
   *
   * NEEDSDOC @param testNode
   * @return  a constant to determine whether the node is accepted,
   *   rejected, or skipped, as defined  above .
   */
  public short acceptNode(Node testNode)
  {

    KeyIterator ki = (KeyIterator) m_lpi;
    Vector keys = ki.getKeyDeclarations();

    QName name = ki.getName();
    try
    {
      String lookupKey = m_lookupKey;

      // System.out.println("lookupKey: "+lookupKey);
      int nDeclarations = keys.size();

      // Walk through each of the declarations made with xsl:key
      for (int i = 0; i < nDeclarations; i++)
      {
        KeyDeclaration kd = (KeyDeclaration) keys.elementAt(i);

        if(!kd.getName().equals(name)) 
          continue;
        
        ki.getXPathContext().setNamespaceContext(ki.getPrefixResolver());

        // See if our node matches the given key declaration according to 
        // the match attribute on xsl:key.
        double score = kd.getMatch().getMatchScore(ki.getXPathContext(),
                                                   testNode);

        if (score == kd.getMatch().MATCH_SCORE_NONE)
          continue;        
                
        // Query from the node, according the the select pattern in the
        // use attribute in xsl:key.
        XObject xuse = kd.getUse().execute(ki.getXPathContext(), testNode,
                                           ki.getPrefixResolver());

        if (xuse.getType() != xuse.CLASS_NODESET)
        {
          String exprResult = xuse.str();
          ((KeyIterator)m_lpi).addRefNode(exprResult, testNode);
          
          if (lookupKey.equals(exprResult))
            return this.FILTER_ACCEPT;
        }
        else
        {
          NodeIterator nl = xuse.nodeset();
          Node useNode;
          short result = -1;
          /*
          We are walking through all the nodes in this nodeset
          rather than stopping when we find the one we're looking
          for because we don't currently save the state of KeyWalker
          such that the next time it gets called it would continue
          to look in this nodeset for any further matches. 
          TODO: Try to save the state of KeyWalker, i.e. keep this node
          iterator saved somewhere and finish walking through its nodes
          the next time KeyWalker is called before we look for any new
          matches. What if the next call is for the same match+use 
          combination??
          */
          while (null != (useNode = nl.nextNode()))
          {
            String exprResult = m_lpi.getDOMHelper().getNodeData(useNode);
            ((KeyIterator)m_lpi).addRefNode(exprResult, testNode); 
            
            if ((null != exprResult) && lookupKey.equals(exprResult))
              result = this.FILTER_ACCEPT;
              //return this.FILTER_ACCEPT;
          }
          if (-1 != result)
            return result;
        }       
        
      }  // end for(int i = 0; i < nDeclarations; i++)
    }
    catch (TransformerException se)
    {

      // TODO: What to do?
    }

    return this.FILTER_REJECT;
  }
  
   /**
   *  Moves the <code>TreeWalker</code> to the next visible node in document
   * order relative to the current node, and returns the new node. If the
   * current node has no next node,  or if the search for nextNode attempts
   * to step upward from the TreeWalker's root node, returns
   * <code>null</code> , and retains the current node.
   * @return  The new node, or <code>null</code> if the current node has no
   *   next node  in the TreeWalker's logical view.
   */
  public Node nextNode()
  {
    Node node = super.nextNode();
    if (node == null)
      ((KeyIterator)m_lpi).setLookForMoreNodes(false);
    return node;
  }
  
}
