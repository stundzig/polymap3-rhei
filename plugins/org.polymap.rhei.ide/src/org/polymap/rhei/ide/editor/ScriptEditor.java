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
package org.polymap.rhei.ide.editor;

import static org.polymap.rhei.ide.Messages.i18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.lf5.util.StreamUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.rwt.widgets.codemirror.CodeMirror;
import org.eclipse.rwt.widgets.codemirror.LineMarker;
import org.eclipse.rwt.widgets.codemirror.CodeMirror.TextSelection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.EditorPart;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.ListenerList;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.ide.RheiIdePlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 1.0
 */
public class ScriptEditor
        extends EditorPart 
        implements IEditorPart, IGotoMarker, IResourceChangeListener, ISelectionProvider {

    static Log log = LogFactory.getLog( ScriptEditor.class );

    public static final String          ID = "org.polymap.rhei.ide.ScriptEditor";
    
    
//    public static ScriptEditor open( IFile file ) {
//        try {
//            return open( file.getLocationURI().toURL(), file.getFileExtension() );
//        }
//        catch (MalformedURLException e) {
//            throw new RuntimeException( e );
//        }
//    }
//
//
//    /**
//     *
//     * @return The editor of the given script URL, or null.
//     */
//    public static ScriptEditor open( URL scriptUrl, String lang ) {
//        try {
//            log.debug( "open(): URL= " + scriptUrl );
//            ScriptEditorInput input = new ScriptEditorInput( scriptUrl, lang );
//
//            // check current editors
//            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//            IEditorReference[] editors = page.getEditorReferences();
//            for (IEditorReference reference : editors) {
//                IEditorInput cursor = reference.getEditorInput();
//                if (cursor instanceof ScriptEditorInput) {
//                    log.debug( "        editor: " + cursor );
//                }
//                if (cursor.equals( input )) {
//                    Object previous = page.getActiveEditor();
//                    page.activate( reference.getPart( true ) );
//                    return (ScriptEditor)reference.getEditor( false );
//                }
//            }
//
//            // not found -> open new editor
//            IEditorPart part = page.openEditor( input, input.getEditorId(), true,
//                    IWorkbenchPage.MATCH_NONE );
//            log.debug( "editor= " + part );
//            // might be ErrorEditorPart
//            return part instanceof ScriptEditor ? (ScriptEditor)part : null;
//        }
//        catch (PartInitException e) {
//            PolymapWorkbench.handleError( RheiIdePlugin.PLUGIN_ID, null, e.getMessage(), e );
//            return null;
//        }
//    }

    
    // instance *******************************************
    
    private List<Action>                actions = new ArrayList();

    private boolean                     isDirty;
    
    private boolean                     isValid;
    
    private boolean                     actionsEnabled;

    private CodeMirror                  editor;
    
    private IMarker[]                   markers;
    
    private Map<String,Object>          calculatorParams = new HashMap();
    
    /** Listeners of this {@link ISelectionProvider}. */
    private ListenerList<ISelectionChangedListener> selectionListeners = new ListenerList();

    /** The current selection of this {@link ISelectionProvider}. */
    private ISelection                  selection;
    
    
    public ScriptEditor() {
    }

    
    public void setCalculatorParams( Map<String,Object> params ) {
        this.calculatorParams.clear();    
        this.calculatorParams.putAll( params );    
    }

    
    public IFileEditorInput getEditorInput() {
        return (IFileEditorInput)super.getEditorInput();
    }


    public void init( IEditorSite _site, IEditorInput _input )
            throws PartInitException {
        super.setSite( _site );
        super.setInput( _input );

        setPartName( _input.getName() );
        //setContentDescription( "Script: " + name );
        setTitleToolTip( _input.getToolTipText() );

        // listener to resource changes
        ResourcesPlugin.getWorkspace().addResourceChangeListener( this );

        // selection provider
        getSite().setSelectionProvider( this );

        // submit action
        Action submitAction = new Action( i18n( "ScriptEditor_submit" ) ) {
            public void run() {
                try {
                    log.debug( "submitAction.run(): ..." );
                    doSave( new NullProgressMonitor() );
                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( RheiIdePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
                }
            }
        };
        submitAction.setImageDescriptor( ImageDescriptor.createFromURL( 
                RheiIdePlugin.getDefault().getBundle().getResource( "icons/etool16/validate.gif" ) ) );
        submitAction.setToolTipText( i18n( "ScriptEditor_submitTip" ) );
        actions.add( submitAction );

        // revert action
        Action revertAction = new Action( i18n( "ScriptEditor_revert" ) ) {
            public void run() {
                log.debug( "revertAction.run(): ..." );
                doLoad( new NullProgressMonitor() );
            }
        };
        revertAction.setImageDescriptor( ImageDescriptor.createFromURL( 
                RheiIdePlugin.getDefault().getBundle().getResource( "icons/etool16/revert.gif" ) ) );
        revertAction.setToolTipText( i18n( "ScriptEditor_revertTip" ) );
        actions.add( revertAction );

        // run action
        actions.add( new RunScriptAction(this) );
    }

    
    //        log.debug( "fieldChange(): dirty=" + isDirty + ", isValid=" + isValid );
    //        boolean old = actionsEnabled;
    //        actionsEnabled = isValid && isDirty;
    //        if (actionsEnabled != old) {
    //            for (Action action : standardPageActions) {
    //                action.setEnabled( actionsEnabled );
    //            }
    //            editorDirtyStateChanged();
    //        }
    //    }
    
    
    public void dispose() {
        super.dispose();
        ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );
    }


    public void resourceChanged( IResourceChangeEvent event ) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            IResourceDelta rootDelta = event.getDelta();
            // find my changes
            IResourceDelta fileDelta = rootDelta.findMember( getEditorInput().getFile().getFullPath() );
            if (fileDelta != null) {
                updateMarkers();                        
            }
        }
    }
    
    
    protected void updateMarkers() {
        try {
            editor.lineMarkers().clear();
            IMarker[] newMarkers = getEditorInput().getFile().findMarkers( null, true, 1 );
            for (IMarker marker : newMarkers) {
                editor.lineMarkers().put( new LineMarker()
                        .setLine( marker.getAttribute( IMarker.LINE_NUMBER, 0 ) )
                        .setText( marker.getAttribute( IMarker.MESSAGE, "" ) ) );
            }
            markers = newMarkers;
        }
        catch (CoreException e) {
            PolymapWorkbench.handleError( RheiIdePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }

    
    public void createPartControl( Composite parent ) {
        Composite content = new Composite( parent, SWT.NONE );
        FormLayout layout = new FormLayout();
        content.setLayout( layout );
        
        // buttonbar
        Composite buttonbar = new Composite( content, SWT.NONE );
        buttonbar.setLayout( new RowLayout() );
        buttonbar.setLayoutData( new SimpleFormData().top( 0 ).left( 0 ).right( 100 ).create() );

        // buttons
        for (final Action action : actions) {
            Button btn = new Button( buttonbar, SWT.PUSH );
            // XX dispose images
            btn.setImage( action.getImageDescriptor().createImage( true ) );
            btn.setToolTipText( action.getToolTipText() );
            btn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    action.run();
                }
            });
        }
        
        // separator
        Label sep = new Label( content, SWT.SEPARATOR | SWT.HORIZONTAL );
        sep.setLayoutData( new SimpleFormData().top( buttonbar ).left( 0 ).right( 100 ).create() );

        // editor
        editor = new CodeMirror( content, SWT.NONE );
        editor.setLayoutData( new SimpleFormData().top( sep ).left( 0 ).right( 100 ).bottom( 100 ).create() );
        
        editor.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent ev ) {
                
                if (ev.getPropertyName().equals( CodeMirror.PROP_CURSOR_POS )) {
                    int pos = (Integer)ev.getNewValue();
                    fireSelectionChanged( pos, pos );                    
                }
                else if (ev.getPropertyName().equals( CodeMirror.PROP_SELECTION )) {
                    TextSelection sel = editor.getSelection();
                    fireSelectionChanged( sel.getStart(), sel.getEnd() );
                }
                else if (ev.getPropertyName().equals( CodeMirror.PROP_TEXT )) {
                    isDirty = true;
                    Polymap.getSessionDisplay().asyncExec( new Runnable() {
                        public void run() {
                            firePropertyChange( PROP_DIRTY );
                        }
                    });
                }
            }
        });
        
        doLoad( new NullProgressMonitor() );
    }


    
