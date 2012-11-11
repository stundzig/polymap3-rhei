/* 
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
package org.polymap.rhei.navigator.filter;

import org.opengis.filter.Filter;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.ui.featureselection.FeatureSelectionView;
import org.polymap.core.geohub.LayerFeatureSelectionOperation;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.Messages;
import org.polymap.rhei.RheiFormPlugin;
import org.polymap.rhei.filter.IFilter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class OpenFilterAction
        extends Action
        implements IAction {

    protected IFilter             filter;
    
    
    OpenFilterAction( IFilter filter ) {
        super();
        this.filter = filter;
        setText( Messages.get( "OpenFilterAction_name" ) );
        setToolTipText( Messages.get( "OpenFilterAction_tip" ) );
        setImageDescriptor( ImageDescriptor.createFromURL( 
                RheiFormPlugin.getDefault().getBundle().getResource( "icons/etool16/search.gif" ) ) );
    }

    
    public void run() {
        assert !filter.hasControl();
        try {
            Filter filterFilter = filter.createFilter( null );
            if (filterFilter != null) {
                showResults( filter.getLayer(), filterFilter );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Fehler beim Suchen und Öffnen der Ergebnistabelle.", e );
        }
    }

    
    public static void showResults( ILayer layer, Filter filter ) 
    throws Exception {
        if (filter == null) {
            return;
        }
        // XXX find an indirect way to signal that the layer has selected
        // features; GeoHub? 
        FeatureSelectionView.open( layer );
        
        // change feature selection
        LayerFeatureSelectionOperation op = new LayerFeatureSelectionOperation();
        op.init( layer, filter, null, null );
        OperationSupport.instance().execute( op, true, false );
            
//        // emulate a selection event so that the view can handle it
//        GeoEvent event = new GeoEvent( GeoEvent.Type.FEATURE_SELECTED, 
//                layer.getMap().getLabel(), 
//                layer.getGeoResource().getIdentifier().toURI() );
//        event.setFilter( filter );
//        GeoHub.instance().send( event );
    }
    
}
