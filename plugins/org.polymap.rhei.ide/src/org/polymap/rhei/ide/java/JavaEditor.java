/* 
 * polymap.org
 * Copyright 2011-2012, Falko Bräutigam. All rights reserved.
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

import java.util.Arrays;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.rwt.widgets.codemirror.CodeMirror;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.text.edits.ReplaceEdit;

import org.polymap.core.runtime.UIJob;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.ide.RheiIdePlugin;
import org.polymap.rhei.ide.editor.RunScriptAction;
import org.polymap.rhei.ide.editor.ScriptEditor;
import org.polymap.rhei.ide.java.JavaCompletionProvider;

/**
 * Java script code editor.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 2.0
 */
public class JavaEditor
        extends ScriptEditor {

    static Log log = LogFactory.getLog( JavaEditor.class );

    @SuppressWarnings("hiding")
    public static final String          ID = "org.polymap.rhei.ide.JavaEditor";
    
    protected ICompilationUnit          workingCopy;
    
    protected IProblemRequestor         problemHandler;
    
    
    public void init( IEditorSite _site, IEditorInput _input )
    throws PartInitException {
        super.init( _site, _input );
        actions.add( new RunScriptAction( this ) );

        addCompletionProvider( new JavaCompletionProvider() );
    }

    
    public void dispose() {
        try {
            if (workingCopy != null) {
                workingCopy.discardWorkingCopy();
            }
        }
        catch (JavaModelException e) {
            log.warn( e.getLocalizedMessage(), e );
        }
    }

    
    public ICompilationUnit getWorkingCopy() {
        return workingCopy;
    }


    public void createPartControl( Composite parent ) {
        super.createPartControl( parent );
        
        editor.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent ev ) {
                if (ev.getPropertyName().equals( CodeMirror.PROP_TEXT )) {
                    applyTextEdit();
                }
                
                PropertyChangeEvent ev2 = new PropertyChangeEvent( 
                        JavaEditor.this,
                        ev.getPropertyName(),
                        ev.getOldValue(), ev.getNewValue() );
            }
        });
    }


    protected void applyTextEdit() {
        UIJob job = new UIJob( "Applying edit...") {
            protected void runWithException( IProgressMonitor monitor )
            throws Exception {
                try {
                    String text = editor.getText();
                    final ReplaceEdit edit = new ReplaceEdit( 0, workingCopy.getBuffer().getLength(), text );

                    workingCopy.applyTextEdit( edit, monitor );
                }
                catch (JavaModelException e) {
                    log.warn( "", e );
                    //PolymapWorkbench.handleError( RheiIdePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
                }
            }
        };
        job.setPriority( Job.SHORT );
        job.schedule();
    }

    
    public void doSave( IProgressMonitor monitor ) {
        try {
            workingCopy.commitWorkingCopy( false, monitor );
            updateDirtyState( false );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiIdePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    @SuppressWarnings("deprecation")
    public void doLoad( IProgressMonitor monitor ) {
        try {
            problemHandler = new ProblemHandler();
            workingCopy = (ICompilationUnit)JavaCore.create( getEditorInput().getFile() );
            workingCopy.becomeWorkingCopy( problemHandler, monitor );
            
            editor.setText( workingCopy.getBuffer().getContents() );
            updateDirtyState( false );

            updateMarkers();
        }
        catch (JavaModelException e) {
            throw new RuntimeException( e );
        }
    }


    public boolean isSaveAsAllowed() {
        return false;
    }

    
    protected List gatherSelections( int start, int end ) 
    throws Exception {
        List elms = super.gatherSelections( start, end );

        if (workingCopy != null) {
            IJavaElement[] javaElms = workingCopy.codeSelect( start, end-start );
            elms.addAll( Arrays.asList( javaElms ) );
            
            if (javaElms.length > 0) {
                //                if (selectedJavaElements[0] instanceof IMember) {
                //                    ISourceRange name = ((IMember)selectedJavaElements[0]).getNameRange();
                //                    if (name != null) {
                //                        editor.setSelection( name.getOffset(), name.getOffset() + name.getLength() );
                //                    }
                //                }
            }
        }
        return elms;
    }
    
    
    /**
     * 
     */
    class ProblemHandler
            implements IProblemRequestor {

        public void acceptProblem( IProblem problem ) {
            log.info( "PROBLEM: " + problem );
        }

        public void beginReporting() {
        }

        public void endReporting() {
        }

        public boolean isActive() {
            return true;
//            boolean result = getSite().getPage().getActiveEditor() == this;
//            return result;
        }
    }
    
}
