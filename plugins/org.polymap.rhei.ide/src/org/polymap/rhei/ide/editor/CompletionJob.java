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
package org.polymap.rhei.ide.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.UIJob;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class CompletionJob
        extends UIJob {

    private static Log log = LogFactory.getLog( CompletionJob.class );

    private ScriptEditor        editor;
    
    private ICompilationUnit    cu;
    
    private int                 offset;

    
    public CompletionJob( ScriptEditor editor, ICompilationUnit cu, int offset ) {
        super( "Completions..." );
        this.editor = editor;
        this.cu = cu;
        this.offset = offset;
        setPriority( SHORT );
    }

    
    protected void runWithException( IProgressMonitor monitor )
            throws Exception {
        Timer timer = new Timer();
        cu.codeComplete( offset, new CompletionRequestor() {
            public void accept( CompletionProposal proposal ) {
                log.info( "PROPOSAL: " + new String( proposal.getCompletion() ) );
            }
        }, monitor );
        log.info( getName() + " ...ready. (" + timer.elapsedTime() + "ms)" );
    }
    
}
