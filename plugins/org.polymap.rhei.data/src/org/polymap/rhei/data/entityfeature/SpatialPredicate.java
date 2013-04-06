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
package org.polymap.rhei.data.entityfeature;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.Identifier;

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryExpressionException;
import org.qi4j.api.query.grammar.Predicate;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.api.query.grammar.SingleValueExpression;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.data.DataPlugin;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class SpatialPredicate<T extends Geometry>
        implements Predicate {

    public static final FilterFactory2  ff = DataPlugin.ff;
    
    public static final Predicate EXCLUDE = new Predicate() {
        public boolean eval( Object target ) {
            return false;
        }
    };

    public static final Predicate INCLUDE = new Predicate() {
        public boolean eval( Object target ) {
            return false;
        }
    };


    // instance *******************************************
    
    protected PropertyReference<T>      propertyReference;
    
    protected SingleValueExpression     valueExpression;
    
    
    public SpatialPredicate( PropertyReference<T> property, SingleValueExpression value ) {
        assert property != null && value != null;
        this.propertyReference = property;
        this.valueExpression = value;
    }
    
    public PropertyReference<T> getPropertyReference() {
        return propertyReference;
    }
    
    public SingleValueExpression<T> getValueExpression() {
        return valueExpression;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + valueExpression.value().toString() + "]";
    }

    @Override
    public boolean eval( Object target ) {
        Object value = valueExpression.value();

        Property<T> prop = propertyReference.eval( target );
        if (prop == null) {
            return value == null;
        }
        
        T propValue = prop.get();
        if (propValue == null) {
            return value == null;
        }

//        if (! (propValue instanceof Geometry)) {
//            throw new QueryExpressionException( "Wrong property value for spatial predicate: " + propValue );
//        }
        return eval( propValue, value );
    }
    
    
    protected abstract boolean eval( T propertyValue, Object literal );
    

    /**
     * BBOX
     */
    public static class BBOX<T extends Geometry>
            extends SpatialPredicate<T> {

        public BBOX( PropertyReference<T> property, SingleValueExpression value ) {
            super( property, value );
        }

        @Override
        protected boolean eval( T propertyValue, Object literal ) {
            if (literal instanceof Envelope) {
                return propertyValue.getEnvelopeInternal().intersects( (Envelope)literal );
            }
            else {
                throw new QueryExpressionException( "Value for BBOX query should be Envelope: " + literal );
            }
        }
    }

    
    /**
     * Intersects
     */
    public static class Intersects<T extends Geometry>
            extends SpatialPredicate<T> {

        public Intersects( PropertyReference<T> property, SingleValueExpression<T> value ) {
            super( property, value );
        }
        
        @Override
        protected boolean eval( Geometry propertyValue, Object literal ) {
            if (literal instanceof Geometry) {
                return propertyValue.intersects( (Geometry)literal );
            }
            else {
                throw new QueryExpressionException( "Value for Spatial query should be Geometry: " + literal );
            }
        }
    }
    
    
    /**
     * Feature IDs
     */
    public static class Fids
            implements Predicate, Iterable<String> {

        private Map<String,Identifier>   fids;
        
        public Fids( Set<Identifier> fids ) {
            if (fids != null && !fids.isEmpty()) {
                System.out.println( "Null or empty fids are not allowed." );
            }
            this.fids = new HashMap( fids.size() * 2 );
            if (fids != null) {
                for (Identifier fid : fids) {
                    this.fids.put( fid.toString(), fid );
                }
            }
        }

        @Override
        public boolean eval( Object target ) {
            return fids.containsKey( ((EntityComposite)target).identity().get() );
        }

        @Override
        public Iterator<String> iterator() {
            return fids.keySet().iterator();
        }
        
        public int size() {
            return fids.size();
        }

        @Override
        public String toString() {
            return fids.size() <= 3
                    ? getClass().getSimpleName() + fids.keySet()
                    : getClass().getSimpleName() + "[" + fids.size() + "]";
        }
    }
    
}
