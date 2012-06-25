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
package org.polymap.rhei.ide;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.internal.views.markers.MarkerSupportInternalUtilities;

import org.eclipse.core.resources.IMarker;

import org.polymap.core.runtime.Polymap;

/**
 * Listens to {@link SelectionChangedEvent} containing {@link IMarker}
 * and updates the given status line with the contant of the marker.   
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public class MarkerSelectionStatusLineAdapter
        implements ISelectionChangedListener {
    
    private static Log log = LogFactory.getLog( MarkerSelectionStatusLineAdapter.class );

    private IStatusLineManager      manager;
    

    public MarkerSelectionStatusLineAdapter( IStatusLineManager manager ) {
        assert manager != null;
        this.manager = manager;
    }


    public void selectionChanged( SelectionChangedEvent ev ) {
        String msg = null;
        Image image = null;
        if (ev.getSelection() instanceof IStructuredSelection) {
            List<IMarker> markers = new ArrayList();
            // find markers
            for (Object elm : ((IStructuredSelection)ev.getSelection()).toList()) {
                if (elm instanceof IMarker) {
                    markers.add( (IMarker)elm );
                }
            }
            //
            if (markers.size() > 1) {
                msg = "Multiple markers: " + markers.size();
            }
            else if (markers.size() == 1) {
                msg = markers.get( 0 ).getAttribute( IMarker.MESSAGE, "Marker has no message." );
                image = MarkerSupportInternalUtilities.getSeverityImage(
                        markers.get( 0 ).getAttribute( IMarker.SEVERITY, IMarker.SEVERITY_INFO ) );
            }
        }
        final String displayMsg = msg;
        final Image displayImage = image;
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                log.debug( "Status msg: " + displayMsg );
                manager.setMessage( displayImage, displayMsg );
            }
        });
    }
    
}
