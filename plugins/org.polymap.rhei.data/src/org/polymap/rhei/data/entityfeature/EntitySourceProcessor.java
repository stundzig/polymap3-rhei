/*
 * polymap.org
 * Copyright 2010, 2012 Polymap GmbH. All rights reserved.
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import java.io.IOException;

import net.refractions.udig.catalog.IService;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.identity.FeatureIdImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.value.ValueComposite;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.FeatureChangeEvent;
import org.polymap.core.data.feature.AddFeaturesRequest;
import org.polymap.core.data.feature.FidSet;
import org.polymap.core.data.feature.GetFeatureTypeRequest;
import org.polymap.core.data.feature.GetFeatureTypeResponse;
import org.polymap.core.data.feature.GetFeaturesRequest;
import org.polymap.core.data.feature.GetFeaturesResponse;
import org.polymap.core.data.feature.GetFeaturesSizeRequest;
import org.polymap.core.data.feature.GetFeaturesSizeResponse;
import org.polymap.core.data.feature.ModifyFeaturesRequest;
import org.polymap.core.data.feature.ModifyFeaturesResponse;
import org.polymap.core.data.feature.RemoveFeaturesRequest;
import org.polymap.core.data.feature.buffer.IFeatureBufferProcessor;
import org.polymap.core.data.feature.buffer.LayerFeatureBufferManager;
import org.polymap.core.data.pipeline.ITerminalPipelineProcessor;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.ProcessorRequest;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.ProcessorSignature;
import org.polymap.core.model.Entity;
import org.polymap.core.model.EntityType;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerUseCase;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.data.entityfeature.catalog.EntityGeoResourceImpl;
import org.polymap.rhei.data.entityfeature.catalog.EntityServiceImpl;

/**
 * Provides Qi4j entities as features to the pipeline. This processor is the
 * link between features and entities. It converts between the (meta) data model
 * and APIs of both worlds.
 * <p>
 * This class provides default implementation to build {@link FeatureType} and
 * {@link Feature} instances out of the given {@link EntityProvider}. The caller
 * may provide an {@link EntityProvider2} in order to control this process.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 */
