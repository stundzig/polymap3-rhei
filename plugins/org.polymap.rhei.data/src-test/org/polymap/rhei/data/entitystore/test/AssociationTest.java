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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.spi.structure.ApplicationSPI;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AssociationTest {

    private static Log log = LogFactory.getLog( AssociationTest.class );
    
    private static RepositoryAssembler assembler;

    private static Company theCompany;

    private static Person thePaul;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.rhei.data", "debug" );

        assembler = new RepositoryAssembler( Person.class, Company.class );
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

        // create entities
        UnitOfWork uow = assembler.uowf.newUnitOfWork();
        
        theCompany = uow.newEntity( Company.class );
        theCompany.name().set( "theCompany" );
        
        thePaul = uow.newEntity( Person.class );
        thePaul.name().set( "paul" );
        theCompany.chief().set( thePaul );
        theCompany.employees().add( thePaul );
        uow.complete();
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }


    @Before
    public void setUp() throws Exception {
    }


    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void simpleAccess() throws Exception {
        UnitOfWork uow = assembler.uowf.newUnitOfWork();
        Company company = uow.get( theCompany );        
        Assert.assertEquals( "paul", company.chief().get().name().get() );
        
        Assert.assertEquals( 1, company.employees().count() );
        Assert.assertEquals( "paul", company.employees().get( 0 ).name().get() );
    }

    
    @Test
    public void associationQuery() throws Exception {
        UnitOfWork uow = assembler.uowf.newUnitOfWork();
        QueryBuilderFactory factory = assembler.module.queryBuilderFactory();

        // paul -> found
        Person paul = uow.get( thePaul );        
        Company template = QueryExpressions.templateFor( Company.class );
        QueryBuilder<Company> builder = factory.newQueryBuilder( Company.class )
                .where( QueryExpressions.eq( template.chief(), paul ) );
        Query<Company> query = builder.newQuery( uow );
        
        Assert.assertEquals( 1, query.count() );
        Company company = query.find();
        Assert.assertEquals( "theCompany", company.name().get() );

        // anyPerson -> not found
        Person anyPerson = uow.newEntity( Person.class );
        anyPerson.name().set( "anyPerson" );

        builder = factory.newQueryBuilder( Company.class )
                .where( QueryExpressions.eq( template.chief(), anyPerson ) );
        query = builder.newQuery( uow );
        
        Assert.assertEquals( 0, query.count() );
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
    public static interface Company
            extends EntityComposite {

        @Optional
        public Property<String>         name();
        
        @Optional
        public Association<Person>      chief();
        
        public ManyAssociation<Person>  employees();
        
    }

}
