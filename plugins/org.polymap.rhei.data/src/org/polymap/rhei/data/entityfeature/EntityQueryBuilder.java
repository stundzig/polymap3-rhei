/* 
 * polymap.org
 * Copyright 2011-2013, Falko Bräutigam. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;

import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Intersects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;

import com.vividsolutions.jts.geom.Envelope;

import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;

/**
 * Builds Qi4j {@link BooleanExpression} out of Feature {@link Filter}.
 * <p/>
 * Impl. note: just a skeleton, work in progress...
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.0
 */
class EntityQueryBuilder {

    private static final Log log = LogFactory.getLog( EntityQueryBuilder.class );

    private EntityType      entityType; 
    
    
    public EntityQueryBuilder( EntityType entityType ) {
        this.entityType = entityType;
    }


    public BooleanExpression convert( Filter filter ) throws Exception {
        BooleanExpression result = null;
        
        // EXCLUDE
        if (filter.equals( Filter.EXCLUDE )) {
            result = SpatialPredicate.EXCLUDE;
        }
        // INCLUDE
        else if (filter.equals( Filter.INCLUDE )) {
            result = SpatialPredicate.INCLUDE;
        }
        // AND
        else if (filter instanceof And) {
            result = new ListConverter( ((And)filter).getChildren() ) {
                BooleanExpression expression( BooleanExpression lhs, BooleanExpression rhs, BooleanExpression... opt ) {
                    return QueryExpressions.and( lhs, rhs, opt );
                }
            }.convert();
        }
        // OR
        else if (filter instanceof Or) {
            result = new ListConverter( ((Or)filter).getChildren() ) {
                BooleanExpression expression( BooleanExpression lhs, BooleanExpression rhs, BooleanExpression... opt ) {
                    return QueryExpressions.or( lhs, rhs, opt );
                }
            }.convert();
        }
        // NOT
        else if (filter instanceof Not) {
            result = QueryExpressions.not( convert( ((Not)filter).getFilter() ) );
        }
        // BBOX
        else if (filter instanceof BBOX) {
            BBOX bbox = (BBOX)filter;
            String propName = bbox.getPropertyName();
            
            result = new SpatialPredicate.BBOX(
                    QueryExpressions.asPropertyExpression( property( propName ) ),
                    QueryExpressions.asTypedValueExpression( new Envelope( bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox.getMaxY() ) ) );
        }
        // Intersects
        else if (filter instanceof Intersects) {
            BinarySpatialOperator op = (BinarySpatialOperator)filter;
            PropertyName propName = (PropertyName)op.getExpression1();
            Literal literal = (Literal)op.getExpression2();
            
            try {
                // XXX I don't have a schema here, so I gues it is "geom"
                String name = propName.getPropertyName().length() != 0
                        ? propName.getPropertyName() : "geom";
                
                result = new SpatialPredicate.Intersects(
                        QueryExpressions.asPropertyExpression( property( name ) ),
                        QueryExpressions.asTypedValueExpression( literal.getValue() ) );
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException( "The Geometry property of the entity should be named 'geom'!" );
            }
        }
        // Comparison
        else if (filter instanceof BinaryComparisonOperator) {
            BinaryComparisonOperator comparison = (BinaryComparisonOperator)filter;
            // XXX expect propName and literal in order
            PropertyName propName = (PropertyName)comparison.getExpression1();
            Literal literal = (Literal)comparison.getExpression1();

            if (comparison instanceof PropertyIsEqualTo) {
                result = QueryExpressions.eq( property( propName.getPropertyName() ), literal.getValue() );
            }
            // XXX other comparisons...
        }
        // Like
        else if (filter instanceof PropertyIsLike) {
            PropertyIsLike isLike = (PropertyIsLike)filter;
            PropertyName propName = (PropertyName)isLike.getExpression();
            String literal = isLike.getLiteral();
            // XXX conversion to MATCHES does only work with LuceneQueryBuilder
            result = QueryExpressions.matches( property( propName.getPropertyName() ), literal );
        }
        // Feature Id
        else if (filter instanceof Id) {
            result = new SpatialPredicate.Fids( ((Id)filter).getIdentifiers() );
        }
        // nothing found?
        if (result == null) {
            throw new IllegalStateException( "Unable to build entity query for filter: " + filter );
        }
        log.debug( "Entity query: " + result );
        return result;
    }

    
    protected Property property( String name ) throws Exception {
        Entity template = QueryExpressions.templateFor( entityType.getType() );
        Method m = entityType.getType().getMethod( name, new Class[0] );
        return (Property)m.invoke( template, new Object[0] );
    }
    

    /**
     * Handles the special Qi4j query API with left + right + optional
     * arguments.
     */
    abstract class ListConverter {
        
        abstract BooleanExpression expression( BooleanExpression lhs, BooleanExpression rhs, BooleanExpression... opt);
 
        private List<Filter>        filters;
        
        
        public ListConverter( List<Filter> filters ) {
            this.filters = filters;    
        }
        
        public BooleanExpression convert() throws Exception {
            Entity template = QueryExpressions.templateFor( entityType.getType() );
            
            // build children
            List<BooleanExpression> children = new ArrayList<BooleanExpression>();
            for (Filter filter : filters) {
                BooleanExpression child = EntityQueryBuilder.this.convert( filter );
                if (child != null) {
                    children.add( child );
                }
            }
            
            switch (children.size()) {
                case 0 : {
                    return null;
                }
                case 1 : {
                    return children.get( 0 );
                }
                case 2 : {
                    return expression( children.get( 0 ), children.get( 1 ) );
                }
                default : {
                    List<BooleanExpression> tail = children.subList( 2, children.size()-2 );
                    return expression( 
                            children.get( 0 ),
                            children.get( 1 ),
                            tail.toArray( new BooleanExpression[tail.size()] ) );
                }
            }
        }
        
    }

}
