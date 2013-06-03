/* 
 * polymap.org
 * Copyright 2010-2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.internal.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldDecorator;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.filter.FilterEditor;
import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * The filter form specific parent Composite of a form field, consisting of an
 * {@link IFormField}, an {@link IFormFieldLabel} and an
 * {@link IFormFieldDecorator}. The FilterFieldComposite provides them a context
 * via the {@link IFormFieldSite}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FilterFieldComposite
        implements IFormFieldSite {

    private static Log log = LogFactory.getLog( FilterFieldComposite.class );

    private FilterEditor            editor;
    
    private String                  propName;
    
    private Class                   propType;
    
    private IFormEditorToolkit      toolkit;
    
    private IFormField              field;
    
    private IFormFieldDecorator     decorator;
    
    private IFormFieldLabel         labeler;
    
    private IFormFieldValidator     validator;
    
    private boolean                 isDirty = false;
    
    /** The current error, externally set or returned by the validator. */
    private String                  errorMsg;
    
    /** Error message set by {@link #setErrorMessage(String)} */
    private String                  externalErrorMsg;

    private Object                  value;


    public FilterFieldComposite( FilterEditor editor, IFormEditorToolkit toolkit,
            String propName, Class propType,
            IFormField field, IFormFieldLabel labeler, IFormFieldDecorator decorator,
            IFormFieldValidator validator ) {
        this.editor = editor;
        this.propName = propName;
        this.propType = propType;
        this.toolkit = toolkit;
        this.field = field;
        this.labeler = labeler;
        this.decorator = decorator;
        this.validator = validator;
    }
    
    
    public Composite createComposite( Composite parent, int style ) {
        Composite result = toolkit.createComposite( parent, style );
        result.setLayout( new FormLayout() );
        
        // label
        labeler.init( this );
        Control labelControl = labeler.createControl( result, toolkit );
        FormData layoutData = new FormData( 90, SWT.DEFAULT );
        layoutData.left = new FormAttachment( 0 );
        layoutData.top = new FormAttachment( 0, 4 );
        labelControl.setLayoutData( layoutData );
        
        // decorator
        decorator.init( this );
        Control decoControl = decorator.createControl( result, toolkit );
        layoutData = new FormData( 30, SWT.DEFAULT );
        layoutData.left = new FormAttachment( 100, -22 );
        layoutData.right = new FormAttachment( 100 );
        layoutData.top = new FormAttachment( 0, 0 );
        decoControl.setLayoutData( layoutData );
        
        // field
        field.init( this );
        Control fieldControl = field.createControl( result, toolkit );
        layoutData = fieldControl.getLayoutData() != null
                ? (FormData)fieldControl.getLayoutData()
                : new FormData( 100, SWT.DEFAULT );
        layoutData.left = new FormAttachment( labelControl, 5 );
        layoutData.right = new FormAttachment( decoControl, -3 );
        fieldControl.setLayoutData( layoutData );
        
        result.pack( true );
        return result;
    }


    public void dispose() {
        log.debug( "dispose(): ..." );
        if (field != null) {
            field.dispose();
            field = null;
        }
        if (labeler != null) {
            labeler.dispose();
            labeler = null;
        }
        if (decorator != null) {
            decorator.dispose();
            decorator = null;
        }
    }


    public IFormField getFormField() {
        return field;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean isValid() {
        return errorMsg == null;       
    }


    /**
     * Returns the raw value of this field. The value is not transformed to be a
     * 'field value'.
     */
    public Object getValue() {
        return value;        
    }

    // IFormFieldSite *************************************
        
    public String getFieldName() {
        return propName;
    }

    public Class getFieldType() {
        return propType;
    }

    public Object getFieldValue()
    throws Exception {
        return validator.transform2Field( value );
    }

    public void setFieldValue( Object value )
    throws Exception {
        this.value = validator.transform2Model( value );
    }

    public void loadDefaultValue( Object defaultValue ) 
    throws Exception {
        this.value = defaultValue;
        field.load();
    }


    public IFormEditorToolkit getToolkit() {
        return toolkit;
    }

    public void addChangeListener( IFormFieldListener l ) {
        EventManager.instance().subscribe( l, new EventFilter<FormFieldEvent>() {
            public boolean apply( FormFieldEvent ev ) {
                return ev.getFormField() == field;
            }
        });
    }
    
    public void removeChangeListener( IFormFieldListener l ) {
        EventManager.instance().unsubscribe( l );
    }
    
    public void fireEvent( Object source, int eventCode, Object newValue ) {
        Object validatedNewValue = null;

        errorMsg = externalErrorMsg;
        
        // check isDirty / isValid
        if (eventCode == IFormFieldListener.VALUE_CHANGE && errorMsg == null) {
            if (validator != null) {
                errorMsg = validator.validate( newValue );
            }
            if (errorMsg == null) {
                if (value == null && newValue == null) {
                    isDirty = false;
                }
                else {
                    isDirty = value == null && newValue != null ||
                    value != null && newValue == null ||
                    !value.equals( newValue );
                }
                try {
                    validatedNewValue = validator.transform2Model( newValue );
                }
                catch (Exception e) {
                    // XXX hmmm... what to do?
                    throw new RuntimeException( e );
                }
            }
        }
        // propagate;
        // syncPublish() helps to avoid to much UICallbacks browser which slows
        // down form performance of text fields in particular
        FormFieldEvent ev = new FormFieldEvent( editor, source, propName, field, eventCode, null, validatedNewValue );
        EventManager.instance().syncPublish( ev );
    }

    public String getErrorMessage() {
        return errorMsg;
    }

    public void setErrorMessage( String errorMsg ) {
        this.externalErrorMsg = errorMsg;
    }

}
