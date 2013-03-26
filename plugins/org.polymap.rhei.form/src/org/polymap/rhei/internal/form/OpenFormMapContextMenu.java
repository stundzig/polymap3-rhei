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

import java.util.List;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.mapeditor.contextmenu.ContextMenuSite;
import org.polymap.core.mapeditor.contextmenu.IContextMenuContribution;
import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;

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
    
    private ContextMenuSite         site;


    public IContextMenuContribution init( ContextMenuSite _site ) {
        this.site = _site;
        setVisible( false );
        for (ILayer layer : site.getMap().getLayers()) {
            if (layer.isVisible()) {
                List<Feature> features = site.coveredFeatures( layer );
                if (features != null && features.size() > 0 && features.size() < 5) {
                    setVisible( true );
                    break;
                }
            }
        }
        return this;
    }


    public String getMenuGroup() {
        return GROUP_HIGH;
    }


    public void fill( final Menu parent, final int index ) {
        for (final ILayer layer : site.getMap().getLayers()) {
            
            if (layer.isVisible()) {
            
                List<Feature> features = site.coveredFeatures( layer );
                if (features != null) {
                    try {
                        final PipelineFeatureSource fs = PipelineFeatureSource.forLayer( layer, true );
                        
                        for (final Feature feature : features) {
                            Action action = new Action( createLabel( feature, layer ) ) {
                                public void run() {
                                    FormEditor.open( fs, feature, layer, true );
                                }            
                            };
                            action.setImageDescriptor( RheiFormPlugin.imageDescriptorFromPlugin(
                                    RheiFormPlugin.PLUGIN_ID, "icons/etool16/open_form_editor.gif" ) );
                            new ActionContributionItem( action ).fill( parent, index );
                        }
                    }
                    catch (Exception e) {
                        PolymapWorkbench.handleError( RheiFormPlugin.PLUGIN_ID, this, "", e );
                    }
                }
            }
        }
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
