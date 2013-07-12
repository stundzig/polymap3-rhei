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
 */
package org.polymap.rhei.internal.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.RheiFormPlugin;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NullValidator;
import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.DefaultFormFieldDecorator;
import org.polymap.rhei.internal.DefaultFormFieldLabeler;

/**
 *
 * @deprecated This is the first version of a Form Page container. It is used in
 *             {@link FormEditor} only. Another version exists as
 *             {@link AbstractFormEditorPageContainer}, which is used in
 *             {@link FormDialog}. Should be refactored into one code base.
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FormEditorPageContainer
        extends FormPage
        implements IFormEditorPageSite {

    static Log log = LogFactory.getLog( FormEditorPageContainer.class );

    private IFormEditorPage             page;

    private IManagedForm                form;

    private IFormEditorToolkit          toolkit;

    private List<FormFieldComposite>    fields = new ArrayList( 32 );

    private volatile boolean            blockEvents;


    public FormEditorPageContainer( IFormEditorPage page, FormEditor editor, String id, String title ) {
        super( editor, id, title );
        this.page = page;
    }


    public synchronized void dispose() {
        if (page != null && page instanceof IFormEditorPage2) {
            ((IFormEditorPage2)page).dispose();
        }
        for (FormFieldComposite field : fields) {
            field.dispose();
        }
        fields.clear();
    }


    public void addFieldListener( IFormFieldListener l ) {
        EventManager.instance().subscribe( l, new EventFilter<FormFieldEvent>() {
            public boolean apply( FormFieldEvent ev ) {
                return !blockEvents && ev.getEditor() == getEditor();
            }
        });
    }

    public void removeFieldListener( IFormFieldListener l ) {
        EventManager.instance().unsubscribe( l );
    }

    /*
     * Called from page provider client code.
     */
    public void fireEvent( Object source, String fieldName, int eventCode, Object newValue ) {
        if (!blockEvents) {
            FormFieldEvent ev = new FormFieldEvent( getEditor(), source, fieldName, null, eventCode, null, newValue );
            EventManager.instance().publish( ev );
        }
    }


    @Override
    public boolean isDirty() {
        if (page instanceof IFormEditorPage2) {
            if (((IFormEditorPage2)page).isDirty()) {
                return true;
            }
        }
        for (FormFieldComposite field : fields) {
            if (field.isDirty()) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean isValid() {
        if (page instanceof IFormEditorPage2) {
            if (!((IFormEditorPage2)page).isValid()) {
                return false;
            }
        }
        for (FormFieldComposite field : fields) {
            if (!field.isValid()) {
                return false;
            }
        }
        return true;
    }


    public Map<Property,Object> doSubmit( IProgressMonitor monitor )
    throws Exception {
        super.doSave( monitor );

        Map<Property,Object> result = new HashMap();

        for (FormFieldComposite field : fields) {
            if (field.isDirty()) {
                Object newValue = field.store();
                Object old = result.put( field.getProperty(), newValue );
                if (old != null) {
                    throw new RuntimeException( "Submitted value already exists for property: " + field.getProperty() );
                }
            }
        }

        // after form fields in order to allow subclassed Property instances
        // to be notified of submit
        if (page instanceof IFormEditorPage2) {
            ((IFormEditorPage2)page).doSubmit( monitor );
        }

        return result;
    }


    public void doLoad( IProgressMonitor monitor )
    throws Exception {
        if (page instanceof IFormEditorPage2) {
            ((IFormEditorPage2)page).doLoad( monitor );
        }

        try {
            // do not dispatch events while loading
            blockEvents = true;

            for (FormFieldComposite field : fields) {
                field.load();
            }
        }
        finally {
            blockEvents = false;
        }
    }


    protected void createFormContent( IManagedForm managedForm ) {
        form = managedForm;
        toolkit = new FormEditorToolkit( form.getToolkit() );
        
        try {
            // ask the delegate to create content
            page.createFormContent( this );
            doLoad( new NullProgressMonitor() );
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( RheiFormPlugin.PLUGIN_ID, this, "An error occured while creating the new page.", e );
        }

        // XXX hack: help the ScrolledCompositeLayout to correctly display
        // the vertical scrollbar on init
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                Rectangle clientSize = form.getForm().getClientArea();
                ScrollBar vbar = form.getForm().getVerticalBar();
                vbar.setVisible( true );
                
                Point formSize = form.getForm().computeSize( clientSize.width-vbar.getSize().x, SWT.DEFAULT );
                
                form.getForm().setMinHeight( clientSize.height );
                form.getForm().getContent().setBounds( 0, 0, formSize.x, formSize.y );
            }
        });
        
        // form.getToolkit().decorateFormHeading( form.getForm().getForm() );

        // add page editor actions
        Action[] pageActions = page.getEditorActions();
        if (pageActions != null && pageActions.length > 0) {
            form.getForm().getToolBarManager().add( new GroupMarker( "__pageActions__" ) );

            for (Action action : pageActions) {
                form.getForm().getToolBarManager().appendToGroup( "__pageActions__", action );
            }
            form.getForm().getToolBarManager().appendToGroup( "__pageActions__", new Separator() );
        }

        // add standard page actions/items
        IToolBarManager manager = form.getForm().getToolBarManager();
        manager.add( new GroupMarker( "__standardPageActions__" ) );
        for (Object item : ((FormEditor)getEditor()).standardPageActions) {
            if (item instanceof IAction) {
                form.getForm().getToolBarManager().appendToGroup( "__standardPageActions__", (IAction)item );
            }
            else if (item instanceof IContributionItem) {
                form.getForm().getToolBarManager().appendToGroup( "__standardPageActions__", (IContributionItem)item );
            }
            else {
                throw new RuntimeException( "Unknown FormEditor action type: " + item.getClass() );
            }
        }
        form.getForm().getToolBarManager().update( true );
    }


    // IFormEditorPageSite ****************************

    public Composite getPageBody() {
        return form.getForm().getBody();
    }

    public IFormEditorToolkit getToolkit() {
        return toolkit;
    }

    @Override
    public void setFormTitle( String title ) {
        form.getForm().setText( title );
    }

    @Override
    public void setEditorTitle( String title ) {
        ((FormEditor)getEditor()).setPartName( title );
    }

    public void setActivePage( String pageId ) {
        ((FormEditor)getEditor()).setActivePage( pageId );
    }

    public Composite newFormField( Composite parent, Property prop, IFormField field, IFormFieldValidator validator ) {
        return newFormField( parent, prop, field, validator, null );
    }

    public Composite newFormField( Composite parent, Property prop, IFormField field, IFormFieldValidator validator, String label ) {
        FormFieldComposite result = new FormFieldComposite( getEditor(), getToolkit(), prop, field,
                new DefaultFormFieldLabeler( label ), new DefaultFormFieldDecorator(),
                validator != null ? validator : new NullValidator() );
        fields.add( result );

        return result.createComposite( parent != null ? parent : getPageBody(), SWT.NONE );
    }

    public void setFieldValue( String fieldName, Object value ) {
        for (FormFieldComposite field : fields) {
            if (field.getFieldName().equals( fieldName )) {
                field.setFormFieldValue( value );
                return;
            }
        }
        throw new RuntimeException( "No such field: " + fieldName );
    }

    public void setFieldEnabled( String fieldName, boolean enabled ) {
        for (FormFieldComposite field : fields) {
            if (field.getFieldName().equals( fieldName )) {
                field.setEnabled( enabled );
                return;
            }
        }
        throw new RuntimeException( "No such field: " + fieldName );
    }

    public void reloadEditor()
    throws Exception {
        ((FormEditor)getEditor()).doLoad( new NullProgressMonitor() );
    }

    public void submitEditor()
    throws Exception {
        ((FormEditor)getEditor()).doSave( new NullProgressMonitor() );
    }
    
    public void clearFields() {
        dispose();
        
        // dispose any left sections and stuff
        for (Control child : getPageBody().getChildren()) {
            if (!child.isDisposed()) {
                child.dispose();
            }
        }
    }

}