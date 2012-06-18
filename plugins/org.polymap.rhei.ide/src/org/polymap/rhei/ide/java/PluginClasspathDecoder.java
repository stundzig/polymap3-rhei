/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.ide.java;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jdt.internal.core.ClasspathEntry;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public class PluginClasspathDecoder {

    private static Log log = LogFactory.getLog( PluginClasspathDecoder.class );
    
    private URL                 res;
    
    
    public PluginClasspathDecoder( URL res ) {
        this.res = res;
    }


    public void process( EntryHandler handler ) throws Exception {
            Document doc = readClasspathDocument();
            if (doc != null) {
                NodeList children = doc.getElementsByTagName( "classpathentry" );
                for (int i=0; i<children.getLength(); i++) {
                    Element node = (Element)children.item( i );
                    assert node.getTagName().equals( "classpathentry" );

                    String kind = node.getAttribute( ClasspathEntry.TAG_KIND );
                    String path = node.getAttribute( ClasspathEntry.TAG_PATH );
                    handler.handle( kind, path, null, null );
                }
            }
    }

    
    private Document readClasspathDocument() throws Exception {
        InputStream in = null;
        try {
            in = res.openStream();
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = parser.parse( new InputSource( in ) );
            //new XMLSerializer( System.out, new OutputFormat() ).asDOMSerializer().serialize( doc );
            return doc;
        } 
        catch (FileNotFoundException e) {
            return null;    
        }
        finally {
            IOUtils.closeQuietly( in );
        }
    }
    

    /**
     * 
     */
    public static interface EntryHandler {
        public void handle( String kind, String path, String srcAttach, String javadoc );
    }
    
}
