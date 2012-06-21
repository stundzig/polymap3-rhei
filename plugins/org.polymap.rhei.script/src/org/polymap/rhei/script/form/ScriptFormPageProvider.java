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

import java.io.OutputStreamWriter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.opengis.feature.Feature;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormPageProvider;
import org.polymap.rhei.script.RheiScriptPlugin;
import org.polymap.rhei.script.java.ScriptParams;
import org.polymap.rhei.script.java.ScriptedFormEditorPage;

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
        final List<IFormEditorPage> result = new ArrayList();

        IProject project = RheiScriptPlugin.getOrCreateScriptProject();
        IFolder formsFolder = project.getFolder( "src/forms" );
        
        try {
            formsFolder.accept( new IResourceVisitor() {
                public boolean visit( IResource resource ) throws CoreException {
                    if (resource instanceof IFile ) {
                        try {
                            IFile file = (IFile)resource;
                            String code = IOUtils.toString( file.getContents(), file.getCharset() );

                            ScriptEngine engine = manager.getEngineByExtension( file.getFileExtension() );
                            if (engine != null) {
                                engine.getContext().setWriter( new OutputStreamWriter( System.out, "UTF-8" ) );
                                engine.getContext().setErrorWriter( new OutputStreamWriter( System.err, "UTF-8" ) );

                                engine.put( "_feature", feature );
                                engine.put( "_fs", formEditor.getFeatureStore() );
                                ScriptParams.init()
                                        .put( "feature", feature )
                                        .put( "fs", formEditor.getFeatureStore() );

                                try {
                                    engine.eval( code );
                                    
                                    //WantsToBeShown scripted = ((Invocable)engine).getInterface( WantsToBeShown.class );
                                    ScriptedFormEditorPage page = (ScriptedFormEditorPage)engine.getContext().getAttribute( "result" );
                                    if (page != null && page.wantsToBeShown()) {
                                        result.add( page );
                                    }
                                }
                                finally {
                                    ScriptParams.dispose();
                                }

//                            IFormEditorPage page = (IFormEditorPage)engine.getContext().getAttribute( "result" );
//                            if (page != null) {
//                                result.add( page );
//                            }
                            }
                            else {
                                log.warn( "No ScriptEngine for extension: " + file.getFileExtension() );
                            }
                        }
                        catch (Exception e) {
                            log.warn( "Script error: ", e );
                        }
                    }
                    return true;
                }
            });
        }
        catch (CoreException e) {
            throw new RuntimeException( e );
        }
        return result;
    }

}
