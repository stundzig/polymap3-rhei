/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated
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
 *
 * $Id: $
 */
package org.polymap.rhei.data.entityfeature;

import java.util.HashSet;
import java.util.Set;

import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.identity.Identifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.dialogs.MessageDialog;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.model.Entity;
import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.filter.IFilterEditorSite;
import org.polymap.rhei.filter.TransientFilter;

/**
 * Provides common methods for a filter that works on {@link Entity} (instead
 * of Features).
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.1
 */
public abstract class AbstractEntityFilter
        extends TransientFilter {

    private static Log log = LogFactory.getLog( AbstractEntityFilter.class );

    protected static final FilterFactory    ff = DataPlugin.ff;

    protected Class<? extends Entity>       entityClass;


    public AbstractEntityFilter( String id, ILayer layer, String label, Set<String> keywords,
            int maxResults, Class<? extends Entity> entityClass ) {
        super( id, layer, label, keywords, null, maxResults );
        this.entityClass = entityClass;
    }


    /**
     * This default implementation returns <code>false</code>.
     */
    public boolean hasControl() {
        return false;
    }


    public Composite createControl( Composite parent, IFilterEditorSite site ) {
        throw new IllegalStateException( "This Filter does not provide a control." );
    }


    /**
     * Creates the entity query of this filter.
     *
     * @return Newly created entity query.
     */
    protected abstract Query<? extends Entity> createQuery( IFilterEditorSite site );


    public Filter createFilter( IFilterEditorSite site ) {
        Query<? extends Entity> entities = createQuery( site );

        Id result = buildFidFilter( entities, getMaxResults() );
        if (result.getIdentifiers().size() >= getMaxResults()) {
            MessageDialog.openInformation( PolymapWorkbench.getShellToParentOn(), "Zu große Ergebnisliste",
                    "Die Suchanfrage ergab " + entities.count() + " Treffer.\n" +
                    "Die Maximalanzahl für die Suche beträgt " + getMaxResults() + ".\n" +
                    "Schränken Sie die Suche weiter ein, um alle Treffer anzuzeigen." );  //\n\n" +
                    //"Wollen Sie die ersten " + getMaxResults() + " Ergebnisse anzeigen?" )) {
            return null;
        }
        else {
            return result;
        }
    }


    /**
     * Builds a conjunction of the given expressions.
     *
     * @param exp1
     * @param exp2
     * @return
     * @throws IllegalArgumentException If both expressions are null.
     */
    protected BooleanExpression and( BooleanExpression exp1, BooleanExpression exp2 ) {
        if (exp1 == null && exp2 == null) {
            throw new IllegalArgumentException( "Both expressions are null." );
        }
        else if (exp1 == null) {
            return exp2;
        }
        else if (exp2 == null) {
            return exp1;
        }
        else {
            return QueryExpressions.and( exp1, exp2 );
        }
    }


    protected Id buildFidFilter( Query<? extends Entity> entities, int maxResults ) {
        Set<Identifier> fids = new HashSet<Identifier>();
        int count = 0;
        for (Entity entity : entities) {
            if (count++ >= maxResults) {
                break;
            }
            fids.add( ff.featureId( entity.id() ) );
        }
        return ff.id( fids );
    }

}
