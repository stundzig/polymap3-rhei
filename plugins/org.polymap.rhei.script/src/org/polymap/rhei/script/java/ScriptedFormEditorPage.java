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
package org.polymap.rhei.script.java;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;

import org.polymap.rhei.form.DefaultFormEditorPage;

/**
 * Provides the base class for BeanShell scripted forms.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ScriptedFormEditorPage
        extends DefaultFormEditorPage
        implements WantsToBeShown {

    public ScriptedFormEditorPage( String id, String title ) {
        super( id, title, (Feature)ScriptParams.get( "feature" ), (FeatureStore)ScriptParams.get( "fs" ) );
    }


}
