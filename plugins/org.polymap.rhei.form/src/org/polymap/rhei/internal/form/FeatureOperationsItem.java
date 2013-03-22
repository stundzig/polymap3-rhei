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
package org.polymap.rhei.internal.form;

import java.util.Collections;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.filter.FilterFactory2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.operation.DefaultOperationContext;
import org.polymap.core.data.operation.FeatureOperationFactory;
import org.polymap.core.data.operation.FeatureOperationFactory.IContextProvider;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.LazyInit;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureOperationsItem
        extends ContributionItem
        implements IContextProvider {

    private static Log log = LogFactory.getLog( FeatureOperationsItem.class );
    
    public static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
   
    private ILayer              layer;
    
    private Feature             feature;

    private FeatureStore        fs;
    
    
    public FeatureOperationsItem( ILayer layer, Feature feature, FeatureStore fs ) {
        assert feature != null & layer != null && fs != null;
        this.layer = layer;
        this.feature = feature;
        this.fs = fs;
    }

    
    public void fill( final ToolBar parent, int index ) {
        final ToolItem item = new ToolItem( parent, SWT.DROP_DOWN, index );
        Image icon = DataPlugin.getDefault().imageForName( "icons/etool16/feature_ops.gif" );
        item.setImage( icon );
        item.setToolTipText( Messages.get( "FeatureOperationsItem_title" ) );
        
        item.addSelectionListener( new SelectionListener() {

            @Override
            public void widgetSelected( SelectionEvent ev ) {
                widgetDefaultSelected( ev );
            }

            @Override
            public void widgetDefaultSelected( final SelectionEvent e ) {
                if (e.detail != SWT.ARROW) {
                    MessageDialog.openInformation( PolymapWorkbench.getShellToParentOn(),
                            Messages.get( "FeatureOperationsItem_dialogTitle" ), 
                            Messages.get( "FeatureOperationsItem_dialogMsg" ) );
                }
                else {
                    Menu menu = new Menu( parent );
                    menu.setLocation( parent.toDisplay( e.x, e.y ) );

                    FeatureOperationFactory factory = FeatureOperationFactory.forContext( FeatureOperationsItem.this );
                    
                    for (final Action action : factory.actions()) {
                        MenuItem menuItem = new MenuItem( menu, SWT.PUSH );
                        menuItem.setText( action.getText() );
                        
                        // icon
                        ImageDescriptor descriptor = action.getImageDescriptor();
                        if (descriptor != null) {
                            Image icon2 = DataPlugin.getDefault().imageForDescriptor( 
                                    descriptor, action.getText() + "_icon" );
                            menuItem.setImage( icon2 );
                        }

                        menuItem.addSelectionListener( new SelectionListener() {
                            public void widgetSelected( SelectionEvent se ) {
                                widgetDefaultSelected( se );
                            }
                            public void widgetDefaultSelected( SelectionEvent se ) {
                                action.run();
                            }
                        });
                    }
                    menu.setVisible( true );
                } 
            }
        });
    }


    public DefaultOperationContext newContext() {
        return new DefaultOperationContext() {

            private LazyInit<FeatureCollection> fc = new CachedLazyInit( 1024 );

            @Override
            public Object getAdapter( Class adapter ) {
                if (adapter.equals( ILayer.class )) {
                    return layer;
                }
                return super.getAdapter( adapter );
            }

            @Override
            public FeatureCollection features() throws Exception {
                return fc.get( new Supplier<FeatureCollection>() {
                    public FeatureCollection get() {
                        try {
                            return featureSource().getFeatures( ff.id( Collections.singleton( feature.getIdentifier() ) ) );
                        }
                        catch (Exception e) {
                            throw new RuntimeException( e );
                        }
                    }
                });
            }
            
            @Override
            public FeatureSource featureSource() throws Exception {
                return fs;
            }
        };
    }    

}
