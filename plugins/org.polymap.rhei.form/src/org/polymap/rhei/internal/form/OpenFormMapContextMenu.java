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
package org.polymap.rhei.internal.form;

import org.geotools.data.DefaultQuery;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Envelope;

import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.mapeditor.contextmenu.ContextMenuSite;
import org.polymap.core.mapeditor.contextmenu.IContextMenuContribution;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.WaitingAtomicReference;
import org.polymap.rhei.Messages;
import org.polymap.rhei.RheiFormPlugin;
import org.polymap.rhei.form.FormEditor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class OpenFormMapContextMenu
        extends ContributionItem
        implements IContextMenuContribution {

    private static Log log = LogFactory.getLog( OpenFormMapContextMenu.class );
    
    public static final FilterFactory2  ff = CommonFactoryFinder.getFilterFactory2( null );
    
    private ContextMenuSite              site;

    private WaitingAtomicReference<Menu> menuRef = new WaitingAtomicReference();

    private int                          menuIndex;
    
    
    public String getMenuGroup() {
        return GROUP_HIGH;
    }


    public IContextMenuContribution init( ContextMenuSite _site ) {
        this.site = _site;
        setVisible( false );
        
        final ReferencedEnvelope bbox = site.boundingBox();

        for (final ILayer layer : site.getMap().getLayers()) {
            if (layer.isVisible()) {
                setVisible( true );
                UIJob job = new UIJob( "Find features: " + layer.getLabel() ) {
                    protected void runWithException( IProgressMonitor monitor ) throws Exception {
                        FeatureIterator it = null;
                        try {
                            PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, false );
                            if (fs != null) {
                                ReferencedEnvelope transformed = bbox.transform( layer.getCRS(), true );
                                String propname = "";
                                Filter filter = ff.intersects( 
                                        ff.property( propname ),
                                        ff.literal( JTS.toGeometry( (Envelope)transformed ) ) );

                                FeatureCollection features = fs.getFeatures( new DefaultQuery( 
                                        fs.getSchema().getTypeName(), filter, 10, null, null ) );

                                for (it = features.features(); it.hasNext(); ) {
                                    awaitAndFillMenu( layer, fs, it.next() );
                                }
                            }
                        }
                        catch (Exception e) {
                            log.warn( "Filtering covered features failed: ", e );
                        }
                        finally {
                            if (it != null) {it.close();}
                        }
                    }
                };
                job.schedule();
            }
        }
        return this;
    }

    
    public void fill( final Menu parent, final int index ) {
        menuRef.setAndNotify( parent );
        menuIndex = index;
    }

    
    protected void awaitAndFillMenu( final ILayer layer, final PipelineFeatureSource fs, final Feature feature ) {
        final Action action = new Action( createLabel( feature, layer ) ) {
            public void run() {
                FormEditor.open( fs, feature, layer, true );
            }            
        };
        action.setImageDescriptor( RheiFormPlugin.getDefault().imageDescriptor( "icons/etool16/open_form_editor.gif" ) );
        
        // await and actually fill menu
        final Menu menu = menuRef.get();
        menu.getDisplay().asyncExec( new Runnable() {
            public void run() {
                if (!menu.isDisposed()) {
                    new ActionContributionItem( action ).fill( menu, menuIndex );
                }
            }
        });
    }


    protected String createLabel( Feature feature, ILayer layer ) {
        // last resort: fid
        String featureLabel = feature.getIdentifier().getID();
        
        for (Property prop : feature.getProperties()) {
            String propName = prop.getName().getLocalPart();
            
            if (propName.equalsIgnoreCase( "name" )
                    && prop.getValue() != null) {
                featureLabel = prop.getValue().toString();
                break;
            }
            else if ((propName.contains( "name" ) || propName.contains( "Name" ))
                    && prop.getValue() != null) {
                featureLabel = prop.getValue().toString();
                break;
            }
            else if ((propName.equalsIgnoreCase( "number" ) || propName.equalsIgnoreCase( "nummer" ))
                    && prop.getValue() != null) {
                featureLabel = prop.getValue().toString();
            }
        }
        return Messages.get( "OpenFormMapContextMenu_label", 
                StringUtils.abbreviate( featureLabel, 0, 35 ), 
                layer.getLabel() );
    }

}
