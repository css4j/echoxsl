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
package org.apache.xalan.templates;

import org.w3c.dom.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.StringTokenizer;

import org.apache.xml.utils.QName;
import org.apache.xml.utils.NameSpace;
import org.apache.xpath.XPathContext;
import org.apache.xml.utils.StringToStringTable;
import org.apache.xml.utils.NameSpace;
import org.apache.xml.utils.StringVector;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xalan.transformer.ResultTreeHandler;

import javax.xml.transform.TransformerException;

import java.io.*;

import java.util.*;

/**
 * <meta name="usage" content="advanced"/>
 * Implement a Literal Result Element.
 * @see <a href="http://www.w3.org/TR/xslt#literal-result-element">literal-result-element in XSLT Specification</a>
 */
public class ElemLiteralResult extends ElemUse
{

  /**
   * Tells if this element represents a root element
   * that is also the stylesheet element.
   */
  private boolean isLiteralResultAsStylesheet = false;

  /**
   * NEEDSDOC Method setIsLiteralResultAsStylesheet 
   *
   *
   * NEEDSDOC @param b
   */
  public void setIsLiteralResultAsStylesheet(boolean b)
  {
    isLiteralResultAsStylesheet = b;
  }

  /**
   * NEEDSDOC Method getIsLiteralResultAsStylesheet 
   *
   *
   * NEEDSDOC (getIsLiteralResultAsStylesheet) @return
   */
  public boolean getIsLiteralResultAsStylesheet()
  {
    return isLiteralResultAsStylesheet;
  }

  /**
   * The created element node will have the attribute nodes
   * that were present on the element node in the stylesheet tree,
   * other than attributes with names in the XSLT namespace.
   */
  private Vector m_avts = null;

  /** List of attributes with the XSLT namespace.   */
  private Vector m_xslAttr = null;

  /**
   * Set a literal result attribute (AVTs only).
   *
   * NEEDSDOC @param avt
   */
  public void addLiteralResultAttribute(AVT avt)
  {

    if (null == m_avts)
      m_avts = new Vector();

    m_avts.addElement(avt);
  }

  /**
   * Set a literal result attribute (used for xsl attributes).
   *
   * NEEDSDOC @param att
   */
  public void addLiteralResultAttribute(String att)
  {

    if (null == m_xslAttr)
      m_xslAttr = new Vector();

    m_xslAttr.addElement(att);
  }

  /**
   * Get a literal result attribute by name.
   *
   * NEEDSDOC @param name
   *
   * NEEDSDOC ($objectName$) @return
   */
  public AVT getLiteralResultAttribute(String name)
  {

    if (null != m_avts)
    {
      int nAttrs = m_avts.size();

      for (int i = (nAttrs - 1); i >= 0; i--)
      {
        AVT avt = (AVT) m_avts.elementAt(i);

        if (avt.getRawName().equals(name))
        {
          return avt;
        }
      }  // end for
    }

    return null;
  }
  
  /**
   * Get whether or not the passed URL is contained flagged by
   * the "extension-element-prefixes" property.
   * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
   *
   * @param prefix non-null reference to prefix that might be excluded.
   *
   * @return true if the prefix should normally be excluded.
   */
  public boolean containsExcludeResultPrefix(String prefix)
  {

    if (null == m_excludeResultPrefixes)
      return super.containsExcludeResultPrefix(prefix);

    if (prefix.length() == 0)
      prefix = Constants.ATTRVAL_DEFAULT_PREFIX;

    if(m_excludeResultPrefixes.contains(prefix))
      return true;
    else
      return super.containsExcludeResultPrefix(prefix);
  }
  
