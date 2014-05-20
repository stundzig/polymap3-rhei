/* 
 * polymap.org
 * Copyright (C) 2010-2015, Falko Bräutigam. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CheckboxFormField
        implements IFormField {

    private static Log log = LogFactory.getLog( CheckboxFormField.class );

    private IFormFieldSite      site;
    
    private Button              checkbox;
    
    // XXX use (proper) validator to make the translation to String 
    private Object              loadedValue;


    @Override
    public void init( IFormFieldSite _site ) {
        this.site = _site;
    }

    @Override
    public void dispose() {
        checkbox.dispose();
    }

    @Override
    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        checkbox = toolkit.createButton( parent, "", SWT.CHECK );

        // modify listener
        checkbox.addSelectionListener( new SelectionListener() {
            public void widgetSelected( SelectionEvent e ) {
                log.debug( "modifyEvent(): test= " + checkbox.getSelection() );
                Boolean newValue = loadedValue == null && !checkbox.getSelection() ? null : checkbox.getSelection();
                site.fireEvent( this, IFormFieldListener.VALUE_CHANGE, newValue );
            }
            public void widgetDefaultSelected( SelectionEvent e ) {
            }
        });
        // focus listener
        checkbox.addFocusListener( new FocusListener() {
            public void focusLost( FocusEvent event ) {
                checkbox.setBackground( FormEditorToolkit.textBackground );
                site.fireEvent( this, IFormFieldListener.FOCUS_LOST, checkbox.getText() );
            }
            public void focusGained( FocusEvent event ) {
                checkbox.setBackground( FormEditorToolkit.textBackgroundFocused );
                site.fireEvent( this, IFormFieldListener.FOCUS_GAINED, checkbox.getText() );
            }
        });
        return checkbox;
    }

    @Override
    public IFormField setEnabled( boolean enabled ) {
        checkbox.setEnabled( enabled );
        return this;
    }
    
    @Override
    public IFormField setValue( Object value ) {
        checkbox.setSelection( ((Boolean)value).booleanValue() );
        return this;
    }

    @Override
    public void load() throws Exception {
        assert checkbox != null : "Control is null, call createControl() first.";
        
        boolean prevValue = checkbox.getSelection();

        loadedValue = site.getFieldValue();
        checkbox.setSelection( loadedValue != null && ((Boolean)loadedValue).booleanValue() );

        // CheckBox does not trigger selection listener on setSelection()
        if (prevValue != checkbox.getSelection()) {
            Boolean newValue = loadedValue == null && !checkbox.getSelection() ? null : checkbox.getSelection();
            site.fireEvent( this, IFormFieldListener.VALUE_CHANGE, newValue );
        }
    }

    @Override
    public void store() throws Exception {
        site.setFieldValue( checkbox.getSelection() );
    }

}
