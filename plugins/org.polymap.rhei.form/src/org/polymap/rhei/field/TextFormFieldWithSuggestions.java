/*
 * polymap.org Copyright 2010-2013, Falko Bräutigam. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.rhei.field;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.polymap.rhei.form.IFormEditorToolkit;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class TextFormFieldWithSuggestions
        extends TextFormField {

    private final Set<String> suggestions;


    public TextFormFieldWithSuggestions( Set<String> suggestions, Style... styles ) {
        super( styles );
        this.suggestions = suggestions;
    }


    public void init( IFormFieldSite _site ) {
        super.init( _site );
    }


    protected Control createControl( Composite parent, IFormEditorToolkit toolkit, int style ) {

        Composite area = toolkit.createComposite( parent, SWT.NONE );
        area.setLayout( new FormLayout() );

        final Text text = (Text)super.createControl( area, toolkit, style );
        FormData data = (FormData)text.getLayoutData();
        data.left = new FormAttachment( 0 );
        data.right = new FormAttachment( 100 );
        data.bottom = new FormAttachment( 77 ); // %
        text.setLayoutData( data );

        final Combo combo = toolkit.createCombo( area, suggestions, SWT.SINGLE );
        data = new FormData();
        data.left = new FormAttachment( 0 );
        data.right = new FormAttachment( 35 );
        data.top = new FormAttachment( text, 3 ); // 3 offset to the top element

        combo.setLayoutData( data );

        combo.addSelectionListener( new SelectionListener() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                String suggest = combo.getText();
                if (suggest != null) {
                    String currentText = text.getText();
                    if (currentText != null && !currentText.isEmpty()) {
                        text.setText( currentText + "\n" + suggest );
                    } else {
                        text.setText( suggest );
                    }
                }
            }


            @Override
            public void widgetDefaultSelected( SelectionEvent e ) {
                // nothing todo
            }
        } );
        // area.pack();
        return area;
    }

}
