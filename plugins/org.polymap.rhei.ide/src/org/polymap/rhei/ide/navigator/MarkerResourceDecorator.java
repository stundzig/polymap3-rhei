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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.eclipse.ui.PlatformUI;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;

import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.ide.RheiIdePlugin;

/**
 * Decorates {@link IResource} elements with icon for max marker severity. 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MarkerResourceDecorator
        extends BaseLabelProvider
        implements ILightweightLabelDecorator, IResourceChangeListener {

    private static Log log = LogFactory.getLog( MarkerResourceDecorator.class );

    private static final ImageDescriptor    error = RheiIdePlugin.imageDescriptorFromPlugin( 
            RheiIdePlugin.PLUGIN_ID, "icons/ovr16/error_co.gif" );

    private static final ImageDescriptor    warn = RheiIdePlugin.imageDescriptorFromPlugin( 
            RheiIdePlugin.PLUGIN_ID, "icons/ovr16/warning_co.gif" );


    private Set<IResource>  decorated = new HashSet();
    
    private Display         display = Polymap.getSessionDisplay();

    
    public MarkerResourceDecorator() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener( this,
                IResourceChangeEvent.POST_CHANGE | 
                IResourceChangeEvent.POST_BUILD | 
                IResourceChangeEvent.PRE_DELETE );
    }


    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );
    }

    
    public void decorate( Object elm, IDecoration decoration ) {
        if (elm instanceof IResource) {
            try {
                int severity = ((IResource)elm).findMaxProblemSeverity( null, true, IResource.DEPTH_INFINITE );
                if (severity == IMarker.SEVERITY_ERROR) {
                    decoration.addOverlay( error, IDecoration.BOTTOM_LEFT );
                }
                else if (severity == IMarker.SEVERITY_WARNING) {
                    decoration.addOverlay( warn, IDecoration.BOTTOM_LEFT );
                }
                
                decorated.add( (IResource)elm );
            }
            catch (Exception e) {
                log.warn( "", e );
            }
        }
    }

    
    public void resourceChanged( IResourceChangeEvent ev ) {
        for (IMarkerDelta delta : ev.findMarkerDeltas( null, true )) {
            if (decorated.contains( delta.getResource() )) {
                display.asyncExec( new Runnable() {
                    public void run() {
                        if (!PlatformUI.getWorkbench().isClosing()) {
                            fireLabelProviderChanged( new LabelProviderChangedEvent( MarkerResourceDecorator.this ) );
                        }
                    }
                });
                break;
            }
        }
        //
        if (ev.getType() == IResourceChangeEvent.PRE_DELETE) {
            decorated.remove( ev.getResource() );
        }
    }

}
