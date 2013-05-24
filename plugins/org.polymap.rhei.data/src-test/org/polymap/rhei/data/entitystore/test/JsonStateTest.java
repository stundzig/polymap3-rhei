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
package org.polymap.rhei.data.entitystore.test;

import java.util.Collection;
import java.util.Date;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.event.ModelChangeSupport;

import org.polymap.rhei.data.model.JsonState;

/**
 * Test of {@link JsonState}
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class JsonStateTest {

    private static Log log = LogFactory.getLog( JsonStateTest.class );
    
    private static RepositoryAssembler assembler;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.rhei.data", "debug" );

        assembler = new RepositoryAssembler( Person.class, Company.class );
        assembler.setValues( EventValue.class );
        Energy4Java qi4j = new Energy4Java();

        ApplicationSPI application = qi4j.newApplication( new ApplicationAssembler() {
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory ) throws AssemblyException {
                ApplicationAssembly app = applicationFactory.newApplicationAssembly();
                assembler.assemble( app );
                return app;
            }
        });

        assembler.setApp( application );
        application.activate();
    }


    @Test
    public void serialize() throws Exception {
        UnitOfWork uow = assembler.uowf.newUnitOfWork();
        Company company = uow.newEntity( Company.class );
        company.name().set( "theCompany" );
        Date now = new Date();
        company.date().set( now );
        
        ValueBuilder<EventValue> builder = assembler.module.valueBuilderFactory().newValueBuilder( EventValue.class );
        builder.prototype().name().set( "event1" );
        EventValue event = builder.newInstance();
        company.event().set( event );
        company.events().get().add( event );
        
        JSONObject json = company.encodeJsonState( false );
        System.out.println( json.toString( 4 ) );
        
        Assert.assertEquals( "theCompany", json.getString( "name" ) );
        uow.discard();
    }

    
    @Test
    public void deserialize() throws Exception {
        QiModule repo = assembler.newModule();
        
        JSONObject json = new JSONObject();
        json.put( "name", "name" );
        
        Company company = repo.newEntity( Company.class, null );
        company.decodeJsonState( json, repo, false );
        
        Assert.assertEquals( "name", company.name().get() );
        repo.revertChanges();
    }

    
    /**
     * 
     */
    @Mixins({ 
        JsonState.Mixin.class, 
        QiEntity.Mixin.class,
        ModelChangeSupport.Mixin.class
        })
    public static interface Company
            extends JsonState, QiEntity, EntityComposite {

        @Optional
        public Property<String>         name();
        
        @Optional
        public Property<Integer>        intNumber();
        
        @Optional
        public Property<Float>          floatNumber();
        
        @Optional
        public Property<Date>           date();
        
        @Optional
        public Association<Person>      chief();
        
        public ManyAssociation<Person>  employees();
        
        @Optional
        public Property<EventValue> event();

        @Optional
        @UseDefaults
        public Property<Collection<EventValue>> events();
    }

    
    /**
     * 
     */
    public static interface Person
            extends EntityComposite {
    
        @Optional
        public Property<String>     name();
        
        @Optional
        public Property<Integer>    age();
    }


    /**
     * 
     */
    public static interface EventValue
            extends ValueComposite {
        
        @Optional
        public Property<String>         name();
        
        @Optional
        public Property<Integer>        intNumber();
        
        @Optional
        public Property<Float>          floatNumber();
        
        @Optional
        public Property<Date>           date();
    }

}