  /**
   * Augment resolvePrefixTables, resolving the namespace aliases once 
   * the superclass has resolved the tables.
   */
  public void resolvePrefixTables() throws TransformerException
  {
    super.resolvePrefixTables();
        
    StylesheetRoot stylesheet = getStylesheetRoot();
    if((null != m_namespace) && (m_namespace.length() > 0))
    {
      NamespaceAlias nsa = stylesheet.getNamespaceAliasComposed(m_namespace);
      if(null != nsa)
      {
        m_namespace = nsa.getResultNamespace();
        
        String resultPrefix = nsa.getResultPrefix();
        if((null != resultPrefix) && (resultPrefix.length() > 0))
          m_rawName = resultPrefix+":"+m_localName;
        else
          m_rawName = m_localName;
      }
    }
    
    if(null != m_avts)
    {
      int n = m_avts.size();
      for(int i = 0; i < n; i++)
      {
        AVT avt = (AVT)m_avts.elementAt(i);
        
        // Should this stuff be a method on AVT?
        String ns = avt.getURI();
        if((null != ns) && (ns.length() > 0))
        {
          NamespaceAlias nsa = stylesheet.getNamespaceAliasComposed(m_namespace);
          if(null != nsa)
          {
            String namespace = nsa.getResultNamespace();
            
            String resultPrefix = nsa.getResultPrefix();
            String rawName = avt.getName();
            if((null != resultPrefix) && (resultPrefix.length() > 0))
              rawName = resultPrefix+":"+rawName;
            avt.setURI(namespace);
            avt.setRawName(rawName);
          }
        }
      }
    }
  }

  /**
   * The namespace of the element to be created.
   */
  private String m_namespace;

  /**
   * Set the namespace URI of the result element to be created.
   * Note that after resolvePrefixTables has been called, this will 
   * return the aliased result namespace, not the original stylesheet 
   * namespace.
   *
   * @param ns The Namespace URI, or the empty string if the
   *        element has no Namespace URI.
   */
  public void setNamespace(String ns)
  {
    m_namespace = ns;
  }

  /**
   * Get the original namespace of the Literal Result Element.
   *
   * @return The Namespace URI, or the empty string if the
   *        element has no Namespace URI.
   */
  public String getNamespace()
  {
    return m_namespace;
  }
  
  /**
   * The raw name of the element to be created.
   */
  private String m_localName;

  /**
   * Set the local name of the LRE.
   *
   * @param localName The local name (without prefix) of the result element 
   *                  to be created.
   */
  public void setLocalName(String localName)
  {
    m_localName = localName;
  }

  /**
   * Get the local name of the Literal Result Element.
   * Note that after resolvePrefixTables has been called, this will 
   * return the aliased name prefix, not the original stylesheet 
   * namespace prefix.
   *
   * @return The local name (without prefix) of the result element 
   *                  to be created.
   */
  public String getLocalName()
  {
    return m_localName;
  }

  /**
   * The raw name of the element to be created.
   */
  private String m_rawName;
  
  /**
   * Set the raw name of the LRE.
   *
   * @param rawName The qualified name (with prefix), or the
   *        empty string if qualified names are not available.
   */
  public void setRawName(String rawName)
  {
    m_rawName = rawName;
  }

  /**
   * Get the raw name of the Literal Result Element.
   *
   * @return  The qualified name (with prefix), or the
   *        empty string if qualified names are not available.
   */
  public String getRawName()
  {
    return m_rawName;
  }

  /**
   * The "extension-element-prefixes" property, actually contains URIs.
   */
  private StringVector m_ExtensionElementURIs;

  /**
   * Set the "extension-element-prefixes" property.
   * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
   *
   * NEEDSDOC @param v
   */
  public void setExtensionElementPrefixes(StringVector v)
  {
    m_ExtensionElementURIs = v;
  }

  /**
   * Get and "extension-element-prefix" property.
   * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
   *
   * NEEDSDOC @param i
   *
   * NEEDSDOC ($objectName$) @return
   *
   * @throws ArrayIndexOutOfBoundsException
   */
  public String getExtensionElementPrefix(int i)
          throws ArrayIndexOutOfBoundsException
  {

    if (null == m_ExtensionElementURIs)
      throw new ArrayIndexOutOfBoundsException();

    return m_ExtensionElementURIs.elementAt(i);
  }

  /**
   * Get the number of "extension-element-prefixes" Strings.
   * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
   *
   * NEEDSDOC ($objectName$) @return
   */
  public int getExtensionElementPrefixCount()
  {
    return (null != m_ExtensionElementURIs)
           ? m_ExtensionElementURIs.size() : 0;
  }

