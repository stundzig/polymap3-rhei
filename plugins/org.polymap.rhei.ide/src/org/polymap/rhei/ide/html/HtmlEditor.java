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
package org.polymap.rhei.ide.html;

import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.filters.StringInputStream;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.rwt.widgets.codemirror.CodeMirror;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.ide.RheiIdePlugin;
import org.polymap.rhei.ide.editor.RunScriptAction;
import org.polymap.rhei.ide.editor.ScriptEditor;

/**
 * Java script code editor.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 2.0
 */
public class HtmlEditor
        extends ScriptEditor {

    static Log log = LogFactory.getLog( HtmlEditor.class );

    @SuppressWarnings("hiding")
    public static final String          ID = "org.polymap.rhei.ide.JavaEditor";
    
    protected IFile                     file;
    
    protected String                    workingCopy;
    
    
    public void init( IEditorSite _site, IEditorInput _input )
    throws PartInitException {
        super.init( _site, _input );
        actions.add( new RunScriptAction( this ) {
            public void run() {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
        });

        //addCompletionProvider( new JavaCompletionProvider() );
    }

    
    public void dispose() {
    }

    
//    public ICompilationUnit getWorkingCopy() {
//        return workingCopy;
//    }


    public void createPartControl( Composite parent ) {
        super.createPartControl( parent );
        
        editor.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent ev ) {
                if (ev.getPropertyName().equals( CodeMirror.PROP_TEXT )) {
                    applyTextEdit();
                }
                
//                PropertyChangeEvent ev2 = new PropertyChangeEvent( 
//                        HtmlEditor.this,
//                        ev.getPropertyName(),
//                        ev.getOldValue(), ev.getNewValue() );
            }
        });
    }


    protected void applyTextEdit() {
        UIJob job = new UIJob( "Applying edit...") {
            protected void runWithException( IProgressMonitor monitor )
            throws Exception {
                workingCopy = editor.getText();
            }
        };
        job.setPriority( Job.SHORT );
        job.schedule();
    }

    
    public void doSave( IProgressMonitor monitor ) {
        try {
            file.setContents( new StringInputStream( workingCopy ), true, true, monitor );

            updateDirtyState( false );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiIdePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    @SuppressWarnings("deprecation")
    public void doLoad( IProgressMonitor monitor ) {
        try {
            file = getEditorInput().getFile();
            workingCopy = String.valueOf( IOUtils.toCharArray( file.getContents(), "UTF8" ) );
            editor.setText( workingCopy );
            updateDirtyState( false );

            updateMarkers();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    public boolean isSaveAsAllowed() {
        return false;
    }

    
    protected List gatherSelections( int start, int end ) 
    throws Exception {
        List elms = super.gatherSelections( start, end );

//        if (workingCopy != null) {
//            IJavaElement[] javaElms = workingCopy.codeSelect( start, end-start );
//            elms.addAll( Arrays.asList( javaElms ) );
//            
//            if (javaElms.length > 0) {
//                //                if (selectedJavaElements[0] instanceof IMember) {
//                //                    ISourceRange name = ((IMember)selectedJavaElements[0]).getNameRange();
//                //                    if (name != null) {
//                //                        editor.setSelection( name.getOffset(), name.getOffset() + name.getLength() );
//                //                    }
//                //                }
//            }
//        }
        return elms;
    }
    
}
