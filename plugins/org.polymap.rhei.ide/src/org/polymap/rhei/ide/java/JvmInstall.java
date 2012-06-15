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

import java.util.Collections;
import java.util.List;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.Path;

/**
 * Provides information about a JVM installation. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class JvmInstall {

    private static Log log = LogFactory.getLog( JvmInstall.class );
    
    private File            dir;
    
    
    public JvmInstall( String dir ) {
        this.dir = new File( dir );
    }


    public List<LibraryLocation> libraryLocations() {
        LibraryLocation rt = new LibraryLocation( 
                Path.fromOSString( dir.getAbsolutePath() + "/jre/lib/rt.jar" ),
                Path.fromOSString( dir.getAbsolutePath() + "/src.zip" ),
                null );
        return Collections.singletonList( rt );
    }
    
}
