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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.EditorPart;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.ListenerList;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.ide.MarkerSelectionStatusLineAdapter;
import org.polymap.rhei.ide.RheiIdePlugin;

/**
 * Base implementation of a script code editor.
 * <p/>
 * Open a new editor via one of the {@link IDE#openEditor()} methods.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @since 1.0
 */
public class ScriptEditor
        extends EditorPart 
        implements IEditorPart, IGotoMarker, IResourceChangeListener, ISelectionProvider {

    static Log log = LogFactory.getLog( ScriptEditor.class );

    public static final String      ID = "org.polymap.rhei.ide.ScriptEditor";
    
    public final static Image       ERROR = RheiIdePlugin.getDefault().imageForName( "icons/elcl16/error_tsk.gif" );
    public final static Image       WARN = RheiIdePlugin.getDefault().imageForName( "icons/elcl16/warn_tsk.gif" );
    public final static Image       INFO = RheiIdePlugin.getDefault().imageForName( "icons/elcl16/info_tsk.gif" );
    
    private boolean                 isDirty;
    
    protected CodeMirror            editor;
    
    protected IMarker[]             markers;
    
    protected List<Action>          actions = new ArrayList();

    /** Listeners of this {@link ISelectionProvider}. */
    protected ListenerList<ISelectionChangedListener> selectionListeners = new ListenerList();

    /** The current selection of this {@link ISelectionProvider}. */
    protected ISelection            selection;

    protected Set<ICompletionProvider> completionProviders = new HashSet();
    
    private CompletionJob           completionJob;
    
    
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
        
        // contribute marker selections to status line
        addSelectionChangedListener( new MarkerSelectionStatusLineAdapter(
                getEditorSite().getActionBars().getStatusLineManager() ) );

        // submit action
        final Action submitAction = new Action( i18n( "ScriptEditor_submit" ) ) {
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
        final Action revertAction = new Action( i18n( "ScriptEditor_revert" ) ) {
            public void run() {
                log.debug( "revertAction.run(): ..." );
                doLoad( new NullProgressMonitor() );
            }
        };
        revertAction.setImageDescriptor( ImageDescriptor.createFromURL( 
                RheiIdePlugin.getDefault().getBundle().getResource( "icons/etool16/revert.gif" ) ) );
        revertAction.setToolTipText( i18n( "ScriptEditor_revertTip" ) );
        actions.add( revertAction );
        
        addPropertyListener( new IPropertyListener() {
            public void propertyChanged( Object source, int propId ) {
                if (propId == PROP_DIRTY) {
                    submitAction.setEnabled( isDirty() );
                    revertAction.setEnabled( isDirty() );
                }
            }
        });
    }

    
    public void dispose() {
        super.dispose();
        ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );
    }


    public void resourceChanged( IResourceChangeEvent event ) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            IResourceDelta rootDelta = event.getDelta();
            // find my changes
            IResourceDelta fileDelta = rootDelta.findMember( getEditorInput().getFile().getFullPath() );
            if (fileDelta != null && fileDelta.getKind() == IResourceDelta.REMOVED) {
                getSite().getPage().closeEditor( this, false );
            }
            else if (fileDelta != null && editor != null) {
                updateMarkers();                        
            }
        }
    }
    
    
    protected void updateMarkers() {
        try {
            editor.lineMarkers().clear();
            IMarker[] newMarkers = getEditorInput().getFile().findMarkers( null, true, IResource.DEPTH_ZERO );
            for (IMarker marker : newMarkers) {
                Image image = null;
                switch (marker.getAttribute( IMarker.SEVERITY, IMarker.SEVERITY_WARNING )) {
                    case IMarker.SEVERITY_ERROR: 
                        image = ERROR; break;
                    case IMarker.SEVERITY_WARNING: 
                        image = WARN; break;
                    case IMarker.SEVERITY_INFO: 
                        image = INFO; break;
                    default:
                        image = INFO; break;
                }
                editor.lineMarkers().put( new LineMarker( String.valueOf( marker.getId() ) )
                        .setLine( marker.getAttribute( IMarker.LINE_NUMBER, 0 ) )
                        .setCharPos( marker.getAttribute( IMarker.CHAR_START, 0 ), marker.getAttribute( IMarker.CHAR_END, 0 ) )
                        .setText( marker.getAttribute( IMarker.MESSAGE, "" ) )
                        .setIcon( image ));
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
            // XXX dispose images
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
        
        // editor property changes
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
                    updateDirtyState( true );
                }
                else if (ev.getPropertyName().equals( CodeMirror.PROP_SAVE )) {
                    doSave( null );
                }
            }
        });

        // init completion system
        initCompletionProviders();
        completionJob = new CompletionJob( this );
        addPropertyChangeListener( completionJob );

        doLoad( new NullProgressMonitor() );
    }


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

    /**
     * Update the {@link #isDirty()} state and fires property event if necessary.
     * 
     * @param dirty The new dirty state
     */
    protected void updateDirtyState( boolean dirty ) {
        if (isDirty != dirty) {
            isDirty = dirty;
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    firePropertyChange( PROP_DIRTY );
                }
            });
        }
    }
    
    
    public void doSave( IProgressMonitor monitor ) {
        try {
            IFileEditorInput input = getEditorInput();
            InputStream in = new ByteArrayInputStream( editor.getText().getBytes( "UTF-8" ) );
            input.getFile().setContents( in, 0, monitor );
            updateDirtyState( false );
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
            IOUtils.copy( in, out );
            editor.setText( out.toString( "UTF-8" ) );
            updateDirtyState( false );

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

    
    /**
     * Finds the selections at the given text position. The gathered elements
     * are fired as a {@link SelectionChangedEvent}.
     * <p/>
     * Sub classes may override and add their specific selection elements.
     *
     * @return List of elements of a {@link SelectionChangedEvent}.
     */
    protected List gatherSelections( int start, int end )
    throws Exception {
        List elms = new ArrayList();
        // find markers at position
        for (IMarker marker : markers) {
            int markerStart = marker.getAttribute( IMarker.CHAR_START, 0 );
            int markerEnd = marker.getAttribute( IMarker.CHAR_END, 0 );
            if (start >= markerStart && end <= markerEnd) {
                elms.add( marker );
            }
        }
        return elms;
    }


    /**
     * Finds selection elements via {@link #gatherSelections(int, int)} and fires a
     * {@link SelectionChangedEvent} to the {@link #selectionListeners}.
     * 
     * @param start Start position of the text selection.
     * @param end End position of the text selection.
     */
    protected void fireSelectionChanged( int start, int end ) {
        try {
            // gather selections
            List elms = gatherSelections( start, end );
            
            // fire event
            if (!elms.isEmpty()) {
                StructuredSelection sel = new StructuredSelection( elms );
                SelectionChangedEvent ev = new SelectionChangedEvent( this, sel );
                for (ISelectionChangedListener l : selectionListeners) {
                    l.selectionChanged( ev );
                }
            }
        }
        catch (Exception e) {
            // just log, no UI message
            log.warn( e.getLocalizedMessage(), e );
        }
    }

    
    /**
     * Add listener to the underlying {@link CodeMirror} editor. The source
     * of the events is the {@link CodeMirror} instance.
     * 
     * @param l Listener to add.
     */
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        editor.addPropertyChangeListener( l );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        editor.removePropertyCHangeListener( l );
    }

    
    // completions ****************************************
    
    /**
     * Find extensions.
     */
    protected void initCompletionProviders() {
    }
    
    public void addCompletionProvider( ICompletionProvider provider ) {
        completionProviders.add( provider );    
    }
    
}
