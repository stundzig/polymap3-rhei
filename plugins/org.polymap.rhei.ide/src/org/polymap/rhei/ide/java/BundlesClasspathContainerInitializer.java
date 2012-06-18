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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BundlesClasspathContainerInitializer
        extends ClasspathContainerInitializer {

    private static Log log = LogFactory.getLog( BundlesClasspathContainerInitializer.class );

    public static final String      ID = "org.polymap.rhei.ide.BUNDLES_CONTAINER";

    
    public BundlesClasspathContainerInitializer() {
    }


    public void initialize( IPath path, IJavaProject project )
    throws CoreException {
        if (path.segmentCount() > 0) {
            if (path.segment(0).equals( ID )) {
                BundlesClasspathContainer container = new BundlesClasspathContainer();
                JavaCore.setClasspathContainer( path, new IJavaProject[] {project}, 
                        new IClasspathContainer[] {container}, null);
            }
        }
    }
    
}
