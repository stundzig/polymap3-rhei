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

import java.util.ArrayList;
import java.util.List;

import java.io.Reader;
import java.net.URL;

import org.osgi.framework.BundleContext;

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
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavadocContentAccess;

import org.polymap.rhei.ide.java.JvmInstall;
import org.polymap.rhei.ide.java.LibraryLocation;
import org.polymap.rhei.script.RheiScriptPlugin;

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
	private static RheiIdePlugin plugin;
	

    public RheiIdePlugin() {
    }

    
    public void start( BundleContext context ) throws Exception {
		super.start( context );
		plugin = this;
		
		createScriptsProject();
	}

	
	public void stop( BundleContext context ) throws Exception {
		plugin = null;
		super.stop(context);
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


    protected void createScriptsProject() {
        try {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IProject project = root.getProject( "Scripts" );
    
            // delete also ./.metadata/.plugins/org.eclipse.core.resources/.projects/Scripts/
            if (!project.exists()) {
                //http://sdqweb.ipd.kit.edu/wiki/JDT_Tutorial:_Creating_Eclipse_Java_Projects_Programmatically
                log.info( "Creating Scripts project..." );
                project.create( null );
                project.open( null );
    
                // create project
                IProjectDescription description = project.getDescription();
                description.setNatureIds( new String[] { JavaCore.NATURE_ID });
                project.setDescription( description, null );
                
                // java project
                IJavaProject javaProject = JavaCore.create( project ); 
                
                // src folder
                IFolder sourceFolder = project.getFolder( "src" );
                sourceFolder.create( false, true, null );
                
                // output folder
                IFolder binFolder = project.getFolder( "build" );
                binFolder.create( false, true, null );
                javaProject.setOutputLocation( binFolder.getFullPath(), null );
    
                // classpath
                List<IClasspathEntry> entries = new ArrayList();
                JvmInstall jvm = new JvmInstall( "/home/falko/bin/jdk1.7.0_04" );
                for (LibraryLocation elm : jvm.libraryLocations()) {
                    entries.add( JavaCore.newLibraryEntry( elm.getLibraryPath(), elm.getSourcePath(), null ) );
                }
                // add VM libs to classpath
                javaProject.setRawClasspath(
                        entries.toArray( new IClasspathEntry[entries.size()] ), null );
                
                // add src to classpath
                IPackageFragmentRoot srcRoot = javaProject.getPackageFragmentRoot( sourceFolder );
                IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
                IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
                System.arraycopy( oldEntries, 0, newEntries, 0, oldEntries.length );
                newEntries[oldEntries.length] = JavaCore.newSourceEntry( srcRoot.getPath() );
                javaProject.setRawClasspath( newEntries, null );
                
                //IClasspathContainer con = JavaCore.new
                
                // copy test class
                URL res = RheiScriptPlugin.getDefault().getBundle().getResource( "resources/TestFormPage.java" );
                String code = IOUtils.toString( res.openStream(), "UTF-8" );
                IPackageFragment pkg = srcRoot.createPackageFragment( "forms", false, null );
                ICompilationUnit obj = pkg.createCompilationUnit( "forms/TestFormPage.java", code, false, null );
            }
            else {
                project.open( null );
                IJavaProject javaProject = JavaCore.create( project ); 

                project.build( IncrementalProjectBuilder.FULL_BUILD, null );
                
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
                IMember member = cu.findPrimaryType(); //.getMethod( "finalize", new String[0] );
                Reader reader = JavadocContentAccess.getHTMLContentReader( member, true, true );
                log.info( "    JavaDoc: " + IOUtils.toString( reader ) );
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
