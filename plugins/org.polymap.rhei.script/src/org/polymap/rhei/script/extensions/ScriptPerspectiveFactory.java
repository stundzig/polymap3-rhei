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
package org.polymap.rhei.script.extensions;

import java.util.Collections;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.script.RheiScriptPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ScriptPerspectiveFactory
        implements IPerspectiveFactory {

    private static Log log = LogFactory.getLog( ScriptPerspectiveFactory.class );

    @Override
    public void createInitialLayout( IPageLayout layout ) {
        String perspectiveName = Polymap.instance().getInitRequestParam( "perspective", null );
        
        try {
            IProject project = RheiScriptPlugin.getOrCreateScriptProject();
            IFile src = project.getFolder( "src/perspectives" ).getFile( perspectiveName + ".java" );
            
            if (!src.exists()) {
                PolymapWorkbench.handleError( RheiScriptPlugin.PLUGIN_ID, this, "No source file found: " + src.getFullPath().toOSString(), null );
                return;
            }
        
            try {
                ScriptRunner<IPerspectiveFactory> script = 
                        new ScriptRunner<IPerspectiveFactory>( src, Collections.EMPTY_MAP );

                script.delegate.createInitialLayout( layout );
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( RheiScriptPlugin.PLUGIN_ID, this, "An error occured while executing a script.", e );
            }
//            catch (Exception e) {
//                log.warn( "Script error: ", e );
//            }
        }
        catch (Exception e) {
            // don't break everything if no forms dir or something
            log.error( "", e );
        }
    }
    
}
