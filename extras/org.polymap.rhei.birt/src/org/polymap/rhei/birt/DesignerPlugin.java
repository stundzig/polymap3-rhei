/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.birt;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author <a href="mailto:falko@polymap.de">Falko Braeutigam</a>
 */
public class DesignerPlugin extends AbstractUIPlugin {

	public static final String     PLUGIN_ID = "org.polymap.rhei.birt";

	private static DesignerPlugin  plugin;
	
	public DesignerPlugin() {
        System.out.println( "LOADING: " + getClass() );
	}

	public void start( BundleContext context ) throws Exception {
		super.start( context );
		plugin = this;
		
		System.out.print( "Starting: " + getClass() );
	}

	public void stop( BundleContext context ) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static DesignerPlugin getDefault() {
		return plugin;
	}

}
