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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityComposite;
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
public class UpdatedEntityQueryTest {

    private static Log log = LogFactory.getLog( UpdatedEntityQueryTest.class );
    
    private static RepositoryAssembler assembler;

    private static Person thePaul;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.rhei.data", "debug" );

        assembler = new RepositoryAssembler( Person.class );
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
        
        thePaul = uow.newEntity( Person.class );
        thePaul.name().set( "paul" );
        thePaul.age().set( 24 );
        uow.complete();
    }


    protected Query<Person> doQuery( UnitOfWork uow ) throws Exception {
        QueryBuilderFactory factory = assembler.module.queryBuilderFactory();

        Person paul = uow.get( thePaul );        
        Person template = QueryExpressions.templateFor( Person.class );
        QueryBuilder<Person> builder = factory.newQueryBuilder( Person.class )
                .where( QueryExpressions.and(
                        QueryExpressions.eq( template.name(), "paul" ),
                        QueryExpressions.eq( template.age(), 24 ) ) );
        return builder.newQuery( uow );
    }

    
    protected void checkResult( Query<Person> query ) {
        for (Person person : query) {
            Assert.assertEquals( "paul", person.name().get() );
            Assert.assertEquals( (Integer)24, person.age().get() );
        }
    }


    @Test
    public void test() throws Exception {
        UnitOfWork uow = assembler.uowf.newUnitOfWork();

        // without changes
        Query<Person> query = doQuery( uow );
        Assert.assertEquals( 1, query.count() );
        checkResult( query );
        
        // add entity
        Person person = uow.newEntity( Person.class );
        person.name().set( "paul" );
        person.age().set( 24 );
        Assert.assertEquals( 2, query.count() );
        checkResult( query );

        // modify added entity
        person.age().set( 25 );
        Assert.assertEquals( 1, query.count() );
        checkResult( query );

        // modify stored entity
        uow.get( thePaul ).age().set( 25 );
        Assert.assertEquals( 0, query.count() );
        checkResult( query );

        // reset added entity
        person.age().set( 24 );
        Assert.assertEquals( 1, query.count() );
        checkResult( query );

        // reset stored entity
        uow.get( thePaul ).age().set( 24 );
        Assert.assertEquals( 2, query.count() );
        checkResult( query );

        // remove added entity
        uow.remove( person );
        Assert.assertEquals( 1, query.count() );
        checkResult( query );
        
        // remove stored entity
        uow.remove( uow.get( thePaul ) );
        Assert.assertEquals( 0, query.count() );
        checkResult( query );
        
        // other UoW still ok?
        UnitOfWork uow2 = assembler.uowf.newUnitOfWork();
        Query<Person> query2 = doQuery( uow2 );
        Assert.assertEquals( 1, query2.count() );
        checkResult( query2 );
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

}
