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
package org.polymap.rhei.form;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.eclipse.jface.action.Action;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultFormEditorPage
        implements IFormEditorPage {

    protected String                id;
    
    protected String                title;
    
    protected Feature               feature;

    protected FeatureStore          fs;

    
    public DefaultFormEditorPage( String id, String title, Feature feature, FeatureStore fs ) {
        this.id = id;
        this.title = title;
        this.feature = feature;
        this.fs = fs;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Action[] getEditorActions() {
        return null;
    }
    
}
