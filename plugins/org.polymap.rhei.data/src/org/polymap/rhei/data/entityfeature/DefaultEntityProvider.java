/*
 * polymap.org
 * Copyright 2011, Falko Bräutiga, and individual contributors as
 * indicated by the @authors tag.
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
 * $Id$
 */
package org.polymap.rhei.data.entityfeature;

import java.util.Iterator;
import java.util.Set;

import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.FeatureId;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.grammar.BooleanExpression;

import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.data.feature.FidSet;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModule.EntityCreator;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public abstract class DefaultEntityProvider<T extends Entity>
        implements EntityProvider<T> {

    private static Log log = LogFactory.getLog( DefaultEntityProvider.class );

    protected QiModule              repo;

    protected EntityType<T>         type;

    protected Name                  name;
    

    public DefaultEntityProvider( QiModule repo, Class<T> entityClass, Name entityName ) {
        this.repo = repo;
        this.type = repo.entityType( entityClass );
        this.name = entityName;
    }


    public Name getEntityName() {
        return name;
    }


    public EntityType getEntityType() {
        return type;
    }


    public Iterable<T> entities( BooleanExpression query, int firstResult, int maxResults ) {
        return repo.findEntities( type.getType(), query, firstResult, maxResults );
    }


    public int entitiesSize( BooleanExpression query, int firstResult, int maxResults ) {
        // XXX cache result for subsequent entities() call?
        Query result = repo.findEntities( type.getType(), query, firstResult, maxResults );
        return (int)result.count();
    }


    public T newEntity( EntityCreator<T> creator )
    throws Exception {
        // FIXME: operation bounds are handled by AntragOperationConcern !?
        return repo.newEntity( type.getType(), null, creator );
    }


    @Override
    public Set<FeatureId> removeEntity( BooleanExpression query ) {
        FidSet result = new FidSet();        
        for (Entity entity : repo.findEntities( type.getType(), query, 0, -1 )) {
            result.add( new FeatureIdImpl( entity.id() ) );
            repo.removeEntity( entity );
        }
        return result;
    }


    public T findEntity( String id ) {
        return repo.findEntity( type.getType(), id );
    }

    
    @Override
    public ReferencedEnvelope getBounds() {
        assert getDefaultGeometry() != null : "no getDefaultGeometry() -> no bounds";
        org.qi4j.api.query.Query<T> result = repo.findEntities( type.getType(), new GetBoundsQuery( getDefaultGeometry() ), 0, 10 );
        Iterator<T> it = result.iterator();
        
        if (!it.hasNext()) {
            return new ReferencedEnvelope( getCoordinateReferenceSystem( null ) );
        }
        try {
            Geometry geom = (Geometry)type.getProperty( getDefaultGeometry() ).getValue( it.next() );
            double minX = geom.getEnvelopeInternal().getMinX();

            geom = (Geometry)type.getProperty( getDefaultGeometry() ).getValue( it.next() );
            double maxX = geom.getEnvelopeInternal().getMaxX();

            geom = (Geometry)type.getProperty( getDefaultGeometry() ).getValue( it.next() );
            double minY = geom.getEnvelopeInternal().getMinY();

            geom = (Geometry)type.getProperty( getDefaultGeometry() ).getValue( it.next() );
            double maxY = geom.getEnvelopeInternal().getMaxY();

            return new ReferencedEnvelope( minX, maxX, minY, maxY, getCoordinateReferenceSystem( null ) );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public void revert() {
        log.warn( "FIXME FIXME FIXME - reverting ALL changes instead of a layer" );
        repo.revertChanges();
    }

}
