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

import java.util.Map;

import org.geotools.feature.NameImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;

import com.google.common.base.Joiner;

/**
 * Adapter between a Qi4j {@link org.qi4j.api.property.Property} of a
 * {@link ValueComposite} and an OGC property.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 3.1
 */
public class ValuePropertyAdapter
        implements org.opengis.feature.Property {

    private Property                delegate;
    
    private Property<ValueComposite> valueProperty;

    private String                  prefix;
    
    private boolean                 readOnly;


    /**
     * 
     * 
     * @param delegate The property of a {@link ValueComposite} to delegate to.
     * @param valueProperty The property of the {@link EntityComposite} that holds the {@link ValueComposite}.
     */
    public ValuePropertyAdapter( Property delegate, Property<? extends ValueComposite> valueProperty ) {
        this( null, delegate, valueProperty );
    }


    /**
     * 
     * @param prefix
     * @param delegate The property of a {@link ValueComposite} to delegate to.
     * @param valueProperty The property of the {@link EntityComposite} that holds the {@link ValueComposite}.
     */
    public ValuePropertyAdapter( String prefix, Property delegate, Property<? extends ValueComposite> valueProperty ) {
        assert valueProperty != null && delegate != null;
        this.prefix = prefix;
        this.valueProperty = (Property<ValueComposite>)valueProperty;
        this.delegate = delegate;
    }


    protected Property delegate() {
        return delegate;
    }

    
    public Name getName() {
        return new NameImpl( Joiner.on( "" ).skipNulls().join( prefix, delegate.qualifiedName().name() ) );
    }

    
    public PropertyType getType() {
        return new AttributeTypeImpl( getName(), (Class<?>)delegate.type(), false, false, null, null, null );
    }

    
    public PropertyDescriptor getDescriptor() {
        // signal that we are a 'complex' property
        // see FormEditor#doSave() for implementation detail
        return null;
    }

    
    public Object getValue() {
        return delegate.get();
    }

    
    public void setValue( final Object value ) {
        if (!readOnly) {
            ValueComposite oldValue = valueProperty.get();
            ValueBuilder<ValueComposite> vbuilder = oldValue.buildWith();
            final ValueComposite newValue = vbuilder.prototype();
            
            // copy/set properties
            oldValue.state().visitProperties( new StateHolder.StateVisitor() {
                public void visitProperty( QualifiedName name, Object propValue ) {
                    newValue.state().getProperty( name ).set( 
                            name.equals( delegate.qualifiedName() ) ? value : propValue );
                }
            });
            // set value property
            valueProperty.set( vbuilder.newInstance() );
        }
    }

    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean isNillable() {
        throw new RuntimeException( "not yet implemented." );
    }

}
