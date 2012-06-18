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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class JREClasspathContainer
        implements IClasspathContainer {

    private static Log log = LogFactory.getLog( JREClasspathContainer.class );

    private IPath           path;

    
    protected JREClasspathContainer() {
        String javaHome = System.getProperty( "java.home" );
        assert javaHome != null;
        path = new Path( javaHome /*"/home/falko/bin/jdk1.7.0_04"*/ );
        if (path.lastSegment().equals( "jre" )) {
            path = path.removeLastSegments( 1 );
        }
    }
    
    public String getDescription() {
        return "JRE libraries";
    }

    public int getKind() {
        return K_DEFAULT_SYSTEM;
    }

    public IPath getPath() {
        return new Path( JREClasspathContainerInitializer.ID );
    }

    public IClasspathEntry[] getClasspathEntries() {
        List<IClasspathEntry> entries = new ArrayList();
        JREInstall jre = new JREInstall( path.toOSString() );
        for (LibraryLocation elm : jre.libraryLocations()) {
            entries.add( JavaCore.newLibraryEntry( elm.getLibraryPath(), elm.getSourcePath(), null ) );
        }
        return entries.toArray( new IClasspathEntry[ entries.size() ] );
    }
    
}
