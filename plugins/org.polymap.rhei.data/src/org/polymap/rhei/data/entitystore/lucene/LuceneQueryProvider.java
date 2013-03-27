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
package org.polymap.rhei.data.entitystore.lucene;

import static com.google.common.collect.Iterables.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.data.util.Identifiers;
import org.polymap.core.model.EntityType;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.QueryExpression;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.ResultSet;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordQuery;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordState;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

import org.polymap.rhei.data.entityfeature.EntityProvider.FidsQueryExpression;
import org.polymap.rhei.data.entityfeature.EntityProvider.FidsQueryProvider;

/**
 * Converts OGC {@link Filter} into Lucene query. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneQueryProvider
        implements FidsQueryProvider {

    public static Log log = LogFactory.getLog( LuceneQueryProvider.class );

    private static final Query      ALL = new MatchAllDocsQuery();
    
    private LuceneRecordStore       store;


    public LuceneQueryProvider( LuceneRecordStore store ) {
        this.store = store;
        //this.schema = schema;
    }


    public FidsQueryExpression convert( final org.geotools.data.Query input, FeatureType schema, EntityType entityType )
    throws Exception {
        // FID query?
        if (input.getFilter() instanceof Id) {
            Set<Identifier> identifiers = ((Id)input.getFilter()).getIdentifiers();
            Set<String> fids = Sets.newHashSet( transform( identifiers, Identifiers.asString() ) );
            return new FidsQueryExpression( fids );
        }
        
        Query typeQuery = new TermQuery( new Term( "type", entityType.getName() ) );

        // convert input query
        final Converter converter = new Converter( schema );
        Query filterQuery = converter.processQuery( input );
        
        BooleanQuery query = new BooleanQuery();
        query.add( typeQuery, BooleanClause.Occur.MUST );
        query.add( filterQuery, BooleanClause.Occur.MUST );

        log.debug( StringUtils.abbreviate( "LUCENE query: [" + query.toString() + "]", 256 ) );

        // build RecordQuery
        LuceneRecordQuery recordQuery = new LuceneRecordQuery( store, query );
        if (input.getStartIndex() != null) {
            recordQuery.setFirstResult( input.getStartIndex() );
        }
        if (input.getMaxFeatures() > 0) {
            recordQuery.setMaxResults( input.getMaxFeatures() );
        }
        if (input.getSortBy() != null && input.getSortBy().length > 0) {
            if (input.getSortBy().length > 1) {
                throw new UnsupportedOperationException( "More than 1 SortBy is not supported yet." );
            }
            SortBy sortBy = input.getSortBy()[0];
            String propName = sortBy.getPropertyName().getPropertyName();
            recordQuery.sort( propName, 
                    sortBy.getSortOrder() == SortOrder.ASCENDING ? RecordQuery.ASC : RecordQuery.DESC,
                    schema.getDescriptor( propName ).getType().getBinding() );
        }

        // execute Lucene query
        Timer timer = new Timer();
        final ResultSet rs = store.find( recordQuery );
        log.debug( "    non-processed results: " + rs.count() + " (" + timer.elapsedTime() + "ms)" );

        // result: FidsQueryExpression
        return new FidsQueryExpression( null ) {
            private Filter      filter = input.getFilter();
            private boolean     hasProcess = !converter.postProcess.isEmpty();
            @Override
            public <E> Iterable<E> entities( final QiModule repo, final Class<E> type, int _firstResult, int _maxResults ) {
                return transform( rs, new Function<IRecordState,E>() {
                    public E apply( IRecordState record ) {
                        return repo.findEntity( type, (String)record.id() );
                    }
                });
            }
            @Override
            public int entitiesSize() {
                return rs.count();
            }
            @Override
            public boolean hasPostProcess() {
                return hasProcess;
            }
            @Override
            public Feature postProcess( Feature feature ) {
                return !hasProcess || filter.evaluate( feature ) ? feature : null;
            }
        };
    }
    
    
    /**
     * 
     */
    class Converter {

        private FeatureType         schema;
        
        /** Filters that cannot be translated by the {@link FidsQueryProvider}. */
        private List<Filter>        postProcess = new ArrayList();


        public Converter( FeatureType schema ) {
            this.schema = schema;
        }


        public Query processQuery( org.geotools.data.Query input ) {
            if (input.getStartIndex() != null) {
                throw new UnsupportedOperationException( "startIndex != null is not supported yet.");
            }
            if (input.getSortBy() != null && input.getSortBy().length > 0) {
                throw new UnsupportedOperationException( "sortBy != null is not supported yet.");
            }
            return processFilter( input.getFilter() );    
        }

        
        public Query processFilter( Filter filter ) {
            // start
            if (filter == null) {
                return ALL;
            }
            // AND
            else if (filter instanceof And) {
                BooleanQuery result = new BooleanQuery();
                for (Filter child : ((And)filter).getChildren()) {
                    if (child instanceof Not) {
                        result.add( processFilter( ((Not)child).getFilter() ), BooleanClause.Occur.MUST_NOT );                        
                    }
                    else {
                        result.add( processFilter( child ), BooleanClause.Occur.MUST );
                    }
                }
                return result;
            }
            // OR
            else if (filter instanceof Or) {
                BooleanQuery result = new BooleanQuery();
                for (Filter child : ((Or)filter).getChildren()) {
                    // XXX child == Not?
                    result.add( processFilter( child ), BooleanClause.Occur.SHOULD );
                }
                return result;
            }
            // NOT
            else if (filter instanceof Not) {
                BooleanQuery result = new BooleanQuery();
                Filter child = ((Not)filter).getFilter();
                result.add( processFilter( child ), BooleanClause.Occur.MUST_NOT );
                return result;
            }
            // INCLUDE
            else if (filter instanceof IncludeFilter) {
                return ALL;
            }
            // EXCLUDE
            else if (filter instanceof ExcludeFilter) {
                // XXX any better way to express
                return new TermQuery( new Term( "__does_not_exist__", "true") );
            }
            // BBOX
            else if (filter instanceof BBOX) {
                return processBBOX( (BBOX)filter );
            }
            // BinarySpatial
            else if (filter instanceof BinarySpatialOperator) {
                return processBinarySpatial( (BinarySpatialOperator)filter );
            }
            // FID
            else if (filter instanceof Id) {
                Id fidFilter = (Id)filter;
                if (fidFilter.getIdentifiers().size() > BooleanQuery.getMaxClauseCount()) {
                    BooleanQuery.setMaxClauseCount( fidFilter.getIdentifiers().size() );
                }
                BooleanQuery result = new BooleanQuery();
                for (Identifier fid : fidFilter.getIdentifiers()) {
                    Query fidQuery = store.getValueCoders().searchQuery( 
                            new QueryExpression.Equal( LuceneRecordState.ID_FIELD, fid.getID() ) );
                    result.add( fidQuery, BooleanClause.Occur.SHOULD );
                }
                return result;
            }
            // comparison
            else if (filter instanceof BinaryComparisonOperator) {
                return processComparison( (BinaryComparisonOperator)filter );
            }
            // isLike
            else if (filter instanceof PropertyIsLike) {
                return processIsLike( (PropertyIsLike)filter );
            }
            // isNull
            else if (filter instanceof PropertyIsNull) {
                throw new UnsupportedOperationException( "PropertyIsNull" );
            }
            // between
            else if (filter instanceof PropertyIsBetween) {
                throw new UnsupportedOperationException( "PropertyIsBetween" );
            }
            else {
                throw new UnsupportedOperationException( "Unsupported filter type: " + filter.getClass() );
            }
        }


        @SuppressWarnings("deprecation")
        protected Query processBBOX( final BBOX bbox ) {
            String propName = bbox.getPropertyName();
            //assert !propName.equals( "" ) : "Empty propName no supported for BBOX filter.";
            final String fieldName = propName.equals( "" ) ? schema.getGeometryDescriptor().getLocalName() : propName;
            
            return store.getValueCoders().searchQuery( 
                    new QueryExpression.BBox( fieldName, bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY() ) );
        }

        
        protected org.apache.lucene.search.Query processBinarySpatial( BinarySpatialOperator filter ) {
            PropertyName prop = (PropertyName)filter.getExpression1();
            Literal literal = (Literal)filter.getExpression2();
            
            // fieldName
            final String fieldName = prop.getPropertyName().equals( "" ) 
                    ? schema.getGeometryDescriptor().getLocalName() 
                    : prop.getPropertyName();

            // get bounds for bbox
            Envelope bounds = null;
            if (literal.getValue() instanceof Geometry) {
                bounds = ((Geometry)literal.getValue()).getEnvelopeInternal();
            }
            else {
                throw new IllegalArgumentException( "Geometry type not supported: " + literal.getValue() );
            }
            
            postProcess.add( filter );
            
            return store.getValueCoders().searchQuery( 
                    new QueryExpression.BBox( fieldName, bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY() ) );
        }


        protected Query processComparison( BinaryComparisonOperator predicate ) {
            Expression expression1 = predicate.getExpression1();
            Expression expression2 = predicate.getExpression2();

            Literal literal = null;
            PropertyName prop = null;

            // expression1
            if (expression1 instanceof Literal) {
                literal = (Literal)expression1;
            }
            else if (expression1 instanceof PropertyName) {
                prop = (PropertyName)expression1;
            }
            else {
                throw new RuntimeException( "Expression type not supported: " + expression1 );
            }

            // expression2
            if (expression2 instanceof Literal) {
                literal = (Literal)expression2;
            }
            else if (expression2 instanceof PropertyName) {
                prop = (PropertyName)expression2;
            }
            else {
                throw new RuntimeException( "Expression type not supported: " + expression2 );
            }

            if (literal == null || prop == null) {
                throw new RuntimeException( "Comparison not supported: " + expression1 + " - " + expression2 );
            }

            String fieldname = prop.getPropertyName();

            // equals
            if (predicate instanceof PropertyIsEqualTo) {
                return store.getValueCoders().searchQuery( 
                        new QueryExpression.Equal( fieldname, literal.getValue() ) );
            }
            // not equals
            if (predicate instanceof PropertyIsNotEqualTo) {
                Query arg = store.getValueCoders().searchQuery( 
                        new QueryExpression.Equal( fieldname, literal.getValue() ) );
                BooleanQuery result = new BooleanQuery();
                result.add( arg, BooleanClause.Occur.MUST_NOT );
                return result;
            }
            // ge
            else if (predicate instanceof PropertyIsGreaterThanOrEqualTo) {
                return store.getValueCoders().searchQuery( 
                        new QueryExpression.GreaterOrEqual( fieldname, literal.getValue() ) );
            }
            // gt
            else if (predicate instanceof PropertyIsGreaterThan) {
                return store.getValueCoders().searchQuery( 
                        new QueryExpression.Greater( fieldname, literal.getValue() ) );
            }
            // le
            else if (predicate instanceof PropertyIsLessThanOrEqualTo) {
                return store.getValueCoders().searchQuery( 
                        new QueryExpression.LessOrEqual( fieldname, literal.getValue() ) );
            }
            // lt
            else if (predicate instanceof PropertyIsLessThan) {
                return store.getValueCoders().searchQuery( 
                        new QueryExpression.Less( fieldname, literal.getValue() ) );
            }
            else {
                throw new UnsupportedOperationException( "Predicate type not supported in comparison: " + predicate.getClass() );
            }
        }


        protected Query processIsLike( PropertyIsLike predicate ) {
            String value = predicate.getLiteral();
            PropertyName prop = (PropertyName)predicate.getExpression();
            String fieldname = prop.getPropertyName();

            // assuming that QueryExpression.Match use *,?
            value = StringUtils.replace( value, predicate.getWildCard(), "*" );
            value = StringUtils.replace( value, predicate.getSingleChar(), "?" );

            return store.getValueCoders().searchQuery( 
                    new QueryExpression.Match( fieldname, value ) );
        }


//        /**
//         * Handle the contains predicate.
//         * <p/>
//         * Impl. note: This needs a patch in
//         * org.qi4j.runtime.query.grammar.impl.PropertyReferenceImpl<T> to work with
//         * Qi4j 1.0.
//         */
//        protected Query processContainsPredicate( ContainsPredicate predicate ) {
//            final int maxElements = 10;
//    
//            PropertyReference property = predicate.propertyReference();
//            final String baseFieldname = property2Fieldname( property );
//            SingleValueExpression valueExpression = (SingleValueExpression)predicate.valueExpression();
//    
//            BooleanQuery result = new BooleanQuery();
//            for (int i=0; i<maxElements; i++) {
//                final BooleanQuery valueQuery = new BooleanQuery();
//                
//                final ValueComposite value = (ValueComposite)valueExpression.value();
//                ValueModel valueModel = (ValueModel)ValueInstance.getValueInstance( value ).compositeModel();
//                List<PropertyType> actualTypes = valueModel.valueType().types();
//                //                    json.key( "_type" ).value( valueModel.valueType().type().name() );
//    
//    
//                // all properties of the value
//                final int index = i;
//                value.state().visitProperties( new StateVisitor() {
//                    public void visitProperty( QualifiedName name, Object propValue ) {
//                        if (propValue == null) {
//                        }
//                        else if (propValue.toString().equals( "-1" )) {
//                            // FIXME hack to signal that this non-optional(!) value is not to be considered
//                            log.warn( "Non-optional field ommitted: " + name.name() + ", value=" + propValue );
//                        }
//                        else {
//                            String fieldname = baseFieldname + "[" + index + "]" + LuceneEntityState.SEPARATOR_PROP + name.name();
//    
//                            Property<Object> fieldProperty = value.state().getProperty( name );
//                            String encodedValue = ValueCoder.encode( propValue, (Class)fieldProperty.type() );
//    
//                            // checking for wildcards in the value, like in the matches predicate;
//                            // this might not be the selmantics of contains predicate but it is useless
//                            // if one cannot do a search without (instead of just a strict match)
//                            Query propQuery = null;
//                            if (encodedValue.endsWith( "*" ) 
//                                    && StringUtils.countMatches( encodedValue, "*" ) == 1
//                                    && StringUtils.countMatches( encodedValue, "?" ) == 0) {
//                                propQuery = new PrefixQuery( new Term( fieldname, encodedValue.substring( 0, encodedValue.length()-1 ) ) );
//                            }
//                            else if (StringUtils.countMatches( encodedValue, "*" ) > 1
//                                    || StringUtils.countMatches( encodedValue, "?" ) > 0) {
//                                propQuery = new WildcardQuery( new Term( fieldname, encodedValue ) );
//                            }
//                            else {
//                                propQuery = new TermQuery( new Term( fieldname, encodedValue ) );
//                            }
//                            
//                            valueQuery.add( propQuery, BooleanClause.Occur.MUST );
//                        }
//                    }
//                });
//    
//                result.add( valueQuery, BooleanClause.Occur.SHOULD );
//            }
//            return result;
//        }
    
    
//    /**
//     * Build the field name for the Lucene query. 
//     */
//    protected String property2Fieldname( PropertyReference property ) {
////        Class type = property.propertyType();
////        Class declaringType = property.propertyDeclaringType();
////        Method accessor = property.propertyAccessor();
//
//        String prefix = "";
//        PropertyReference traversedProperty = property.traversedProperty();
//        if (traversedProperty != null) {
//            prefix = property2Fieldname( traversedProperty ) + LuceneEntityState.SEPARATOR_PROP;
//        }
//        AssociationReference traversedAssoc = property.traversedAssociation();
//        if (traversedAssoc != null) {
//            throw new UnsupportedOperationException( "Traversed association in query. (Property:" + property.propertyName() + ")" );
//        }
//        
//        return prefix + property.propertyName();
//    }

    }
    
    
    public static boolean supports( Filter _filter ) {
        final List notSupported = new ArrayList();
        _filter.accept( new DefaultFilterVisitor() {
            public Object visit( Beyond filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Contains filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Crosses filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Disjoint filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Divide expression, Object data ) {
                notSupported.add( expression );
                return super.visit( expression, data );
            }
            public Object visit( DWithin filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( org.opengis.filter.expression.Function expression, Object data ) {
                notSupported.add( expression );
                return super.visit( expression, data );
            }
            public Object visit( Intersects filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Multiply expression, Object data ) {
                notSupported.add( expression );
                return super.visit( expression, data );
            }
            public Object visit( Overlaps filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Subtract expression, Object data ) {
                notSupported.add( expression );
                return super.visit( expression, data );
            }
            public Object visit( Touches filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
            public Object visit( Within filter, Object data ) {
                notSupported.add( filter );
                return super.visit( filter, data );
            }
        }, notSupported );
        
        return notSupported.isEmpty();
    }
    
}