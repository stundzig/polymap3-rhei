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
package org.polymap.rhei.data.entitystore.lucene;

import java.util.ArrayList;
import java.util.Collection;

import org.qi4j.runtime.types.CollectionType;
import org.qi4j.spi.property.ValueType;

import com.google.common.base.Joiner;

/**
 * 
 * <p/>
 * XXX don't store the entire collection on every add()/remove()
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class ValueCollection
        extends ArrayList {
    
    LuceneEntityState       state;
    
    String                  fieldName;
    
    private ValueType       propertyType;
    
    
    ValueCollection( LuceneEntityState state, String fieldName, ValueType propertyType ) {
        this.state = state;
        this.fieldName = fieldName;    
        this.propertyType = propertyType;

        Integer size = state.record.get( fieldName + "__length");
        if (size != null) {
            ValueType collectedType = ((CollectionType)propertyType).collectedType();

            for (int i=0; i<size; i++) {
                Object elm = state.loadProperty( 
                        Joiner.on( "" ).join( fieldName, "[", i, "]" ), 
                        collectedType );
                super.add( elm );
            }
        }
    }
    
    
    ValueCollection( LuceneEntityState state, String fieldName, ValueType propertyType, Collection value ) {
        this.state = state;
        this.fieldName = fieldName;    
        this.propertyType = propertyType;
        
        super.addAll( value );
    }
    
    
    void store() {
        ValueType collectedType = ((CollectionType)propertyType).collectedType();
        int count = 0;
        for (Object collectedValue : this) {
            state.storeProperty( Joiner.on( "" ).join( fieldName, "[", count++, "]" ), 
                    collectedType, collectedValue );
        }
        // ignore removed entries, just update the length field
        state.record.put( fieldName + "__length", count );
        
        state.markUpdated();
    }
    
    
    public boolean add( Object object ) {
        LuceneEntityState.log.debug( "add(): object=" + object );
        if (super.add( object )) {
            store();
            return true;
        }
        return false;
    }


    public void add( int index, Object object ) {
        LuceneEntityState.log.debug( "add(): index=" + index + ", object=" + object );
        super.add( index, object );
        store();
    }


    public boolean addAll( int index, Collection coll ) {
        if (super.addAll( index, coll )) {
            store();
            return true;
        }
        return false;
    }
    
    
    public Object remove( int index ) {
        LuceneEntityState.log.debug( "remove(): index=" + index );
        Object result = super.remove( index );
        store();
        return result;
    }
    
    
    public boolean remove( Object o ) {
        LuceneEntityState.log.debug( "remove(): o=" + o );
        boolean result = super.remove( o );
        store();
        return result;
    }


    public Object set( int index, Object object ) {
        LuceneEntityState.log.debug( "set(): index=" + index + ", object=" + object );
        Object result = super.set( index, object );
        store();
        return result;
    }


    public boolean addAll( Collection c ) {
        throw new RuntimeException( "not yet implemented." );
    }


    public void clear() {
        throw new RuntimeException( "not yet implemented." );
    }


    public boolean removeAll( Collection c ) {
        throw new RuntimeException( "not yet implemented." );
    }


    public boolean retainAll( Collection c ) {
        throw new RuntimeException( "not yet implemented." );
    }
    
}