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

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;

import org.polymap.core.qi4j.idgen.HRIdentityGeneratorService;

import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreInfo;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreQueryService;
import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreService;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class RepositoryAssembler {

    Application                 app;

    UnitOfWorkFactory           uowf;

    Module                      module;


    public RepositoryAssembler() {
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
        domainModule.addEntities(
                Person.class,
                Company.class
                );
//        domainModule.addValues(
//                AktivitaetValue.class,
//                BiotoptypValue.class,
//                PflanzeValue.class,
//                PilzValue.class,
//                TierValue.class
//        );

        // persistence: workspace/Lucene
//        File moduleRoot = new File( "/home/falko/servers/workspace-biotop/data/org.polymap.biotop/" );

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
