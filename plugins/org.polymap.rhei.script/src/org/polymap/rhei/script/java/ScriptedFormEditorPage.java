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
 * Provides the base class for BeanShell/Java scripted forms.
 * <p/>
 * There are the following variables available:
 * <dl>
 * <dt>feature</dt><dd>The {@link Feature} of this form.</dd>
 * <dt>fs</dt><dd>The {@link FeatureStore} of the feature.</dd>
 * <dt>title</dt><dd>The {@link String} title of this form page.</dd>
 * <dt>id</dt><dd>The unique {@link String} id of this form page.</dd>
 * </dl>
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ScriptedFormEditorPage
        extends DefaultFormEditorPage
        implements WantsToBeShown {

    public ScriptedFormEditorPage( String id, String title ) {
        super( id, title, (Feature)ScriptParams.get( "feature" ), (FeatureStore)ScriptParams.get( "fs" ) );
    }

    // re-run editor actions are provided by ScriptedFormPageProvider#PageWrapper
}