//        log.debug( "fieldChange(): dirty=" + isDirty + ", isValid=" + isValid );
//        boolean old = actionsEnabled;
//        actionsEnabled = isValid && isDirty;
//        if (actionsEnabled != old) {
//            for (Action action : standardPageActions) {
//                action.setEnabled( actionsEnabled );
//            }
//            editorDirtyStateChanged();
//        }
//    }


    public void gotoMarker( IMarker marker ) {
        try {
            Integer start = (Integer)marker.getAttribute( IMarker.CHAR_START );
            Integer end = (Integer)marker.getAttribute( IMarker.CHAR_END );
            Integer ln = (Integer)marker.getAttribute( IMarker.LINE_NUMBER );
            log.info( start + " " + end + " " + ln );
            editor.setSelection( start, end );
        }
        catch (CoreException e) {
            PolymapWorkbench.handleError( RheiIdePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    public boolean isDirty() {
        return isDirty;
    }


    public void doSave( IProgressMonitor monitor ) {
        try {
            IFileEditorInput input = getEditorInput();
            InputStream in = new ByteArrayInputStream( editor.getText().getBytes( "UTF-8" ) );
            input.getFile().setContents( in, 0, monitor );
            isDirty = false;
            firePropertyChange( PROP_DIRTY );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiIdePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
    }


    public void doLoad( IProgressMonitor monitor ) {
        InputStream in = null;
        try {
            in = getEditorInput().getFile().getContents();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamUtils.copy( in, out );
            editor.setText( out.toString( "UTF-8" ) );
            isDirty = false;

            updateMarkers();
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiIdePlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
        }
        finally {
            IOUtils.closeQuietly( in );
        }
    }


    public void doSaveAs() {
        throw new RuntimeException( "not yet implemented." );
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setFocus() {
//        StructuredSelection sel = new StructuredSelection( getEditorInput().getFile() );
//        SelectionChangedEvent ev = new SelectionChangedEvent( this, sel );
//        for (ISelectionChangedListener l : selectionListeners) {
//            l.selectionChanged( ev );
//        }
    }


    // ISelectionProvider *********************************
    
    public void addSelectionChangedListener( ISelectionChangedListener listener ) {
        selectionListeners.add( listener );
    }

    public void removeSelectionChangedListener( ISelectionChangedListener listener ) {
        selectionListeners.remove( listener );
    }

    public ISelection getSelection() {
        return selection;
    }

    public void setSelection( ISelection selection ) {
        this.selection = selection;
    }
    
    protected void fireSelectionChanged( int start, int end ) {
        try {
            // XXX refactor this into org.polymap.rhei.ide.java package
            ICompilationUnit cu = (ICompilationUnit)JavaCore.create( getEditorInput().getFile() );
            IJavaElement[] selectedJavaElements = cu.codeSelect( start, end-start );
            if (selectedJavaElements.length > 0) {
                if (selectedJavaElements.length > 1) {
                    log.warn( "More than one Java elements selected: " + selectedJavaElements );
                }
                StructuredSelection sel = new StructuredSelection( selectedJavaElements[0] );

                SelectionChangedEvent ev = new SelectionChangedEvent( this, sel );
                for (ISelectionChangedListener l : selectionListeners) {
                    l.selectionChanged( ev );
                }
                
//                if (selectedJavaElements[0] instanceof IMember) {
//                    ISourceRange name = ((IMember)selectedJavaElements[0]).getNameRange();
//                    if (name != null) {
//                        editor.setSelection( name.getOffset(), name.getOffset() + name.getLength() );
//                    }
//                }
            }
        }
        catch (JavaModelException e) {
            log.warn( e.getLocalizedMessage(), e );
        }
    }

}
