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
package org.polymap.rhei.ide.project;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;

import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.ide.RheiIdePlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ScriptProjectContentProvider
        implements ITreeContentProvider {

    private static Log log = LogFactory.getLog( ScriptProjectContentProvider.class );


    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
    }


    public void dispose() {
    }


    public Object[] getChildren( Object elm ) {
        try {
            if (elm instanceof IContainer) {
                return ((IContainer)elm).members();
            }
        }
        catch (CoreException e) {
            PolymapWorkbench.handleError( RheiIdePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
        return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }


    public boolean hasChildren( Object elm ) {
        return getChildren(elm) != null;
    }
    
    public Object[] getElements( Object elm ) {
        return getChildren(elm);
    }
    
    
    public Object getParent( Object elm ) {
        if (elm instanceof IContainer) {
            return ((IContainer)elm).getParent();
        }
        return null;
    }
    
}
