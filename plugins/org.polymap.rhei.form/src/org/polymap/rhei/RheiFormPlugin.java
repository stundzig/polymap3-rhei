/* 
 * polymap.org
 * Copyright (C) 2010-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.ImageRegistryHelper;

import org.osgi.framework.BundleContext;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RheiFormPlugin 
        extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.polymap.rhei.form";

	// The shared instance
	private static RheiFormPlugin plugin;
	

	public static RheiFormPlugin getDefault() {
    	return plugin;
    }

	
	// instance *******************************************
	
	private ImageRegistryHelper        images = new ImageRegistryHelper( this );
	
	
    public void start( BundleContext context ) throws Exception {
        super.start( context );
        plugin = this;
    }

    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        super.stop( context );
    }

	public Image imageForDescriptor( ImageDescriptor descriptor, String key ) {
	    return images.image( descriptor, key );
    }

	public Image imageForName( String resName ) {
	    return images.image( resName );
	}

    public ImageDescriptor imageDescriptor( String path ) {
        return images.imageDescriptor( path );
    }

}
