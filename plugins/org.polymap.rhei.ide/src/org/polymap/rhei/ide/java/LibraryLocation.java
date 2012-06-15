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

import org.eclipse.core.runtime.IPath;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LibraryLocation {

    private IPath       libraryPath;
    
    private IPath       sourcePath;
    
    private IPath       javadocPath;

    
    protected LibraryLocation( IPath libraryPath, IPath sourcePath, IPath javadocPath ) {
        this.libraryPath = libraryPath;
        this.sourcePath = sourcePath;
        this.javadocPath = javadocPath;
    }

    public IPath getLibraryPath() {
        return libraryPath;
    }

    public IPath getSourcePath() {
        return sourcePath;
    }
    
    public IPath getJavadocPath() {
        return javadocPath;
    }
    
}
