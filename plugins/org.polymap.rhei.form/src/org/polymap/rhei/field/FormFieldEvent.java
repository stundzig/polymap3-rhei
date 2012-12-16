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
package org.polymap.rhei.field;

import java.util.EventObject;

import org.polymap.rhei.filter.FilterEditor;

/**
 * Default event implementation. 
 *
 * @see IFormFieldListener
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FormFieldEvent
        extends EventObject {

    private Object      editor;
    
    private IFormField  formField;
    
    private int         eventCode;
    
    private Object      oldValue;
    
    private Object      newValue;

    private String      fieldName;
    

    public FormFieldEvent( Object editor, Object source, String fieldName, IFormField field, int eventCode, Object oldValue, Object newValue ) {
        super( source );
        this.editor = editor;
        this.formField = field;
        this.fieldName = fieldName;
        this.eventCode = eventCode;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    /**
     * The editor that this event belongs to.
     *
     * @return {@link org.polymap.rhei.form.FormEditor} or {@link FilterEditor}
     */
    public Object getEditor() {
        return editor;
    }
    
    public String getFieldName() {
        return fieldName;
    }

    public IFormField getFormField() {
        return formField;
    }

    public int getEventCode() {
        return eventCode;
    }

    public <T> T getOldValue() {
        return (T)oldValue;
    }

    public <T> T getNewValue() {
        return (T)newValue;
    }

    public String toString() {
        return "FormFieldEvent [source=" + source + ", eventCode=" + eventCode + ", formField=" + formField
                + ", newValue=" + newValue + ", oldValue=" + oldValue + "]";
    }

}
