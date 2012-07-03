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
package org.polymap.rhei.ide.navigator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.ide.RheiIdePlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DeleteFileAction
        implements IObjectActionDelegate {

    private static Log log = LogFactory.getLog( DeleteFileAction.class );

    private List<IFile>         files = new ArrayList();

    
    public void run( IAction action ) {
        for (IFile file : files) {
            try {
                file.delete( false, null );
            }
            catch (CoreException e) {
                PolymapWorkbench.handleError( RheiIdePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
            }
        }
    }


    public void selectionChanged( IAction action, ISelection sel ) {
        //action.setEnabled(  );
        files.clear();
        if (sel instanceof IStructuredSelection) {
            for (Object elm : ((IStructuredSelection)sel).toList()) {
                files.add( (IFile)elm );
            }
        }
    }


    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }
    
}