  /**
   * Get and "extension-element-prefix" property.
   * @see <a href="http://www.w3.org/TR/xslt#extension-element">extension-element in XSLT Specification</a>
   *
   * NEEDSDOC @param uri
   *
   * NEEDSDOC ($objectName$) @return
   */
  public boolean containsExtensionElementURI(String uri)
  {

    if (null == m_ExtensionElementURIs)
      return false;

    return m_ExtensionElementURIs.contains(uri);
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * NEEDSDOC ($objectName$) @return
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_LITERALRESULT;
  }

  /**
   * Return the node name.
   *
   * NEEDSDOC ($objectName$) @return
   */
  public String getNodeName()
  {

    // TODO: Need prefix.
    return m_rawName;
  }

  /**
   * The XSLT version as specified by this element.
   */
  private String m_version;

  /**
   * Set the "version" property.
   * @see <a href="http://www.w3.org/TR/xslt#forwards">forwards in XSLT Specification</a>
   *
   * NEEDSDOC @param v
   */
  public void setVersion(String v)
  {
    m_version = v;
  }

  /**
   * The "exclude-result-prefixes" property.
   */
  private StringVector m_excludeResultPrefixes;

  /**
   * Set the "exclude-result-prefixes" property.
   * The designation of a namespace as an excluded namespace is
   * effective within the subtree of the stylesheet rooted at
   * the element bearing the exclude-result-prefixes or
   * xsl:exclude-result-prefixes attribute; a subtree rooted
   * at an xsl:stylesheet element does not include any stylesheets
   * imported or included by children of that xsl:stylesheet element.
   * @see <a href="http://www.w3.org/TR/xslt#literal-result-element">literal-result-element in XSLT Specification</a>
   *
   * @param v vector of prefixes that are resolvable to strings.
   */
  public void setExcludeResultPrefixes(StringVector v)
  {
    m_excludeResultPrefixes = v;
  }

  /**
   * Copy a Literal Result Element into the Result tree, copy the
   * non-excluded namespace attributes, copy the attributes not
   * of the XSLT namespace, and execute the children of the LRE.
   * @see <a href="http://www.w3.org/TR/xslt#literal-result-element">literal-result-element in XSLT Specification</a>
   *
   * @param transformer non-null reference to the the current transform-time state.
   * @param sourceNode non-null reference to the <a href="http://www.w3.org/TR/xslt#dt-current-node">current source node</a>.
   * @param mode reference, which may be null, to the <a href="http://www.w3.org/TR/xslt#modes">current mode</a>.
   *
   * @throws TransformerException
   */
  public void execute(
          TransformerImpl transformer, Node sourceNode, QName mode)
            throws TransformerException
  {

    try
    {
      ResultTreeHandler rhandler = transformer.getResultTreeHandler();

      // Add namespace declarations.
      executeNSDecls(transformer);
      rhandler.startElement(getNamespace(), getLocalName(), getRawName());

      // Process any possible attributes from xsl:use-attribute-sets first
      super.execute(transformer, sourceNode, mode);

      //xsl:version, excludeResultPrefixes???
      // Process the list of avts next
      if (null != m_avts)
      {
        int nAttrs = m_avts.size();

        for (int i = (nAttrs - 1); i >= 0; i--)
        {
          AVT avt = (AVT) m_avts.elementAt(i);
          XPathContext xctxt = transformer.getXPathContext();
          String stringedValue = avt.evaluate(xctxt, sourceNode, this);

          if (null != stringedValue)
          {

            // Important Note: I'm not going to check for excluded namespace 
            // prefixes here.  It seems like it's to expensive, and I'm not 
            // even sure this is right.  But I could be wrong, so this needs 
            // to be tested against other implementations.
            rhandler.addAttribute(avt.getURI(), avt.getName(),
                                  avt.getRawName(), "CDATA", stringedValue);
          }
        }  // end for
      }

      // Now process all the elements in this subtree
      // TODO: Process m_extensionElementPrefixes && m_attributeSetsNames
      transformer.executeChildTemplates(this, sourceNode, mode);
      rhandler.endElement(getNamespace(), getLocalName(), getRawName());
      unexecuteNSDecls(transformer);
    }
    catch(org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
  }

  /**
   * Compiling templates requires that we be able to list the AVTs
   * ADDED 9/5/2000 to support compilation experiment
   *
   * NEEDSDOC ($objectName$) @return
   */
  public Enumeration enumerateLiteralResultAttributes()
  {
    return (null == m_avts) ? null : m_avts.elements();
  }
}
