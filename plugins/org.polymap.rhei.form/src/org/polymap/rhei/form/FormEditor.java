/*
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
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
 *
 * $Id: $
 */
package org.polymap.rhei.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.io.IOException;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Id;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.FeatureChangeEvent;
import org.polymap.core.data.operations.ModifyFeaturesOperation;
import org.polymap.core.data.operations.ZoomFeatureBoundsOperation;
import org.polymap.core.operation.IOperationSaveListener;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.qi4j.event.PropertyChangeSupport;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.Messages;
import org.polymap.rhei.RheiFormPlugin;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.internal.form.FeatureOperationsItem;
import org.polymap.rhei.internal.form.FormEditorPageContainer;
import org.polymap.rhei.internal.form.FormPageProviderExtension;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("deprecation")
public class FormEditor
        extends org.eclipse.ui.forms.editor.FormEditor {

    private static Log log = LogFactory.getLog( FormEditor.class );

    public static final String          ID = "org.polymap.rhei.form.FormEditor";


    /**
     *
     * @param fs
     * @param feature
     * @param layer Hint this editor about the layer of the edited feature. Might be null.
     * @param activate XXX
     * @return The editor of the given feature, or null.
     */
    public static FormEditor open( FeatureStore fs, Feature feature, ILayer layer, boolean activate ) {
        try {
            log.debug( "open(): feature= " + feature );
            FormEditorInput input = new FormEditorInput( fs, feature );

            // check current editors
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorReference[] editors = page.getEditorReferences();
            for (IEditorReference reference : editors) {
                IEditorInput cursor = reference.getEditorInput();
                if (cursor instanceof FormEditorInput) {
                    log.debug( "        editor: feature= " + ((FormEditorInput)cursor).getFeature().getIdentifier().getID() );
                }
                if (cursor.equals( input )) {
                    Object previous = page.getActiveEditor();
                    if (activate) {
                        page.activate( reference.getPart( true ) );
                    }
                    return (FormEditor)reference.getEditor( false );
                }
            }

            // not found -> open new editor
            IEditorPart part = page.openEditor( input, input.getEditorId(), activate, IWorkbenchPage.MATCH_NONE );
            log.debug( "editor= " + part );
            // can also be ErrorEditorPart
            return part instanceof FormEditor ? (FormEditor)part : null;
        }
        catch (PartInitException e) {
            PolymapWorkbench.handleError( RheiFormPlugin.PLUGIN_ID, null, e.getMessage(), e );
            return null;
        }
    }


    // instance *******************************************

    private List<FormEditorPageContainer> pages = new ArrayList();

    /**
     * Elements of type {@link IAction} or {@link IContributionItem}.
     * <p/>
     * XXX visibility
     */
    public List                         standardPageActions = new ArrayList();

    private boolean                     isDirty;

    private boolean                     isValid;

    private Action                      submitAction;

    private Action                      revertAction;

    private OperationSaveListener       operationSaveListener;

    private IFormFieldListener          fieldListener;
    

    public FormEditor() {
    }


    public void init( IEditorSite site, IEditorInput input )
            throws PartInitException {
        super.init( site, input );
    }

    protected Composite createPageContainer( Composite parent ) {
        final ILayer layer = ((FormEditorInput)getEditorInput()).getLayer();

        if (layer != null) {
            // feature operations menu
            standardPageActions.add( new FeatureOperationsItem( layer, getFeature(), getFeatureStore() ) );
            
            // zoom feature action
            Action zoomAction = new Action( Messages.get( "FormEditor_zoom" ) ) {
                public void run() {
                    try {
                        IMap map = layer.getMap();
                        CoordinateReferenceSystem crs = map.getCRS();
                        FeatureCollection features = FeatureCollections.newCollection();
                        features.add( getFeature() );
                        ZoomFeatureBoundsOperation op = new ZoomFeatureBoundsOperation( features, map, crs );

                        OperationSupport.instance().execute( op, true, true );
                    }
                    catch (Exception e) {
                        PolymapWorkbench.handleError( RheiFormPlugin.PLUGIN_ID, this, "", e );
                    }
                }
            };
            zoomAction.setImageDescriptor( ImageDescriptor.createFromURL(
                    RheiFormPlugin.getDefault().getBundle().getResource( "icons/elcl16/zoom_selection_co.gif" ) ) );
            zoomAction.setToolTipText( Messages.get( "FormEditor_zoomTip" ) );
            zoomAction.setEnabled( true );
            standardPageActions.add( zoomAction );
    
            // FormEditor listeners
            EventManager.instance().subscribe( this, new EventFilter() {
                public boolean apply( Object ev ) {
                    // PropertyChangeEvent
                    if (ev instanceof PropertyChangeEvent) {
                        PropertyChangeEvent pev = (PropertyChangeEvent)ev;
                        if (PropertyChangeSupport.PROP_ENTITY_REMOVED.equals( pev.getPropertyName() )) {
                            try {
                                layer.id();
                                return false;
                            }
                            catch (NoSuchEntityException e) {
                                return true;
                            }
                        }
                    }
                    // FeatureChangeEvent
                    else if (ev instanceof FeatureChangeEvent) {
                        FeatureChangeEvent fev = (FeatureChangeEvent)ev;
                        return layer.equals( fev.getSource() );
                    }
                    return false;
                }
            });
        }

        // submit action
        submitAction = new Action( Messages.get( "FormEditor_submit" ) ) {
            public void run() {
                try {
                    log.debug( "submitAction.run(): ..." );
                    doSave( new NullProgressMonitor() );
                    
                    //OperationSupport.instance().saveChanges();
                }
//                catch (ConcurrentModificationException e) {
//                    PolymapWorkbench.handleError( RheiFormPlugin.PLUGIN_ID, this,
//                            "Daten wurden von einem anderen Nutzer geändert.\nKlicken Sie auf \"Daten von anderem Nutzer übernehmen\" und öffnen Sie den Datensatz erneut.\nACHTUNG: Wenn Sie den Datensatz nicht erneut öffnen, dann können Daten verloren gehen.", e );
//                }
                catch (Exception e) {
                    PolymapWorkbench.handleError( RheiFormPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
                }
            }
        };
        submitAction.setImageDescriptor( ImageDescriptor.createFromURL(
                RheiFormPlugin.getDefault().getBundle().getResource( "icons/etool16/validate.gif" ) ) );
        submitAction.setToolTipText( Messages.get( "FormEditor_submitTip" ) );
        submitAction.setEnabled( false );
        standardPageActions.add( submitAction );

        // revert action
        revertAction = new Action( Messages.get( "FormEditor_revert" ) ) {
            public void run() {
                log.debug( "revertAction.run(): ..." );
                doLoad( new NullProgressMonitor() );
            }
        };
        revertAction.setImageDescriptor( ImageDescriptor.createFromURL(
                RheiFormPlugin.getDefault().getBundle().getResource( "icons/etool16/revert.gif" ) ) );
        revertAction.setToolTipText( Messages.get( "FormEditor_revertTip" ) );
        revertAction.setEnabled( false );
        standardPageActions.add( revertAction );
        
        // save listener 
        operationSaveListener = new OperationSaveListener();
        OperationSupport.instance().prependOperationSaveListener( operationSaveListener );

        // field change listener
        EventManager.instance().subscribe( fieldListener = 
                new IFormFieldListener() {
                    public void fieldChange( FormFieldEvent ev ) {
                        FormEditor.this.fieldChange( ev );
                    }
                },
                new EventFilter<FormFieldEvent>() {
                    public boolean apply( FormFieldEvent ev ) {
                        return ev.getEditor() == FormEditor.this;
                    }
                });
        
        return super.createPageContainer( parent );
    }


    @EventHandler(display=true)
    protected void layerChanged( PropertyChangeEvent ev ) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        page.closeEditor( FormEditor.this, false );
    }

    
    @EventHandler(display=true)
    protected void featureChanged( FeatureChangeEvent ev ) throws IOException {
        if (ev.getType() == FeatureChangeEvent.Type.FLUSHED
                // XXX how to check if we are not the source of the event? 
                /*|| ev.getType() == FeatureChangeEvent.Type.MODIFIED*/) {
            
            if (ev.getFids().contains( getFeature().getIdentifier() )
                    // EntitySourceProcessor sends empty FLUSH events
                    || ev.getFids().isEmpty() ) {
                
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                page.closeEditor( FormEditor.this, false );

                // reload feature and reset input
                Id filter = DataPlugin.ff.id( Collections.singleton( getFeature().getIdentifier() ) );
                final FeatureStore fs = getFeatureStore();

                // reset input and reload editor
                fs.getFeatures( filter ).accepts( new FeatureVisitor() {
                    public void visit( Feature feature ) {
                        open( fs, feature, null, false ); 
                    }
                }, null );
            }
        }
        else if (ev.getType() == FeatureChangeEvent.Type.REMOVED) {
            if (ev.getFids().contains( getFeature().getIdentifier() )) {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                page.closeEditor( FormEditor.this, false );                
            }
        }
        // XXX reload when modified? (check event source != this)
    }


    /*
     * 
     */
    class OperationSaveListener
            implements IOperationSaveListener {
        
        public void prepareSave( OperationSupport os, final IProgressMonitor monitor )
        throws Exception {
            if (isDirty()) {
                Polymap.getSessionDisplay().syncExec( new Runnable() {
                    public void run() {
                        if (MessageDialog.openQuestion( PolymapWorkbench.getShellToParentOn(),
                                getPartName(), Messages.get( "FormEditor_prepareSave", getPartName() ) )) {
                            doSave( monitor, false );
                        }
                    }
                });
            }
        }
    
        public void save( OperationSupport os, IProgressMonitor monitor ) {
        }
        
        public void rollback( OperationSupport os, IProgressMonitor monitor ) {
        }
    
        public void revert( OperationSupport os, final IProgressMonitor monitor ) {
            if (isDirty()) {
                Polymap.getSessionDisplay().syncExec( new Runnable() {
                    public void run() {
//                if (MessageDialog.openQuestion( PolymapWorkbench.getShellToParentOn(),
//                        getPartName(), Messages.get( "FormEditor_prepareRevert", getPartName() ) )) {
                        doLoad( monitor );
//                }
                    }
                });
            }
        }
    }


    /**
     * 
     */
    protected void fieldChange( FormFieldEvent ev ) {
        // update isDirty / isValid
        boolean oldIsDirty = isDirty;
        isDirty = false;
        for (FormEditorPageContainer page : pages) {
            if (page.isDirty()) {
                isDirty = true;
                break;
            }
        }
        isValid = true;
        for (FormEditorPageContainer page : pages) {
            if (!page.isValid()) {
                isValid = false;
                break;
            }
        }

        log.debug( "fieldChange(): dirty=" + isDirty + ", isValid=" + isValid );
        submitAction.setEnabled( isDirty && isValid );
        revertAction.setEnabled( isDirty );

        if (oldIsDirty != isDirty) {
            editorDirtyStateChanged();
        }
    }

    
    public void dispose() {
        EventManager.instance().unsubscribe( fieldListener );
        
        // this also disposes my pages
        super.dispose();
        pages.clear();
        OperationSupport.instance().removeOperationSaveListener( operationSaveListener );
        
        ILayer layer = ((FormEditorInput)getEditorInput()).getLayer();
        if (layer != null) {
            try {
                layer.removePropertyChangeListener( this );
            }
            catch (NoSuchEntityException e) {
                // layer has been deleted -> ignore
            }
        }
    }


    public Composite getContainer() {
        return super.getContainer();
    }


    /**
     * This called by the Workbench so that we can add our pages. The implementation
     * searches for extensions that provide a {@link IFormPageProvider}. All providers
     * are called.
     */
    protected void addPages() {
        List<IFormEditorPage> _pages = new ArrayList();
        
        // get all pages
        for (FormPageProviderExtension ext : FormPageProviderExtension.allExtensions()) {
            try {
                // ignore standard editors on first iteration
                if (!ext.isStandard()) {
                    IFormPageProvider provider = ext.newPageProvider();
                    _pages.addAll( provider.addPages( this, getFeature() ) );
                }
            }
            catch (CoreException e) {
                log.warn( "Exception while initializing pages of FormEditor.", e );
            }
        }
        
        if (_pages.isEmpty()) {
            // add standard provider only, if no special providers could be found
            for (FormPageProviderExtension ext : FormPageProviderExtension.allExtensions()) {
                try {
                    if (ext.isStandard()) {
                        IFormPageProvider provider = ext.newPageProvider();
                        _pages.addAll( provider.addPages( this, getFeature() ) );
                    }    
                }
                catch (CoreException e) {
                    log.warn( "Exception while initializing pages of FormEditor.", e );
                }
            }
        }

        // sort
        Collections.sort( _pages, new Comparator<IFormEditorPage>() {
            public int compare( IFormEditorPage p1, IFormEditorPage p2 ) {
                return -(p1.getPriority() - p2.getPriority());
            }
        });
        
        // add pages
        for (IFormEditorPage page : _pages) {
            try {
                FormEditorPageContainer wrapper = new FormEditorPageContainer( page, this, page.getId(), page.getTitle() );
                addPage( wrapper );
                pages.add( wrapper );

                if (getActivePage() == -1) {
                    setActivePage( page.getId() );
                    //setPartName( page.getTitle() );
                }
            }
            catch (CoreException e) {
                log.warn( "Exception while initializing pages of FormEditor.", e );
            }
        }
    }


    public void setPartName( String name ) {
        super.setPartName( name );
    }


    public Feature getFeature() {
        return ((FormEditorInput)getEditorInput()).getFeature();
    }

    public FeatureStore getFeatureStore() {
        return ((FormEditorInput)getEditorInput()).getFeatureStore();
    }


    public boolean isDirty() {
        return isDirty;
    }

    
    public void doSave( IProgressMonitor monitor ) {
        doSave( monitor, true );    
    }

    
    protected void doSave( IProgressMonitor monitor, boolean runOperation ) {
        log.debug( "doSave(): ..." );

        Map<Property,Object> changes = new HashMap();

        try {
            // submit all pages and get their changes
            for (FormEditorPageContainer page : pages) {
                changes.putAll( page.doSubmit( monitor ) );
            }

            // find featury properties
            List<AttributeDescriptor> attrs = new ArrayList();
            List values = new ArrayList();
            for (Property prop : changes.keySet()) {
                AttributeDescriptor descriptor = (AttributeDescriptor)prop.getDescriptor();
                // this check allows to subclassed Properties to work with
                // complex attribute types; saving has to be done elsewhere
                if (descriptor != null) {
                    attrs.add( descriptor );
                    values.add( changes.get( prop ) );
                }
                else {
                    log.debug( "Property has no descriptor -> ommitting!" );
                }
            }

            // execute operation
            Id filter = DataPlugin.ff.id( Collections.singleton( getFeature().getIdentifier() ) );

            ModifyFeaturesOperation op = new ModifyFeaturesOperation(
                    ((FormEditorInput)getEditorInput()).getLayer(),
                    getFeatureStore(),
                    filter,
                    attrs.toArray( new AttributeDescriptor[attrs.size()]),
                    values.toArray() );
            if (runOperation) {
                OperationSupport.instance().execute( op, false, false );
            }
            else {
                op.execute( monitor, null );
            }

            // update isDirt/isValid
            fieldChange( null );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiFormPlugin.PLUGIN_ID, this, "Objekt konnte nicht gespeichert werden.", e );
        }
    }


    public void doLoad( IProgressMonitor monitor ) {
        log.debug( "doLoad(): ..." );
        try {
            for (FormEditorPageContainer page : pages) {
                page.doLoad( monitor );
            }
            // update isDirt/isValid
            fieldChange( null );
        }
        catch (Exception e) {
            log.warn( "", e );
            PolymapWorkbench.handleError( RheiFormPlugin.PLUGIN_ID, this, "Objekt konnte nicht korrekt geladen werden.", e );
        }
    }


    public void doSaveAs() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public boolean isSaveAsAllowed() {
        return false;
    }


    public void setFocus() {
    }

}
