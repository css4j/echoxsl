/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: XMLFileFilter.java 470101 2006-11-01 21:03:00Z minchau $
 */
package org.apache.qetest;

/**
 * FilenameFilter supporting includes/excludes returning files
 * that match *.xml.
 *
 * @author shane_curcuru@us.ibm.com
 * @version $Id: XMLFileFilter.java 470101 2006-11-01 21:03:00Z minchau $
 */
public class XMLFileFilter extends FilePatternFilter
{

    /** Default pattern we're looking for: *.xml.  */
    public static final String PATTERN = "*.xml";

    /** 
     * Initialize for default pattern.  
     */
    public XMLFileFilter() 
    {
        super(null, null, PATTERN);
    }

    /**
     * Initialize with some include(s)/exclude(s).  
     *
     * @param inc semicolon-delimited string of inclusion name(s)
     * @param exc semicolon-delimited string of exclusion name(s)
     */
    public XMLFileFilter(String inc, String exc)
    {
        super(inc, exc, PATTERN);
    }
}
