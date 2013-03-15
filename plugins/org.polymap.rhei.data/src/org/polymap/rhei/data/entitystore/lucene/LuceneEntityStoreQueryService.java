/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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

import java.util.Arrays;

import java.io.IOException;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;

import com.google.common.base.Function;
import static com.google.common.collect.Iterables.transform;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.ResultSet;
import org.polymap.core.runtime.recordstore.lucene.GeometryValueCoder;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordQuery;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordState;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

import org.polymap.rhei.data.entityfeature.GetBoundsQuery;

/**
 * This query service relies on the store directly, which is Lucene. 
 * 
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
@Mixins({ 
        LuceneEntityStoreQueryService.LuceneEntityFinderMixin.class,
})
public interface LuceneEntityStoreQueryService
        extends EntityFinder, ServiceComposite {

    /**
     * 
     */
    public static class LuceneEntityFinderMixin
            implements EntityFinder {

        private static Log log = LogFactory.getLog( LuceneQueryParserImpl.class );

        @Service
        private LuceneEntityStoreService    entityStoreService;
        
        private LuceneQueryParserImpl       queryParser;

        private IdentityFieldSelector       identityFieldSelector = new IdentityFieldSelector();

        
        public Iterable<EntityReference> findEntities( 
                String resultType,
                BooleanExpression whereClause, 
                OrderBy[] orderBySegments, 
                Integer firstResult,
                Integer maxResults )
                throws EntityFinderException {

            try {
                Timer timer = new Timer();
                log.debug( "findEntities(): resultType=" + resultType + ", where=" + whereClause + ", maxResults=" + maxResults );

                final LuceneRecordStore store = entityStoreService.getStore();
                queryParser = queryParser != null ? queryParser : new LuceneQueryParserImpl( store );
                
                // getBounds
                if (whereClause instanceof GetBoundsQuery) {
                    return getBounds( resultType, (GetBoundsQuery)whereClause );
                }
                
                if (firstResult != null && firstResult.intValue() != 0) {
                    throw new UnsupportedOperationException( "Not implemented yet: firstResult != 0" );
                }

                // build Lucene query
                Query query = queryParser.createQuery( resultType, whereClause, orderBySegments );

                // execute Lucene query
                final IndexSearcher searcher = store.getIndexSearcher();
                TopDocs topDocs = searcher.search( query, maxResults != null ? maxResults : Integer.MAX_VALUE );
                final ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                log.debug( "    results: " + scoreDocs.length + " (" + timer.elapsedTime() + "ms)" );
                
                return transform( Arrays.asList( scoreDocs ), new Function<ScoreDoc,EntityReference>() {
                    public EntityReference apply( ScoreDoc input ) {
                        try {
                            // use record store instead of getting the document directly
                            // to allow the cache to optimize access
                            LuceneRecordState record = store.get( input.doc );
                            return EntityReference.parseEntityReference( (String)record.id() );
                        }
                        catch (Exception e) {
                            throw new RuntimeException( e );
                        }
                    }
                } );
            }
            catch (Exception e) {
                throw new EntityFinderException( e );
            }
        }


        public EntityReference findEntity( 
                String resultType, BooleanExpression whereClause )
                throws EntityFinderException {
            
            try {
                LuceneRecordStore store = entityStoreService.getStore();
                queryParser = queryParser != null ? queryParser : new LuceneQueryParserImpl( store );
                
                IndexSearcher searcher = store.getIndexSearcher();
                Query query = queryParser.createQuery( resultType, whereClause, null );
                TopDocs topDocs = searcher.search( query, 1 );
                ScoreDoc[] scoreDocs = topDocs.scoreDocs;
                
                if (scoreDocs.length > 0) { 
                    int docnum = scoreDocs[0].doc;
                    Document doc = searcher.doc( docnum, identityFieldSelector );
                    return EntityReference.parseEntityReference( doc.get( LuceneRecordState.ID_FIELD ) );
                }
                else {
                    return null;
                }
            }
            catch (Exception e) {
                throw new EntityFinderException( e );
            }
        }

        
        /**
         * 
         */
        class IdentityFieldSelector
                implements FieldSelector {

            public FieldSelectorResult accept( String fieldName ) {
                return fieldName.equals( "identity" ) 
                        ? FieldSelectorResult.LOAD : FieldSelectorResult.NO_LOAD;
            }
            
        }

        
        public long countEntities( String resultType, BooleanExpression whereClause )
        throws EntityFinderException {
            log.debug( "countEntities(): resultType=" + resultType + ", where=" + whereClause );

            // Lucene does not like Integer.MAX_VALUE
            int maxResults = 10000000;
            try {
                Timer timer = new Timer();
                LuceneRecordStore store = entityStoreService.getStore();
                queryParser = queryParser != null ? queryParser : new LuceneQueryParserImpl( store );
                
                Query query = queryParser.createQuery( resultType, whereClause, null );
                IndexSearcher searcher = store.getIndexSearcher();
                // XXX cache this result for subsequent findEntity() calls
                TopDocs topDocs = searcher.search( query, maxResults );
                
//                ScoreDoc[] scoreDocs = topDocs.scoreDocs;
//                int result = scoreDocs.length;
                
                int result = topDocs.totalHits;
                
                log.debug( "    results: " + result + " (" + timer.elapsedTime() + "ms)" );
                return result;
            }
            catch (IOException e) {
                throw new EntityFinderException( e );
            }
        }
        
        
        protected Iterable<EntityReference> getBounds( String resultType, GetBoundsQuery query )
        throws EntityFinderException {
            Timer timer = new Timer();
            String typeName = resultType;
            String geomName = query.getGeomName();

            LuceneRecordStore store = entityStoreService.getStore();
            
            // type/name query
            Query luceneQuery = queryParser.createQuery( resultType, null, null );
            LuceneRecordQuery rsQuery = new LuceneRecordQuery( store, luceneQuery );
            rsQuery.setMaxResults( 1 );

            try {
                // MinX
                String fieldName = geomName+GeometryValueCoder.FIELD_MINX;
                rsQuery.sort( fieldName, RecordQuery.ASC, Double.class );
                ResultSet resultSet = store.find( rsQuery );
                if (resultSet.count() == 0) {
                    return ListUtils.EMPTY_LIST;
                }
                EntityReference minX = EntityReference.parseEntityReference( (String)resultSet.get( 0 ).id() );

                // MaxX
                fieldName = geomName+GeometryValueCoder.FIELD_MAXX;
                rsQuery.sort( fieldName, RecordQuery.DESC, Double.class );
                resultSet = store.find( rsQuery );
                EntityReference maxX = EntityReference.parseEntityReference( (String)resultSet.get( 0 ).id() );

                // MinY
                fieldName = geomName+GeometryValueCoder.FIELD_MINY;
                rsQuery.sort( fieldName, RecordQuery.ASC, Double.class );
                resultSet = store.find( rsQuery );
                EntityReference minY = EntityReference.parseEntityReference( (String)resultSet.get( 0 ).id() );

                // MaxX
                fieldName = geomName+GeometryValueCoder.FIELD_MAXY;
                rsQuery.sort( fieldName, RecordQuery.DESC, Double.class );
                resultSet = store.find( rsQuery );
                EntityReference maxY = EntityReference.parseEntityReference( (String)resultSet.get( 0 ).id() );

                log.info( "Bounds: ... (" + timer.elapsedTime() + "ms)" );
                
                return Arrays.asList( minX, maxX, minY, maxY );
            }
            catch (Exception e) {
                throw new EntityFinderException( e );
            }        
        }
        
    }

}
