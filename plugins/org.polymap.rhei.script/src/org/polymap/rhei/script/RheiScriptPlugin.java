/* 
 * polymap.org
 * Copyright 2010-2012, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.script;

import java.net.URL;

import org.osgi.framework.BundleContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.polymap.core.workbench.PolymapWorkbench;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class RheiScriptPlugin 
        extends AbstractUIPlugin {

    private static Log              log       = LogFactory.getLog( RheiScriptPlugin.class );

    // The plug-in ID
    public static final String      PLUGIN_ID = "org.polymap.rhei.script";

    // The shared instance
    private static RheiScriptPlugin plugin;


    /**
     * Returns the {@link IProject} containing all the scripts. If it does not exist
     * then it is created. The project is stored under "<workspace>/Scripts" If it
     * does not exists.
     * <p/>
     * This instance id shared by all sessions.
     * 
     * @return The Scripts project.
     */
    public static IProject getOrCreateScriptProject() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject( "Scripts" );

        try {
            if (!project.exists()) {
                // delete also ./.metadata/.plugins/org.eclipse.core.resources/.projects/Scripts/
                project.create( null );
            }
            project.open( null );

            IFolder srcFolder = project.getFolder( "src" );
            if (!srcFolder.exists()) {
                srcFolder.create( false, true, null );
            }
            IFolder formsFolder = project.getFolder( "src/forms" );
            if (!formsFolder.exists()) {
                formsFolder.create( false, true, null );
            }

            //                // copy test class
            //                URL res = RheiScriptPlugin.getDefault().getBundle().getResource( "resources/TestFormPage.java" );
            //                IFile f = formsFolder.getFile( "TestFormPage.java" );
            //                f.create( res.openStream(), 0, null );

            IFolder procsFolder = project.getFolder( "src/procs" );
            if (!procsFolder.exists()) {
                procsFolder.create( false, true, null );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiScriptPlugin.PLUGIN_ID, null, e.getLocalizedMessage(), e );
        }
        return project;
    }

    
    // instance *******************************************
    
    public RheiScriptPlugin() {
    }

    public void start( BundleContext context )
    throws Exception {
        super.start( context );
        plugin = this;
    }

    public void stop( BundleContext context )
    throws Exception {
        plugin = null;
        super.stop( context );
    }

    public static RheiScriptPlugin getDefault() {
        return plugin;
    }

	public Image imageForDescriptor( ImageDescriptor imageDescriptor, String key ) {
        ImageRegistry images = getImageRegistry();
        Image image = images.get( key );
        if (image == null || image.isDisposed()) {
            images.put( key, imageDescriptor );
            image = images.get( key );
        }
        return image;
    }

	public Image imageForName( String resName ) {
        ImageRegistry images = getImageRegistry();
        Image image = images.get( resName );
        if (image == null || image.isDisposed()) {
            URL res = getBundle().getResource( resName );
            assert res != null : "Image resource not found: " + resName;
            images.put( resName, ImageDescriptor.createFromURL( res ) );
            image = images.get( resName );
        }
        return image;
	}
	
}
