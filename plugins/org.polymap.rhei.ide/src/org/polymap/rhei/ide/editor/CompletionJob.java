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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.widgets.codemirror.CodeMirror;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.UIJob;

import org.polymap.rhei.ide.editor.ICompletionProvider.ProposalHandler;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class CompletionJob
        extends UIJob
        implements ProposalHandler, PropertyChangeListener {

    private static Log log = LogFactory.getLog( CompletionJob.class );

    private Timer               timer;
    
    private ScriptEditor        editor;
    
    private IProgressMonitor    monitor;
    
    private int                 maxProposals = 100;
    
    private List<ICompletion>   proposals = new ArrayList( maxProposals );
    
    
    public CompletionJob( ScriptEditor editor ) {
        super( "Completions..." );
        this.editor = editor;
        setPriority( SHORT );
    }

    
    public void propertyChange( PropertyChangeEvent ev ) {
        if (ev.getPropertyName().equals( CodeMirror.PROP_CURSOR_POS )
                && getState() == Job.NONE) {
            schedule();
        }
    }


    protected void runWithException( IProgressMonitor _monitor )
    throws Exception {
        monitor = _monitor;
        timer = new Timer();
        String text = getText();
        int pos = getPos();
        
        if (pos < text.length() && pos >= 0 
                && !Character.isJavaIdentifierPart( text.charAt( pos ) )
                && (Character.isJavaIdentifierPart( text.charAt( pos-1 ) )
                        || text.charAt( pos-1 ) == '.')) {
            
            // call all providers
            List<ICompletionProvider> providers = new ArrayList( editor.completionProviders );
            for (ICompletionProvider provider : providers) {
                try {
                    provider.findProposals( this );
                }
                catch (Exception e) {
                    log.warn( "Provider failed: " + provider, e );
                }
            }
            
            // open completions in editor
            if (!proposals.isEmpty()) {
                List<ICompletion> sorted = new ArrayList( proposals );
                Collections.sort( sorted, new Comparator<ICompletion> () {
                    public int compare( ICompletion o1, ICompletion o2 ) {
                        return -(o1.getRelevance() - o2.getRelevance());
                    }
                });
                editor.editor.openCompletions( new ArrayList<org.eclipse.rwt.widgets.codemirror.ICompletion>( sorted ) );
                proposals.clear();
            }
        }
        log.info( getName() + " ready. (" + proposals.size() + " in " + timer.elapsedTime() + "ms)" );
    }


    // ProposalHandler ************************************
    
    public ScriptEditor getEditor() {
        return editor;
    }

    public IProgressMonitor getMonitor() {
        return monitor;
    }

    public int getPos() {
        return editor.editor.getCursorPos();
    }

    public String getText() {
        return editor.editor.getText();
    }

    public void handle( ICompletion proposal ) {
        //log.info( "PROPOSAL: " + new String( proposal.getCompletion() ) );
        proposals.add( proposal );
        if (proposals.size() > maxProposals) {
            monitor.setCanceled( true );
        }
    }
    
}
