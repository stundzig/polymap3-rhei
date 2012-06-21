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
package org.polymap.rhei.ide;

import java.io.Reader;
import java.net.URL;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.polymap.rhei.ide.java.JavaProjectInitializer;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class RheiIdePlugin 
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( RheiIdePlugin.class );

    // The plug-in ID
	public static final String PLUGIN_ID = "org.polymap.rhei.ide";

	// The shared instance
	private static RheiIdePlugin   plugin;

    private ServiceTracker         httpServiceTracker;
	

    public RheiIdePlugin() {
    }

    
    public void start( BundleContext context ) throws Exception {
		super.start( context );
		plugin = this;
		
		createScriptsProject();
		
        httpServiceTracker = new ServiceTracker( context, HttpService.class.getName(), null ) {
            public Object addingService( ServiceReference reference ) {
                try {
                    HttpService httpService = (HttpService)super.addingService( reference );
                    httpService.registerResources( "/rhei-ide-icons", "/icons", null );
                    return httpService;
                }
                catch (Exception e) {
                    throw new RuntimeException( e );
                }
            }
        };
        httpServiceTracker.open();
	}

	
	public void stop( BundleContext context ) throws Exception {
		plugin = null;
		httpServiceTracker.close();
		super.stop( context );
	}

	
	public static RheiIdePlugin getDefault() {
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


	public IProject getScriptsProject() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject( "Scripts" );
        return project;	    
	}
	
	
    protected void createScriptsProject() {
        try {
            IProject project = getScriptsProject();
            // delete also ./.metadata/.plugins/org.eclipse.core.resources/.projects/Scripts/
            if (!project.exists()) {
                JavaProjectInitializer.initScriptsProject( project );
            }
            else {
                //project.open( null );
                IJavaProject javaProject = JavaCore.create( project ); 

                //project.build( IncrementalProjectBuilder.FULL_BUILD, null );
                
                IFolder srcFolder = project.getFolder( "src" );
                IPackageFragmentRoot srcRoot = javaProject.getPackageFragmentRoot( srcFolder );

                IPackageFragment pkg = srcRoot.getPackageFragment( "forms" );
                ICompilationUnit cu = pkg.getCompilationUnit( "TestFormPage.java" );
                log.info( "CompilationUnit: "  + cu );
                cu.open( null );
                log.info( "   primaryType: " + cu.findPrimaryType() );
                log.info( "   markers:" );
                for (IMarker marker : findJavaProblemMarkers( cu )) {
                    log.info( "        line " + marker.getAttribute( IMarker.LINE_NUMBER )
                            + ": " + marker.getAttribute( IMarker.MESSAGE ) );
                }
                
                // javadoc test
                IMember member = cu.findPrimaryType().getMethod( "finalize", new String[0] );
                Reader reader = JavadocContentAccess.getHTMLContentReader( member, true, true );
                log.info( "    JavaDoc: " + (reader != null ? IOUtils.toString( reader ) : "nothing found") );
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    public IMarker[] findJavaProblemMarkers( ICompilationUnit cu ) 
    throws CoreException {
        IResource javaSourceFile = cu.getUnderlyingResource();
        IMarker[] markers = javaSourceFile.findMarkers(
                IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
                true, IResource.DEPTH_INFINITE);
        return markers;
    }
    
}