public class EntitySourceProcessor
        implements ITerminalPipelineProcessor, IFeatureBufferProcessor {

    private static final Log log = LogFactory.getLog( EntitySourceProcessor.class );

    public static final int                 DEFAULT_CHUNK_SIZE = 100;


    public static ProcessorSignature signature( LayerUseCase usecase ) {
        if (usecase == LayerUseCase.FEATURES_TRANSACTIONAL ) {
            return new ProcessorSignature(
                    new Class[] {ModifyFeaturesRequest.class, RemoveFeaturesRequest.class, AddFeaturesRequest.class, GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
                    new Class[] {},
                    new Class[] {},
                    new Class[] {ModifyFeaturesResponse.class, GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class}
            );
        }
        else {
            return new ProcessorSignature(
                    new Class[] {GetFeatureTypeRequest.class, GetFeaturesRequest.class, GetFeaturesSizeRequest.class},
                    new Class[] {},
                    new Class[] {},
                    new Class[] {GetFeatureTypeResponse.class, GetFeaturesResponse.class, GetFeaturesSizeResponse.class}
            );
        }
    }

    public static boolean isCompatible( IService service ) {
        return service instanceof EntityServiceImpl;
    }


    // instance *******************************************

    /**
     * The {@link FeatureType} of the {@link EntityType} of our
     * {@link EntityProvider}. Initialized by {@link #init(Properties)}.
     * <p>
     * This break the stateless rule for processors. This schema instance is
     * shared by all calling threads.
     */
    private FeatureType             schema;

    /** The attributes that are automatically build by default. */
    private Set<String>             standardAttributes = new HashSet();
    
    /** Might be of type {@link EntityProvider2}. @see #schema */
    private EntityProvider<Entity>  entityProvider;

    private EntityQueryBuilder      filterConverter;

    private ILayer                  layer;
    

    public void init( Properties props ) {
        try {
            // init schema
            layer = (ILayer)props.get( "layer" );
            EntityGeoResourceImpl geores = layer != null
                    ? (EntityGeoResourceImpl)layer.getGeoResource()
                    : (EntityGeoResourceImpl)props.get( "geores" );
            assert geores != null : "No layer and no geores in properties of the processor.";
            entityProvider = geores.resolve( EntityProvider.class, null );
            filterConverter = new EntityQueryBuilder( entityProvider.getEntityType() );

            // EntityProvider2
            if (entityProvider instanceof EntityProvider2) {
                schema = ((EntityProvider2)entityProvider).buildFeatureType();
            }
            // build standard schema
            else {
                EntityType entityType = entityProvider.getEntityType();

                SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
                builder.setName( entityProvider.getEntityName() );

                Collection<EntityType.Property> p = entityType.getProperties();
                for (EntityType.Property prop : p) {
                    Class propType = prop.getType();

                    if (prop instanceof EntityType.Association) {
                        log.debug( "    skipping association: " + prop.getName() + " / " + propType );                        
                    }
                    else if (prop instanceof EntityType.ManyAssociation) {
                        log.debug( "    skipping many-association: " + prop.getName() + " / " + propType );
                    }
                    else if (Geometry.class.isAssignableFrom( propType )) {
                        CoordinateReferenceSystem crs = entityProvider.getCoordinateReferenceSystem( prop.getName() );
                        builder.add( prop.getName(), propType, crs );
                        builder.setDefaultGeometry( prop.getName() );
                        standardAttributes.add( prop.getName() );
                        log.debug( "    Geometry: " + prop.getName() + " / " + propType );
                    }
                    else if (ValueComposite.class.isAssignableFrom( propType )) {
                        log.debug( "    skipping complex: " + prop.getName() + " / " + propType );
                    }
                    else if (Collection.class.isAssignableFrom( propType )) {
                        log.debug( "    skipping collection: " + prop.getName() + " / " + propType );
                    }
                    else {
                        builder.add( prop.getName(), propType );
                        standardAttributes.add( prop.getName() );
                        log.debug( "    primitive: " + prop.getName() + " / " + propType );
                    }
                }
                schema = builder.buildFeatureType();
                
                // EntityProvider3
                if (entityProvider instanceof EntityProvider3) {
                    schema = ((EntityProvider3)entityProvider).buildFeatureType( schema );
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException( e.getMessage(), e );
        }
    }


    @Override
    public void revert( Filter filter, IProgressMonitor monitor ) {
        if (filter.equals( Filter.INCLUDE )) {
            entityProvider.revert();
            fireFeatureChangeEvent( Collections.EMPTY_SET, FeatureChangeEvent.Type.FLUSHED );
        }
        else {
            throw new UnsupportedOperationException( "Revert filter != INCLUDE" );
        }
    }

    
    public void processRequest( ProcessorRequest r, ProcessorContext context )
            throws Exception {
        // resolve FeatureSource
//        ILayer layer = context.getLayers().iterator().next();
//        EntityGeoResourceImpl geores = (EntityGeoResourceImpl)layer.getGeoResource();
//        EntityProvider entityProvider = geores.resolve( EntityProvider.class, null );
        log.debug( "        Request: " + r + ", provider= " + entityProvider );

        // GetFeatureType
        if (r instanceof GetFeatureTypeRequest) {
            context.sendResponse( new GetFeatureTypeResponse( schema ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // AddFeatures
        else if (r instanceof AddFeaturesRequest) {
            AddFeaturesRequest request = (AddFeaturesRequest)r;
            Set<FeatureId> fids = addFeatures( request.getFeatures() );
            fireFeatureChangeEvent( fids, FeatureChangeEvent.Type.ADDED );
            context.sendResponse( new ModifyFeaturesResponse( fids ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // RemoveFeatures
        else if (r instanceof RemoveFeaturesRequest) {
            RemoveFeaturesRequest request = (RemoveFeaturesRequest)r;
            Set<FeatureId> fids = removeFeatures( request.getFilter() );
            fireFeatureChangeEvent( fids, FeatureChangeEvent.Type.REMOVED );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // ModifyFeatures
        else if (r instanceof ModifyFeaturesRequest) {
            ModifyFeaturesRequest request = (ModifyFeaturesRequest)r;
            Set<FeatureId> fids = modifyFeatures( request.getType(), request.getValue(), request.getFilter() );
            fireFeatureChangeEvent( fids, FeatureChangeEvent.Type.MODIFIED );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // GetFeatures
        else if (r instanceof GetFeaturesRequest) {
            GetFeaturesRequest request = (GetFeaturesRequest)r;
            getFeatures( request.getQuery(), context );
            context.sendResponse( ProcessorResponse.EOP );
        }
        // GetFeaturesSize
        else if (r instanceof GetFeaturesSizeRequest) {
            GetFeaturesSizeRequest request = (GetFeaturesSizeRequest)r;
            int result = getFeaturesSize( request.getQuery() );
            context.sendResponse( new GetFeaturesSizeResponse( result ) );
            context.sendResponse( ProcessorResponse.EOP );
        }
        else {
            throw new IllegalArgumentException( "Unhandled request type: " + r );
        }
    }


    protected int getFeaturesSize( Query query )
    throws IOException {
        try {
            BooleanExpression entityQuery = entityQuery( query );

            int firstResult = query.getStartIndex() != null ? query.getStartIndex() : 0;
            int maxResults = query.getMaxFeatures() > 0 ? query.getMaxFeatures() : Integer.MAX_VALUE;

            return entityProvider.entitiesSize( entityQuery, firstResult, maxResults );
        }
        catch (IOException e) {
            throw e;
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IOException( e );
        }
    }


    protected void getFeatures( Query query, ProcessorContext context )
    throws Exception {
        assert query != null;
        if (query.getFilter() == null) {
            log.warn( "Filter is NULL -> changing to EXCLUDE to prevent unwanted loading of all features! Use INCLUDE to get all." );
        }
        Filter filter = query.getFilter() != null ? query.getFilter() : Filter.EXCLUDE;        

        long start = System.currentTimeMillis();
        log.debug( "            Filter: " + StringUtils.abbreviate( filter.toString(), 0, 256 ) );
        int firstResult = query.getStartIndex() != null ? query.getStartIndex() : 0;
        int maxResults = query.getMaxFeatures() > 0 ? query.getMaxFeatures() : Integer.MAX_VALUE;

        BooleanExpression entityQuery = entityQuery( query );

        // 1 pass: query entities
        Iterable<Entity> entities = entityProvider.entities( entityQuery, firstResult, maxResults );

        // 2 pass: filter features
        int count = 0;
        ArrayList<Feature> chunk = new ArrayList( DEFAULT_CHUNK_SIZE );
        for (Entity entity : entities) {

            Feature feature = buildFeature( entity );
            
            if (feature != null) {
                chunk.add( feature );
                if (chunk.size() >= DEFAULT_CHUNK_SIZE) {
                    chunk.trimToSize();
                    context.sendResponse( new GetFeaturesResponse( chunk ) );
                    chunk = new ArrayList( DEFAULT_CHUNK_SIZE );
                }
                if (++count >= query.getMaxFeatures()) {
                    break;
                }
            }
        }
        if (!chunk.isEmpty()) {
            chunk.trimToSize();
            context.sendResponse( new GetFeaturesResponse( chunk ) );
        }
        log.debug( "    getFeatures(): " + (System.currentTimeMillis()-start) + "ms" );
    }

    
    private BooleanExpression entityQuery( Query query ) 
    throws Exception {
        if (entityProvider instanceof EntityProvider2) {
            query = ((EntityProvider2)entityProvider).transformQuery( query );
        }
        if (entityProvider instanceof EntityProvider3) {
            query = ((EntityProvider3)entityProvider).transformQuery( query );
        }
//        // try OGC -> native query (Lucene)
//        if (entityProvider.getQueryProvider() != null) {
//            return entityProvider.getQueryProvider().convert( query, schema, entityProvider.getEntityType() );
//        }
        // try OGC -> Qi4j
        if (filterConverter != null) {
            return filterConverter.convert( query.getFilter() );
        }
        throw new RuntimeException( "No entityQuery! This is no longer supported." );
    }

    
    private Feature buildFeature( Entity entity ) {
        // EntityProvider2
        if (entityProvider instanceof EntityProvider2) {
            return ((EntityProvider2)entityProvider).buildFeature( entity, schema );
        }
        // build standard feature
        else {
            EntityType type = entityProvider.getEntityType();

            // this does not work with Geometry properties yet
//            return new EntityFeature( entity, type, (SimpleFeatureType)schema );
//        }

            // straight forward solution; 2 times slower and probably needs more memory
            SimpleFeatureBuilder fb = new SimpleFeatureBuilder( (SimpleFeatureType)schema );
            try {
                for (AttributeDescriptor attr : ((SimpleFeatureType)schema).getAttributeDescriptors()) {
                    EntityType.Property entityProp = type.getProperty( attr.getName().getLocalPart() );
                    // attribute was generated by EntityProvider2/3
                    if (entityProp != null && standardAttributes.contains( entityProp.getName() )) {
                    	try {
                    	    fb.set( attr.getName(), entityProp.getValue( entity ) );
                    	} 
                    	catch (ClassCastException e) {
                    	    log.warn( "", e );
                    	}
                    }
                }
                Feature feature = fb.buildFeature( entity.id() );
                
                // EntityProvider3
                if (entityProvider instanceof EntityProvider3) {
                    feature = ((EntityProvider3)entityProvider).buildFeature( entity, feature, schema );
                }
                
                return feature;
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
    }


//    /**
//     * Filter the given features with the given query.
//     * <p>
//     * XXX Currently it seems simpler to fetch all features and apply the filter
//     * than converting the geospatial filter to a Qi4j filter. But this can be
//     * memory consuming and might be revised later.
//     *
//     * @param features
//     * @param query
//     * @return
//     */
//    private Feature filterFeature( Feature feature, Filter filter ) {
//        return filter.evaluate( feature ) ? feature : null;
//    }


    protected Set<FeatureId> addFeatures( Collection<Feature> features )
    throws Exception {
        final EntityType entityType = entityProvider.getEntityType();

        FidSet result = new FidSet();
        for (final Feature feature : features) {

            Entity entity = entityProvider.newEntity( new EntityCreator<Entity>() {
                @Override
                public void create( Entity instance ) throws Exception {
                    for (Property featureProp : feature.getProperties()) {
                        String propName = featureProp.getName().getLocalPart();
                        Object value = featureProp.getValue();
                        
                        modifyProperty( instance, entityType, propName, value );

//                        EntityType.Property prop = type.getProperty( propName );
//                    
//                        // check values, do not overwrite default values
//                        if (prop != null && value != null && standardAttributes.contains( propName )) {
//                            prop.setValue( instance, value );
//                        }
//                        
//                        // EntityProvider2
//                        if (entityProvider instanceof EntityProvider2) {
//                            ((EntityProvider2)entityProvider).modifyFeature( instance, propName, value );
//                        }
                    }
                }
            });
            
            // assuming that buildFeature() uses id() as well
            result.add( new FeatureIdImpl( entity.id() ) );
        }
        return result;
    }


    protected Set<FeatureId> removeFeatures( Filter filter )
    throws IOException {
        log.debug( "            Filter: " + filter );
        try {
            BooleanExpression entityQuery = entityQuery( new DefaultQuery( null, filter ) );
            return entityProvider.removeEntity( entityQuery );
        }
        catch (IOException e) {
            throw e;
        }
        catch( Exception e ) {
            throw new IOException( e );
        }
    }


    protected Set<FeatureId> modifyFeatures( AttributeDescriptor[] type, Object[] value, Filter filter )
    throws IOException {
        log.debug( "            Filter: " + filter );

        FidSet result = new FidSet(); 
            
        // filter -> entities
        List<Entity> entities = new ArrayList();
        if (filter instanceof Id) {
            for (Identifier id : ((Id)filter).getIdentifiers()) {
                entities.add( entityProvider.findEntity( (String)id.getID() ) );
                result.add( (FeatureId)id );
            }
        }
        else {
            throw new RuntimeException( "Unknown filter type: " + filter );
        }

        // set values
        EntityType entityType = entityProvider.getEntityType();
        for (Entity entity : entities) {
            for (int i=0; i<type.length; i++) {
                String propName = type[i].getLocalName();
                modifyProperty( entity, entityType, propName, value[i] );
            }
        }
        return result;
    }

    
    private void modifyProperty( Entity entity, EntityType entityType, String propName, Object value ) 
            throws IOException {
        try {
            log.debug( "    modifying: prop=" + propName + ", value=" + value + ", entity=" + entity );
            // EntityProvider2
            if (entityProvider instanceof EntityProvider2) {
                ((EntityProvider2)entityProvider).modifyFeature( entity, propName, value );
            }
            // EntityProvider3
            else if (entityProvider instanceof EntityProvider3) {
                boolean applied = ((EntityProvider3)entityProvider).modifyFeature( entity, propName, value );
                if (!applied && standardAttributes.contains( propName )) {
                    entityType.getProperty( propName ).setValue( entity, value );
                }
            }
            // default
            else {
                entityType.getProperty( propName ).setValue( entity, value );
            }
        }
        catch (Exception e) {
            throw new IOException( "Fehler beim Ändern des Objektes: " + entity, e );
        }
    }
    
    
    // feature events *************************************
    
    /**
     * Fires a {@link FeatureChangeEvent} for the layer of the given context.
     * <p/>
     * For other layers this is done by the {@link LayerFeatureBufferManager}. As
     * the LFBM does not handle entity feature sources the event has to be fired
     * here explicitly. 
     *
     * @param context
     * @param features
     * @param eventType
     */
    private void fireFeatureChangeEvent( Set<FeatureId> fids, FeatureChangeEvent.Type eventType ) {
        if (layer != null) {
            FeatureChangeEvent ev = new FeatureChangeEvent( layer, eventType, fids );
            EventManager.instance().publish( ev );
        }
    }

    
    public void processResponse( ProcessorResponse reponse, ProcessorContext context )
    throws Exception {
        throw new RuntimeException( "This is a terminal processor." );
    }

}
