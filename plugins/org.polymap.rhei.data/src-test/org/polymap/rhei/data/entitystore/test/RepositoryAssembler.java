/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as
 * indicated by the @authors tag. All rights reserved.
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

import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.qi4j.idgen.HRIdentityGeneratorService;

import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreInfo;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreQueryService;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreService;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RepositoryAssembler
        extends QiModuleAssembler {

    Application                                app;

    UnitOfWorkFactory                          uowf;

    Module                                     module;

    private Class<? extends EntityComposite>[] entities;

    private Class<? extends ValueComposite>[]  values;    
    

    public RepositoryAssembler( Class<? extends EntityComposite>... entities ) {
        this.entities = entities;
    }

    public void setValues( Class<? extends ValueComposite>... values ) {
        this.values = values;
    }

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public void createInitData() throws Exception {
    }

    @Override
    public QiModule newModule() {
        return new QiModule( this ) {
        };
    }

    
    protected void setApp( Application app ) {
        this.app = app;
        this.module = app.findModule( "application-layer", "test-module" );
        this.uowf = module.unitOfWorkFactory();
    }


    public void assemble( ApplicationAssembly _app )
    throws AssemblyException {
        // project layer / module
        LayerAssembly domainLayer = _app.layerAssembly( "application-layer" );
        ModuleAssembly domainModule = domainLayer.moduleAssembly( "test-module" );
        domainModule.addEntities( entities );
        if (values != null) {
            domainModule.addValues( values );
        }

        domainModule.addServices( LuceneEntityStoreService.class )
                .setMetaInfo( new LuceneEntityStoreInfo() )
                .instantiateOnStartup()
                .identifiedBy( "lucene-repository" );

        // indexer
        domainModule.addServices( LuceneEntityStoreQueryService.class )
                //.visibleIn( indexingVisibility )
                //.setMetaInfo( namedQueries )
                .instantiateOnStartup();

        domainModule.addServices( HRIdentityGeneratorService.class );
    }
}
