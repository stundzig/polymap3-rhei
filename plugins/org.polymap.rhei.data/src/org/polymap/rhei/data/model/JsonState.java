/* 
 * polymap.org
 * Copyright 2012-2013, Falko Bräutigam. All rigths reserved.
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
package org.polymap.rhei.data.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.property.StateHolder.StateVisitor;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.polymap.core.model.Entity;
import org.polymap.core.qi4j.QiModule;

/**
 * This interface and mixin allows to de/serialize the state of an
 * {@link EntityComposite} to/from JSON {@link JSONObject}.
 * <p/>
 * Originally developed for ANTA2/Vanko code base (http://polymap.org/vanko/).
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 2.0
 */
@Mixins(
        JsonState.Mixin.class
)
public interface JsonState
        extends Entity {

    /**
     * This might be bad, I'm not quite sure about it yet. However, I just don't
     * know (again) how to access the state of an entity from client code.
     */
    public StateHolder state();

    /**
     * 
     * @see Entity#state()
     * @param entity The entity to copy the state from.
    */
    public void copyStateFrom( final JsonState entity );
    
    public JSONObject encodeJsonState( boolean withTweaks );

    public void decodeJsonState( JSONObject value, QiModule repo, boolean withTweaks );

    /**
     * Encode the given property value.
     * <p>
     * This method allows to intercept the encoding by concerns.
     * @param withTweaks 
     * 
     * @return String, "true|false", {@link JSONObject#NULL}
     */
    public Object encodeProperty( QualifiedName name, @Optional Object value, 
            boolean withTweaks );

    public Object decodeProperty( Class type, Object value, boolean withTweaks );

    /**
     * Encode the given {@link ValueComposite}..
     * <p>
     * This method allows to intercept the encoding by concerns.
     * @param withTweaks 
     * 
     * @return Newly created {@link JSONObject} or {@link JSONObject#NULL}.
     */
    public Object encodeValueComposite( QualifiedName name, @Optional ValueComposite value, 
            boolean withTweaks );

    public Object decodeValueComposite( Class<? extends ValueComposite> type,
            final JSONObject value, final QiModule repo, final boolean withTweaks );


    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements JsonState {
        
        private static Log log = LogFactory.getLog( Mixin.class );
        
        /** Near ISO dateTime format, which is "yyyy-MM-dd'T'HH:mm:ss". */
//        public static final FastDateFormat df = FastDateFormat.getInstance( "yyyy-MM-dd' 'HH:mm" );
        public static final SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd' 'HH:mm" );

        @State
        private StateHolder         stateHolder;
        
        @This
        private JsonState           composite;
        

        public StateHolder state() {
            return stateHolder;
        }

        public void copyStateFrom( final JsonState entity ) {
            entity.state().visitProperties( new StateVisitor() {
                public void visitProperty( QualifiedName name, Object value ) {
                    Property<Object> prop = stateHolder.getProperty( name );
                    if (!prop.isImmutable() && !prop.isComputed() && !name.name().equals( "identity" )) {
                        if (value instanceof Property) {
                            prop.set( ((Property)value).get() );
                        }
                        else {
                            throw new RuntimeException( "Unknown value type: " + value.getClass() );
                        }
                    }
                }
            });
        }

        
        public JSONObject encodeJsonState( final boolean withTweaks ) {
            final JSONObject result = new JSONObject();
            
            stateHolder.visitProperties( new StateVisitor() {
                public void visitProperty( QualifiedName name, Object value ) {
                    Property<Object> prop = stateHolder.getProperty( name );
                    encode( name, prop.get(), result, withTweaks );
                }
            });
            return result;
        }

        
        public void decodeJsonState( final JSONObject json, final QiModule repo, final boolean withTweaks ) {
            stateHolder.visitProperties( new StateVisitor() {
                public void visitProperty( QualifiedName name, Object value ) {
                    Property<Object> prop = stateHolder.getProperty( name );
                    decode( prop, json, repo, withTweaks );
                }
            });
        }

        /**
         * Recursivly build the JSON.
         * 
         * @param name
         * @param value
         * @param result
         * @param withTweaks 
         */
        protected void encode( QualifiedName name, Object value, JSONObject result, boolean withTweaks ) {
            try {
                // Collection
                if (value instanceof Collection) {
                    JSONArray subResult = new JSONArray();
                    
                    int index = 0;
                    for (Object _value : (Collection)value) {
                        JSONObject subSubResult = new JSONObject();
                        encode( QualifiedName.fromName( name.type(), "name" ), _value, subSubResult, withTweaks );
                        subResult.put( index++, subSubResult.get( "name" ) );
                    }
                    result.put( name.name(), subResult );
                }
                // ValueComposite
                else if (value instanceof ValueComposite) {
                    result.put( name.name(), withTweaks
                            ? composite.encodeValueComposite( name, (ValueComposite)value, withTweaks )
                            : encodeValueComposite( name,(ValueComposite)value, withTweaks ) );
                }
                // property
                else {
                    result.put( name.name(), withTweaks 
                            ? composite.encodeProperty( name, value, withTweaks )
                            : encodeProperty( name, value, withTweaks ) );
                }
            }
            catch (JSONException e) {
                // print exception but dont break the entire run
                log.warn( "", e );
            }
        }

        /**
         * Recursivly build the JSON.
         * 
         * @param name
         * @param repo 
         * @param value
         * @param result
         * @param withTweaks 
         */
        protected void decode( Property prop, JSONObject value, QiModule repo, boolean withTweaks ) {
            try {
                QualifiedName name = prop.qualifiedName();
                Type type = prop.type();
                
                // Collection
                if (type instanceof ParameterizedType) {
                    JSONArray array = value.optJSONArray( name.name() );
                    if (array != null) {
                        Class arrayType = (Class)((ParameterizedType)type).getActualTypeArguments()[0];

                        List list = new ArrayList();

                        for (int i=0; i<array.length(); i++) {
                            // ValueComposite
                            if (ValueComposite.class.isAssignableFrom( arrayType )) {
                                list.add( decodeValueComposite( arrayType, 
                                        array.getJSONObject( i ), repo, withTweaks ) );
                            }
                            // property
                            else {
                                list.add( decodeProperty( arrayType, array.get( i ), withTweaks ) );
                            }
                        }
                        prop.set( list );
                    }
                }
                // ValueComposite
                else if (ValueComposite.class.isAssignableFrom( (Class<?>)type )) {
                    JSONObject propValue = value.optJSONObject( name.name() );
                    if (propValue != null) {
                        prop.set( decodeValueComposite( (Class<? extends ValueComposite>)type, 
                                propValue, repo, withTweaks ) );
                    }
                }
                // property
                else {
                    if (name.name().equals( "identity" )) {
                        // skip
                    }
                    else {
                        Object propValue = value.opt( name.name() );
                        if (propValue != null) {
                            prop.set( decodeProperty( (Class)type, propValue, withTweaks ) );
                        }
                    }
                }
            }
            catch (JSONException e) {
                // print exception but don't break the entire run
                log.warn( "", e );
            }
        }

        
        public Object encodeProperty( QualifiedName name, Object value, boolean withTweaks ) {
            if (value == null) {
                return JSONObject.NULL;
            }
            // Number
            else if (value instanceof Number) {
                return value.toString();
            }
            // Boolean
            else if (value instanceof Boolean) {
                return value;
            }
            // Date
            else if (value instanceof Date) {
                return df.format( (Date)value );
            }
            // String
            else if (value instanceof String) {
                return value;
            }
            // other
            else {
                return value.toString();
            }
        }

        
        public Object decodeProperty( Class type, Object value, boolean withTweaks ) {
            if (value.equals( JSONObject.NULL )) {
                return null;
            }
            // Boolean
            else if (value instanceof Boolean) {
                return value;
            }
            // Number
            else if (Integer.class.isAssignableFrom( type )) {
                return Integer.valueOf( (String)value );
            }
            else if (Long.class.isAssignableFrom( type )) {
                return Long.valueOf( (String)value );
            }
            else if (Float.class.isAssignableFrom( type )) {
                return Float.valueOf( (String)value );
            }
            else if (Double.class.isAssignableFrom( type )) {
                return Double.valueOf( (String)value );
            }
            else if (Byte.class.isAssignableFrom( type )) {
                return Byte.valueOf( (String)value );
            }
            else if (Short.class.isAssignableFrom( type )) {
                return Short.valueOf( (String)value );
            }
            // Date
            else if (Date.class.isAssignableFrom( type )) {
                try {
                    log.debug( "        date value: " + (String)value );
                    return df.parse( (String)value );
                    //return df.parseObject( (String)value );
                }
                catch (ParseException e) {
                    throw new RuntimeException( e.getMessage() + " at: " + e.getErrorOffset(), e );
                }
            }
            // String
            else if (String.class.isAssignableFrom( type )) {
                return value;
            }
            // other
            else {
                throw new RuntimeException( "Unknown property type:" + type );
            }
        }

        
        public Object encodeValueComposite( QualifiedName name, @Optional ValueComposite value,
                final boolean withTweaks ) {
            
            final JSONObject result = new JSONObject();
            
            final StateHolder state = (value).state();
            state.visitProperties( new StateVisitor() {
                public void visitProperty( QualifiedName _name, Object _value ) {
                    Property<Object> prop = state.getProperty( _name );
                    encode( _name, prop.get(), result, withTweaks );
                }
            });
            return result;
        }
        

        public Object decodeValueComposite( Class<? extends ValueComposite> type,
                final JSONObject value, final QiModule repo, final boolean withTweaks ) {
            
            ValueBuilder<? extends ValueComposite> builder = repo.newValueBuilder( type );
            ValueComposite prototype = builder.prototype();
            
            final StateHolder state = prototype.state();
            state.visitProperties( new StateVisitor() {
                public void visitProperty( QualifiedName _name, Object _value ) {
                    Property<Object> prop = state.getProperty( _name );
                    decode( prop, value, repo, withTweaks );
                }
            });
            return builder.newInstance();
        }
        
    }

}
