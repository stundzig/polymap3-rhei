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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.lifecycle.IEntryPoint;

import org.eclipse.ui.application.IWorkbenchConfigurer;

import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.core.workbench.PolymapWorkbenchAdvisor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ScriptWorkbench
        extends PolymapWorkbench
        implements IEntryPoint {

    private static Log log = LogFactory.getLog( ScriptWorkbench.class );


    @Override
    public int createUI() {
        return createUI( new ScriptWorkbenchAdvisor() );
    }

    
    /**
     * 
     */
    class ScriptWorkbenchAdvisor
            extends PolymapWorkbenchAdvisor {

        @Override
        public void initialize( IWorkbenchConfigurer configurer ) {
            super.initialize( configurer );

            // don't restore workbench so that scripted layout gets loaded
            getWorkbenchConfigurer().setSaveAndRestore( false );
        }
        
        @Override
        public String getInitialWindowPerspectiveId() {
            return "org.polymap.rhei.script.perspective";
        }
        
    }
    
}
