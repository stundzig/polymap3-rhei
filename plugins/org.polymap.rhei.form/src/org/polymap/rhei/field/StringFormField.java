/*
 * polymap.org
 * Copyright 2010-2013, Falko Bräutigam. All rights reserved.
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

import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.RheiFormPlugin;
import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * A form field using a {@link Text} widget.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StringFormField
        implements IFormField {

    private static Log log = LogFactory.getLog( StringFormField.class );

    public static final Pattern URL_PATTERN = Pattern.compile( "^((https?|ftp)://|(www|ftp)\\.)[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$" );
    
    /**
     * Possible styles of a {@link StringFormField}
     */
    public enum Style {
        ALIGN_LEFT      ( SWT.LEFT ),
        ALIGN_CENTER    ( SWT.CENTER ),
        ALIGN_RIGHT     ( SWT.RIGHT ),
        PASSWORD        ( SWT.PASSWORD ),
        /** Allow a download link to be shown if value is an URL. */
        ALLOW_DOWNLOAD  ( 1<<30 );
        
        public int constant = -1;
        Style( int constant ) {
            this.constant = constant;
        }
    }
    
    // instance *******************************************
    
    private IFormFieldSite          site;

    private Text                    text;
    
    private Button                  downloadLink;

    // XXX use (proper) validator to make the translation to String
    private Object                  loadedValue;
    
    private boolean                 deferredEnabled = true;

    private Style[]                 styles = new Style[] { Style.ALIGN_LEFT };
    
    
    public StringFormField( Style... styles ) {
        this.styles = styles;    
    }

    @Override
    public void init( IFormFieldSite _site ) {
        this.site = _site;
    }

    @Override
    public void dispose() {
        text.dispose();
    }

    @Override
    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        int swt = SWT.NONE;
        for (Style style : styles) {
            swt |= style.constant;
        }
        return createControl( parent, toolkit, swt );
    }

    protected Control createControl( Composite parent, IFormEditorToolkit toolkit, int style ) {
        Control result = null;
        
        // download supported
        if (ArrayUtils.contains( styles, Style.ALLOW_DOWNLOAD )) {
            result = parent = site.getToolkit().createComposite( parent );
            parent.setLayout( FormLayoutFactory.defaults().spacing( 3 ).create() );

            text = toolkit.createText( parent, "", style );
            text.setLayoutData( FormDataFactory.filled().create() );
        }
        // basic style, no download link supported
        else {
            result = text = toolkit.createText( parent, "", style );
        }

        // modify listener
        text.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                log.debug( "modifyEvent(): test= " + text.getText() );
                site.fireEvent( StringFormField.this, IFormFieldListener.VALUE_CHANGE,
                        loadedValue == null && text.getText().equals( "" ) ? null : text.getText() );
                
                checkDownloadLink();
            }
        });
        // focus listener
        text.addFocusListener( new FocusListener() {
            public void focusLost( FocusEvent event ) {
                text.setBackground( FormEditorToolkit.textBackground );
                site.fireEvent( StringFormField.this, IFormFieldListener.FOCUS_LOST, text.getText() );
            }
            public void focusGained( FocusEvent event ) {
                text.setBackground( FormEditorToolkit.textBackgroundFocused );
                site.fireEvent( StringFormField.this, IFormFieldListener.FOCUS_GAINED, text.getText() );
            }
        });
        text.setEnabled( deferredEnabled );
        text.setBackground( deferredEnabled ? FormEditorToolkit.textBackground : FormEditorToolkit.textBackgroundDisabled );
        return result;
    }

    @Override
    public IFormField setEnabled( boolean enabled ) {
        if (text != null) {
            text.setEnabled( enabled );
            text.setBackground( enabled ? FormEditorToolkit.textBackground : FormEditorToolkit.textBackgroundDisabled );
        }
        else {
            deferredEnabled = enabled;
        }
        return this;
    }

    /**
     * Explicitly set the value of the text field. This causes events to be
     * fired just like the value was typed in.
     */
    @Override
    public IFormField setValue( Object value ) {
        text.setText( value != null ? (String)value : "" );
        return this;
    }

    @Override
    public void load() throws Exception {
        assert text != null : "Control is null, call createControl() first.";

        loadedValue = site.getFieldValue();
        text.setText( loadedValue != null ? loadedValue.toString() : "" );
    }

    @Override
    public void store() throws Exception {
        // XXX what is the semantics?
//        if (text.getEnabled() && (text.getStyle() | SWT.READ_ONLY) == 0) {
            site.setFieldValue( text.getText() );
//        }
    }

    protected void checkDownloadLink() {
        if (ArrayUtils.contains( styles, Style.ALLOW_DOWNLOAD )) {
            final String value = text.getText();
            if (value != null && URL_PATTERN.matcher( value ).matches()) {
                if (downloadLink == null) {
                    //downloadLink = site.getToolkit().createLabel( text.getParent(), value );
                    //downloadLink.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
                    
                    //downloadLink = site.getToolkit().createButton( text.getParent(), null, SWT.PUSH );
                    downloadLink = new Button( text.getParent(), SWT.PUSH );
                    downloadLink.setImage( RheiFormPlugin.getDefault().imageForName( "icons/etool16/run.gif" ) );
                    downloadLink.setToolTipText( "Download" );
                    downloadLink.setLayoutData( FormDataFactory.filled().clearLeft().width( 30 ).create() );
                    downloadLink.addSelectionListener( new SelectionAdapter() {
                        public void widgetSelected( SelectionEvent e ) {
                            ExternalBrowser.open( "download_window", text.getText(),
                                    ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS );

                        }
                    });

                    text.setLayoutData( FormDataFactory.filled().right( downloadLink ).create() );
                    text.getParent().getParent().layout( true );
                }
            }
        }
    }
    
}
