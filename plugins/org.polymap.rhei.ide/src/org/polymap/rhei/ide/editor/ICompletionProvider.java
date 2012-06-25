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

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.UIJob;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ICompletionProvider {

    /**
     * Find completion proposals. The given handler provides context information via
     * its getters. Results should be passed to the
     * {@link ProposalHandler#handle(ICompletion)} method.
     * <p/>
     * Method is called from within a dedicated {@link UIJob}. Implementations should
     * use the given {@link ProposalHandler#getMonitor() monitor}. The cancel state
     * of the monitor signals that no more proposals should be computed (timeout or
     * count limit reached.
     * 
     * @param handler
     * @throws Exception
     */
    void findProposals( ProposalHandler handler ) throws Exception;

    
    /**
     * Handler for completion proposals. Also provides context information
     * for the provider. 
     */
    interface ProposalHandler {

        ScriptEditor getEditor();
        
        String getText();
        
        int getPos();
        
        IProgressMonitor getMonitor();
        
        /**
         * Takes that given completion proposal.
         * <p/>
         * Callers should check cancel state of the handler's monitor.
         * 
         * @param proposal
         */
        void handle( ICompletion proposal );
        
    }
    
}
