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

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

import org.polymap.core.runtime.Timer;

import org.polymap.rhei.ide.RheiIdePlugin;
import org.polymap.rhei.ide.java.PluginClasspathDecoder.EntryHandler;

/**
 * Provides classpath entries for all installed plugins.
 * <p/>
 * <b>Several preconditions to make this working:</b>
 * <p/>
 * The algorithm to resolve the bundles and libs inside the bundles does not use the
 * OSGI MANIFEST.MF but the .classpath from JDT inside the bundles. So, if the bundle
 * just provides classes (without source) then nothing special needs to be done.
 * <p/>
 * If the bundle provides a <b>src folder</b> for the classes of the folder then the
 * the src folder and the <b>.classpath</b> file needs to be included in the bundle.
 * <p/>
 * If the bundle provides nested jars then they are resolved without any special
 * action. However, if the libs have <b>source attachments</b> then the .classpath
 * needs a proper <code>sourceAttachment</code> entry for this lib and the source jar
 * need to be included in the bundle and the bundle needs to be marked for
 * <b>unpacking after install</b> in the feature.xml that deploys this bundle.
 * <p/>
 * <b>Note:</b> Sometimes Eclipse 3.5 does <b>not</b> properly include the source jars in
 * the bundle if any include/exclude rules are given (see
 * <code>org.polymap.core.libs</code>).
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BundlesClasspathContainer
        implements IClasspathContainer {

    private static Log log = LogFactory.getLog( BundlesClasspathContainer.class );

    private File                homeDir;

    private IClasspathEntry[]   entries;
    
    
    protected BundlesClasspathContainer() {
//        for (Object key : System.getProperties().keySet()) {
//            log.info( key + " : " + System.getProperty( (String)key ) );
//        }
        String home = System.getProperty( "eclipse.home.location" );
        //String home = "/home/falko/servers/polymap3";
        assert home != null;
        if (home.startsWith( "file:" )) {
            homeDir = new File( URI.create( home ) );
        }
        else {
            homeDir = new File( home );
        }
        
        // bundle state changed -> force classpath update
        RheiIdePlugin.getDefault().getBundle().getBundleContext().addBundleListener( new BundleListener() {
            public void bundleChanged( BundleEvent ev ) {
                entries = null;
            }
        });
    }
    
    public String getDescription() {
        return "Bundles";
    }

    public int getKind() {
        return K_SYSTEM;
    }

    public IPath getPath() {
        return new Path( BundlesClasspathContainerInitializer.ID );
    }

    
    public IClasspathEntry[] getClasspathEntries() {
        if (entries == null) {
            try {
                entries = computePluginEntries2();
            }
            catch (Exception e) {
                log.warn( "", e );
            }
        }
        return entries; 
    }

    
    protected IPath bundleFile( Bundle bundle, String path ) { 
        try {
            URL res = FileLocator.find( bundle, Path.fromPortableString( path ), null );
            URL fileRes = FileLocator.toFileURL( res );
            String filePath = StringUtils.substringAfter( fileRes.toExternalForm(), "file:" );
            return Path.fromOSString( filePath );
        }
        catch (Exception e) {
//            throw new RuntimeException( e );
            return null;
        }
    }
    
    
    private IClasspathEntry[] computePluginEntries2() 
    throws Exception {
        Timer timer = new Timer();
        final List<IClasspathEntry> result = new ArrayList( 256 );
        
        Bundle[] bundles = RheiIdePlugin.getDefault().getBundle().getBundleContext().getBundles();
        for (final Bundle bundle : bundles) {
            // plugin file/dir
            File bundleFile = FileLocator.getBundleFile( bundle );
            IPath bundlePath = Path.fromOSString( bundleFile.getAbsolutePath() );
            log.debug( "   bundle path: " + bundlePath );                                
            
            // .classpath
            IPath cpFile = bundleFile( bundle, ".classpath" );
            final List<IPath> bundleSrcPaths = new ArrayList();
            final List<IPath> bundleOutputPaths = new ArrayList();

            if (cpFile != null && cpFile.toFile().exists()) {
                
                new PluginClasspathDecoder( cpFile.toFile() ).process( new EntryHandler() {
                    public void handle( String kind, String path, String srcPath, String javadoc ) {
                        log.debug( "        classpath entry: (" + kind + ") " + path );
                        IPath filePath = bundleFile( bundle, path );
                        log.debug( "            -> file: " + filePath );
                        
                        if ("src".equals( kind )) {
                            bundleSrcPaths.add( filePath );
                        }
                        else if ("output".equals( kind )) {
                            if (filePath != null && filePath.toFile().exists()) {
                                bundleOutputPaths.add( filePath );
                            }
                        }
                        else if ("lib".equals( kind )) {
                            if (filePath != null && filePath.toFile().exists()) {
                                IPath fileSrcPath = srcPath.length() > 0 ? bundleFile( bundle, srcPath ) : null;
                                result.add( JavaCore.newLibraryEntry( filePath, fileSrcPath, null ) );
                            }
                        }
                    }
                });
            }
            
            // deployed jar?
            if (bundlePath.toString().endsWith( "jar" )
                    || bundleOutputPaths.isEmpty()) {
                result.add( JavaCore.newLibraryEntry( bundlePath, 
                        !bundleSrcPaths.isEmpty() ? bundleSrcPaths.get( 0 ) : null, null ) );
            }
            // inside IDE
            else {
                for (IPath outputPath : bundleOutputPaths) {
                    // XXX handle multiple src entries
                    result.add( JavaCore.newLibraryEntry( outputPath, 
                            !bundleSrcPaths.isEmpty() ? bundleSrcPaths.get( 0 ) : null, null ) );
                }
            }
        }
        for (IClasspathEntry entry : result) {
            log.info( entry.getPath() + " -- " + entry.getSourceAttachmentPath() );
        }
        log.info( "Classpath computed: " + result.size() + " entries. (" + timer.elapsedTime() + "ms)" );
        return result.toArray( new IClasspathEntry[ result.size() ] );
    }
    
    
//    private IClasspathEntry[] computePluginEntries() 
//    throws Exception {
//        Timer timer = new Timer();
//        final List<IClasspathEntry> result = new ArrayList();
//        
//        Bundle[] bundles = RheiIdePlugin.getDefault().getBundle().getBundleContext().getBundles();
//        for (Bundle bundle : bundles) {
//            //http://lmap.blogspot.de/2008/03/platform-scheme-uri.html
//            IPath pluginPath = Path.fromPortableString( "platform:/plugin/" + bundle.getSymbolicName() );
//            log.debug( "Path: " + pluginPath );
//            String loc = bundle.getLocation();
//            log.debug( "Location: " + loc );
//            
//            if (loc.contains( "/" )) {
//                // in development workspace
//                if (!loc.endsWith( "jar/" )) {
//                    String home = System.getProperty( "user.home" );
//
//                    final IPath filePath = Path.fromOSString( home )
//                        .append( Path.fromOSString( StringUtils.substringAfter( loc, "file:" ) ).makeAbsolute() );
//
//                    URL cpres = URI.create( pluginPath.append( ".classpath" ).toPortableString() ).toURL();
//                    new PluginClasspathDecoder( cpres ).process( new EntryHandler() {
//
//                        public void handle( String kind, String path, String srcPath, String javadoc ) {
//                            log.debug( "   entry: " + path );
//                            if (kind.equals( "output" )) {
//                                if (filePath.append( path ).toFile().exists()
//                                        // XXX fb: hack; I just don't see why this bundle's output is not found :(
//                                        && !filePath.toString().contains( "org.eclipse.text" )) {
//                                    result.add( JavaCore.newLibraryEntry( 
//                                            filePath.append( path ), filePath.append( srcPath ), null ) );
//                                }
//                            }
//                            else if (kind.equals( "lib" )) {
//                                result.add( JavaCore.newLibraryEntry( filePath.append( path ), 
//                                        srcPath.length() > 0 ? filePath.append( srcPath ) : null, null ) );                                
//                            }
//                        }
//                    });
//                }
//                // plugin jar
//                else {
//                    String home = System.getProperty( "eclipse.home.location" );
//                    
//                    String rawPluginPath = StringUtils.substringAfter( loc, "file:" );
//                    rawPluginPath = StringUtils.substringBeforeLast( rawPluginPath, "/" );
//                    
//                    final IPath filePath = Path.fromOSString( StringUtils.substringAfter( home, "file:" ) )
//                           .append( Path.fromOSString( rawPluginPath ).makeAbsolute() );
//                    log.debug( "   plugin path: " + filePath );                                
//                    result.add( JavaCore.newLibraryEntry( filePath, null, null ) );
//                    
//                    URL cpres = URI.create( pluginPath.append( ".classpath" ).toPortableString() ).toURL();
//                    cpres = FileLocator.find( bundle, Path.fromPortableString( "META-INF/MANIFEST.MF" ), null );
//                    log.info( ".classpath: " + FileLocator.toFileURL( cpres ) );
////                    new PluginClasspathDecoder( cpres ).process( new EntryHandler() {
////                        public void handle( String kind, String path, String srcPath, String javadoc ) {
////                            log.info( "   entry: " + path );
////                        }
////                    });
//                }
//            }
//        }
//        log.info( "Classpath computed: " + result.size() + " entries. (" + timer.elapsedTime() + "ms)" );
//        return result.toArray( new IClasspathEntry[ result.size() ] );
//    }
    
}
