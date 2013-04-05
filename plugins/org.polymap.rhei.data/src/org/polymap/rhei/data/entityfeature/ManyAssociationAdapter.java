/* 
 * polymap.org
 * Copyright 2013 Polymap GmbH. All rights reserved.
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
import java.util.List;
import java.util.Map;

import org.geotools.feature.NameImpl;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;

/**
 * Provides an {@link Association} as OGC property. 
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 * @since 3.1
 */
public class ManyAssociationAdapter<T>
        implements Property {

    private String          name;

    private ManyAssociation<T>  association;
    

    public ManyAssociationAdapter( String name, ManyAssociation<T> association ) {
        this.name = name;
        this.association = association;
    }

    public Name getName() {
        return new NameImpl( name );
    }

    public PropertyType getType() {
        throw new RuntimeException( "not yet implemented." );
    }

    public PropertyDescriptor getDescriptor() {
        // signal that we are a 'complex' property
        // see FormEditor#doSave() for implementation detail
        return null;
    }

    public Object getValue() {
        return association.toList();
    }

    public void setValue( Object value ) {
        // merge the two lists
        Collection<T> toAdd = (Collection<T>)value;
        // alle vorhandene objekte aus der association rauswerfen und alle neuen reintun
        // und das über add und remove
        int count = association.count();
        List<T> toRemove = new ArrayList<T>();
        for (int i=0; i<count; i++) {
            T current = association.get( i );
            if (toAdd.contains( current )) {
                toAdd.remove( current );
            } else {
                toRemove.add( current );
            }
        }
        // delete all toRemoves
        for (T current : toRemove) {
            association.remove( current );
        }
        // add all toAdds
        for (T current : toAdd) {
            association.add( current );
        }
    }

    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean isNillable() {
        throw new RuntimeException( "not yet implemented." );
    }

}