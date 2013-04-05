/* 
 * polymap.org
 * Copyright 2011-2013, Polymap GmbH. All rights reserved.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entitystore.EntityStoreException;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LuceneManyAssociationState
        implements ManyAssociationState, Serializable {

    private static final Log log = LogFactory.getLog( LuceneManyAssociationState.class );

    private LuceneEntityState   entityState;

    private Set<String>         references = new HashSet();
    
    private String              fieldName;


    public LuceneManyAssociationState( LuceneEntityState entityState, QualifiedName stateName ) {
        try {
            this.entityState = entityState;
            fieldName = LuceneEntityState.PREFIX_MANYASSOC + stateName.name();
            
            String json = entityState.record.get( fieldName );
            if (json != null) {
                JSONArray array = new JSONArray( json );
                for (int i=0; i<array.length(); i++) {
                    references.add( array.getString( i ) );
                }
            }
        }
        catch (JSONException e) {
            throw new EntityStoreException( e );
        }
    }

    
    void store() {
        entityState.record.put( fieldName, new JSONArray( references ).toString() );
        entityState.markUpdated();
    }


    public String getFieldName() {
        return fieldName;
    }


    public int count() {
        return references.size();
    }


    public boolean contains( EntityReference entityReference ) {
        return references.contains( entityReference.identity() );
    }


    public boolean add( int index, EntityReference entityReference ) {
        if (contains( entityReference )) {
            return false;
        }
        if (index < count()) {
            throw new UnsupportedOperationException( "add( index) is not supported." );
        }
        boolean result = references.add( entityReference.identity() );
        store();
        return result;
    }


    public boolean remove( EntityReference entityReference ) {
        boolean result = references.remove( entityReference.identity() );
        store();
        return result;
    }


    public EntityReference get( int i ) {
        return EntityReference.parseEntityReference( Iterables.get( references, i ) );
    }


    public Iterator<EntityReference> iterator() {
        return Collections2.transform( references, new Function<String,EntityReference>() {
            public EntityReference apply( String input ) {
                return EntityReference.parseEntityReference( input );
            }
        }).iterator();
                
//        return new Iterator<EntityReference>() {
//            private JSONArray refs = references
//            private int index = 0;
//            @Override
//            public boolean hasNext() {
//                return index < references.length();
//            }
//            @Override
//            public EntityReference next() {
//                try {
//                    return new EntityReference( references.getString( index++ ) );
//                }
//                catch (JSONException e) {
//                    throw new NoSuchElementException();
//                }
//            }
//            @Override
//            public void remove() {
//                throw new UnsupportedOperationException( "Use ManyAssociation.remove() instead." );
//            }
//        };
    }

}
