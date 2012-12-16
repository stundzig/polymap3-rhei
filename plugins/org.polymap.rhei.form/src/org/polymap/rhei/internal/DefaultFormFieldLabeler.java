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
package org.polymap.rhei.internal;

import org.apache.commons.lang.StringUtils;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldSite;
import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultFormFieldLabeler
        implements IFormFieldLabel, IFormFieldListener {

    private IFormFieldSite      site;
    
    private String              labelStr;
    
    private int                 maxWidth = 100;

    private Label               label;
    
    private Font                orig;
    
    
    /**
     * Use the field name as labelStr. 
     */
    public DefaultFormFieldLabeler() {
    }

    public DefaultFormFieldLabeler( String label ) {
        if (label != null && label.equals( NO_LABEL )) {
            this.labelStr = label;
            this.maxWidth = 0;
        }
        else {
            this.labelStr = label;
        }
    }

    public void init( IFormFieldSite _site ) {
        this.site = _site;    
    }

    public void dispose() {
        site.removeChangeListener( this );
    }

    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        label = toolkit.createLabel( parent, 
                labelStr != null ? labelStr : StringUtils.capitalize( site.getFieldName() ) );
        
        // focus listener
        site.addChangeListener( this );
        return label;
    }

    public void fieldChange( FormFieldEvent ev ) {
        if (label.isDisposed()) {
            return;
        }
        if (ev.getEventCode() == FOCUS_GAINED) {
            label.setForeground( FormEditorToolkit.labelForegroundFocused );
            orig = label.getFont();
//            label.setFont( JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) );
        }
        else if (ev.getEventCode() == FOCUS_LOST) {
            label.setForeground( FormEditorToolkit.labelForeground );
//            label.setFont( orig );
        }
    }
    
    public void setMaxWidth( int maxWidth ) {
        this.maxWidth = maxWidth;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

}
