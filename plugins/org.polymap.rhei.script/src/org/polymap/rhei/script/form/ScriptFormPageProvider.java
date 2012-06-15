/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.script.form;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.OutputStreamWriter;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.opengis.feature.Feature;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormPageProvider;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ScriptFormPageProvider
        implements IFormPageProvider {

    private static Log log = LogFactory.getLog( ScriptFormPageProvider.class );

    private static final ScriptEngineManager    manager = new ScriptEngineManager();
    

    public ScriptFormPageProvider() {
        for (ScriptEngineFactory f : manager.getEngineFactories()) {
            log.info( "available ScriptEngineFactory: " + f );
        }
    }

    
    public List<IFormEditorPage> addPages( final FormEditor formEditor, final Feature feature ) {
        List<IFormEditorPage> result = new ArrayList();
        
        File configDir = new File( Polymap.getConfigDir(), "org.polymap.rhei.form" );
        configDir.mkdirs();
        
        File formsDir = new File( configDir, "forms" );
        formsDir.mkdirs();
        for (File f : formsDir.listFiles()) {

            try {
                String code = FileUtils.readFileToString( f, "UTF-8" );

                ScriptEngine engine = manager.getEngineByExtension( FilenameUtils.getExtension( f.getName() ) );
                if (engine != null) {
                    engine.getContext().setWriter( new OutputStreamWriter( System.out, "UTF-8" ) );
                    engine.getContext().setErrorWriter( new OutputStreamWriter( System.err, "UTF-8" ) );

                    engine.put( "_feature", feature );
                    engine.put( "_fs", formEditor.getFeatureStore() );

                    engine.eval( code );

                    IFormEditorPage page = (IFormEditorPage)engine.getContext().getAttribute( "result" );
                    if (page != null) {
                        result.add( page );
                    }
                }
                else {
                    log.warn( "No ScriptEngine for extension: " + f );
                }
            }
            catch (Exception e) {
                log.warn( "", e );
            }
        }
        return result;
    }

}
