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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonNavigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.ide.RheiIdePlugin;

/**
 * Displays all content of the scripting environment as a {@link CommonNavigator}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectNavigator
        extends CommonNavigator {

    private static Log log = LogFactory.getLog( ProjectNavigator.class );

    public static final String ID = "org.polymap.rhei.ProjectNavigator";

    
    public ProjectNavigator() {
    }


    public void createPartControl( Composite parent ) {        
        super.createPartControl( parent );
        getSite().setSelectionProvider( getCommonViewer() );

        getCommonViewer().addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent ev ) {
                try {
                    Object elm = ((IStructuredSelection)getCommonViewer().getSelection()).getFirstElement();
                    if (elm instanceof IFile) {
                        IDE.openEditor( getSite().getPage(), (IFile)elm );
                    }
                }
                catch (PartInitException e) {
                    PolymapWorkbench.handleError( RheiIdePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
                }
            }
        });
        
        try {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            for (IProject project : root.getProjects()) {
                if (!project.isOpen()) {
                    project.open( null );
                }
            }
//            IProject project = root.getProject( "scripts" );
//            project.open( null );
            
            getCommonViewer().setInput( root );
            getCommonViewer().expandToLevel( 3 );
            getCommonViewer().refresh();
        }
        catch (UnsatisfiedLinkError e) {
            log.warn( e.getLocalizedMessage() );
        }
        catch (CoreException e) {
            throw new RuntimeException( e );
        }
    }

}
