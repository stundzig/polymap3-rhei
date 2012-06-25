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
package org.polymap.rhei.ide.java;

import java.beans.PropertyChangeEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rwt.widgets.codemirror.CodeMirror;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;

import org.polymap.rhei.ide.editor.ICompletion;
import org.polymap.rhei.ide.editor.ICompletionProvider;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class JavaCompletionProvider
        implements ICompletionProvider {

    static Log log = LogFactory.getLog( JavaCompletionProvider.class );

    
    public void propertyChange( PropertyChangeEvent ev ) {
        if (ev.getPropertyName().equals( CodeMirror.PROP_CURSOR_POS )) {
            JavaEditor editor = (JavaEditor)ev.getSource();
            ICompilationUnit cu = editor.getWorkingCopy();
            if (cu != null) {
                Integer offset = (Integer)ev.getNewValue();
                //new CompletionsFinder( editor, cu, offset ).schedule();
            }
        }
    }

    
    public void findProposals( final ProposalHandler handler ) 
    throws Exception {
        ICompilationUnit cu = ((JavaEditor)handler.getEditor()).getWorkingCopy();
        if (cu != null) {
            
            cu.codeComplete( handler.getPos(), new CompletionRequestor() {
                public void accept( final CompletionProposal proposal ) {
                    handler.handle( new ICompletion() {
                        public String getCompletion() {
                            return new String( proposal.getCompletion() );
                        }
                        public int getReplaceStart() {
                            return proposal.getReplaceStart();
                        }
                        public int getReplaceEnd() {
                            return proposal.getReplaceEnd();
                        }
                        public int getRelevance() {
                            return proposal.getRelevance();
                        }
                    });
                }
            }, handler.getMonitor() );
        }
    }

}
