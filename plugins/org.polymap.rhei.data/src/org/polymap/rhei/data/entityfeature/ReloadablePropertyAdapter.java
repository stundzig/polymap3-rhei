/*
 * polymap.org Copyright 2013 Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.rhei.data.entityfeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import org.qi4j.api.entity.Entity;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class ReloadablePropertyAdapter<T extends Entity>
        implements org.opengis.feature.Property {

    private String                     name;

    private CompositeProvider<T>       provider;

    private PropertyCallback<T>        pcb;

    private AssociationCallback<T>     acb;

    private ManyAssociationCallback<T> mcb;


    public ReloadablePropertyAdapter( CompositeProvider<T> provider, String name, PropertyCallback<T> cb ) {
        this.provider = provider;
        this.name = name;
        this.pcb = cb;
    }


    public ReloadablePropertyAdapter( CompositeProvider<T> provider, String name, AssociationCallback<T> cb ) {
        this.provider = provider;
        this.name = name;
        this.acb = cb;
    }


    public ReloadablePropertyAdapter( CompositeProvider<T> provider, String name, ManyAssociationCallback<T> cb ) {
        this.provider = provider;
        this.name = name;
        this.mcb = cb;
    }


    public Name getName() {
        return new NameImpl( name );
    }


    public Object getValue() {
        if (pcb != null) {
            Property p = getCurrentProperty();
            return (p == null ? null : p.get());
        }
        else if (acb != null) {
            Association p = getCurrentAssociation();
            return (p == null ? null : p.get());
        }
        else {
            ManyAssociation p = getCurrentManyAssociation();
            return (p == null ? null : p.toList());
        }
    }


    public void setValue( Object value ) {
        if (pcb != null) {
            Property p = getCurrentProperty();
            if (p != null) {
                p.set( value );
            }
        }
        else if (acb != null) {
            Association p = getCurrentAssociation();
            if (p != null) {
                p.set( value );
            }
        }
        else {
            ManyAssociation association = getCurrentManyAssociation();
            if (association != null) {
                Collection<Object> toAdd = (Collection<Object>)value;
                // alle vorhandene objekte aus der association rauswerfen und alle neuen reintun
                // und das über add und remove
                int count = association.count();
                List<Object> toRemove = new ArrayList<Object>();
                for (int i=0; i<count; i++) {
                    Object current = association.get( i );
                    if (toAdd.contains( current )) {
                        toAdd.remove( current );
                    } else {
                        toRemove.add( current );
                    }
                }
                // delete all toRemoves
                for (Object current : toRemove) {
                    association.remove( current );
                }
                // add all toAdds
                for (Object current : toAdd) {
                    association.add( current );
                }
            }
        }
    }


    private Property getCurrentProperty() {
        if (provider.get() != null) {
            return pcb.get( provider.get() );
        }
        return null;
    }


    private Association getCurrentAssociation() {
        if (provider.get() != null) {
            return acb.get( provider.get() );
        }
        return null;
    }


    private ManyAssociation getCurrentManyAssociation() {
        if (provider.get() != null) {
            return mcb.get( provider.get() );
        }
        return null;
    }

    @Override
    public PropertyType getType() {
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public PropertyDescriptor getDescriptor() {
        return null;
    }


    @Override
    public boolean isNillable() {
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }


    public static class CompositeProvider<T> {

        private T composite;


        public T get() {
            return composite;
        }


        public void set( T composite ) {
            this.composite = composite;
        }
    }


    public interface PropertyCallback<T extends Entity> {

        Property get( T entity );
    }


    public interface AssociationCallback<T extends Entity> {

        Association get( T entity );
    }


    public interface ManyAssociationCallback<T extends Entity> {

        ManyAssociation get( T entity );
    }
}