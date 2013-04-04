/*
 * polymap.org
 * Copyright 2010-2013 Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.data.entityfeature;

import java.util.Set;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.qi4j.api.query.grammar.BooleanExpression;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.qi4j.QiModule.EntityCreator;

/**
 * Provides access to a certain entity type. {@link EntityType} interface for
 * use in {@link EntitySourceProcessor}. It provides factory and retrieval
 * methods. It can also be used to provide an 'feature view' for a given entity
 * type.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface EntityProvider<T extends Entity> {

    public Name getEntityName();

    public EntityType getEntityType();

    public CoordinateReferenceSystem getCoordinateReferenceSystem( String propName );

    public String getDefaultGeometry();

    public ReferencedEnvelope getBounds();

    /**
     *
     * @param entityQuery The query to execute. This may also be a
     *        {@link FidsQueryExpression}.
     * @param firstResult
     * @param maxResults
     */
    public Iterable<T> entities( BooleanExpression entityQuery, int firstResult, int maxResults );

    public int entitiesSize( BooleanExpression entityQuery, int firstResult, int maxResults );

    /**
     *
     * @param creator
     * @return The newly created entity
     * @throws Exception If an exception occured while executing the creator.
     */
    public T newEntity( EntityCreator<T> creator )
    throws Exception;

    public T findEntity( String id );

    public Set<FeatureId> removeEntity( BooleanExpression query );

    public void revert();

}
