/*
 * polymap.org Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * $Id: $
 */
package org.polymap.rhei.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.primitives.Ints;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.form.FormEditorToolkit;
import org.polymap.rhei.model.ConstantWithSynonyms;

/**
 * 
 * <p/>
 * By default the {@link #setMultiple(boolean)} is set to false.
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 * @version ($Revision$)
 */
public class SelectlistFormField
        implements IFormField {

    private static Log                   log           = LogFactory
                                                               .getLog( SelectlistFormField.class );

    public static final int              MULTIPLE      = 1;

    private IFormFieldSite               site;

    private org.eclipse.swt.widgets.List list;

    private boolean                      isMultiple    = false;

    private LabelProvider                labelProvider = new LabelProvider();

    /**
     * Maps display value into associated return code (when selected). The TreeMap
     * sorts tha keys alphabetically.
     */
    private final ValueProvider          values;

    private Object                       loadedValue;


    /**
     * 
     */
    public static class LabelProvider {

        public String getText( String label, Object value ) {
            return label;
        }
    }


    /**
     * A additional {@link LabelProvider} allows to transform the labels in the
     * dropdown of the list of this selectlist.
     */
    public void setLabelProvider( LabelProvider labelProvider ) {
        this.labelProvider = labelProvider;
    }


    public SelectlistFormField( int... flags ) {
        this.values = new DefaultValueProvider();
        if (flags.length > 0) {
            setIsMultiple( ArrayUtils.contains( flags, MULTIPLE ) );
        }
    }


    /**
     * The given strings are used as label in the combo and as value to be stored in
     * the property.
     * 
     * @param values
     */
    public SelectlistFormField( Iterable<String> values, int... flags ) {
        this( flags );
        for (String value : values) {
            this.values.get().put( value, value );
        }
    }


    /**
     * The given strings are used as label in the combo and as value to be stored in
     * the property.
     * 
     * @param values
     */
    public SelectlistFormField( String[] values, int... flags ) {
        this( flags );
        for (String value : values) {
            this.values.get().put( value, value );
        }
    }


    /**
     * 
     * @param values Maps labels into property values
     */
    public SelectlistFormField( Map<String, ? extends Object> values ) {
        this.values = new DefaultValueProvider();
        this.values.get().putAll( values );
    }


    public SelectlistFormField( ValueProvider valueProvider ) {
        this.values = valueProvider;
    }


    public SelectlistFormField(
            ConstantWithSynonyms.Type<? extends ConstantWithSynonyms, String> constants ) {
        this.values = new DefaultValueProvider();
        for (ConstantWithSynonyms constant : constants) {
            this.values.get().put( (String)constant.label, constant.id );
        }
    }


    public void init( IFormFieldSite _site ) {
        this.site = _site;
    }


    public void dispose() {
        list.dispose();
    }


    /**
     * Sets the selection mode of the list.
     * <p>
     * This method can be called only while initializing before the widget has been
     * created.
     */
    public void setIsMultiple( boolean value ) {
        this.isMultiple = value;
    }


    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
        int comboStyle = isMultiple ? SWT.MULTI : SWT.SINGLE;
        list = toolkit.createList( parent, comboStyle | SWT.BORDER | SWT.V_SCROLL);
        
        // add values
        fillList();

        // selection listener
        list.addSelectionListener( new SelectionListener() {
            public void widgetSelected( SelectionEvent ev ) {
                log.debug( "widgetSelected(): selectionIndex= " + list.getSelectionIndex() );                
                site.fireEvent( SelectlistFormField.this, IFormFieldListener.VALUE_CHANGE, getSelectedValues() );
            }
            public void widgetDefaultSelected( SelectionEvent ev ) {
            }
        });

        // focus listener
        list.addFocusListener( new FocusListener() {
            public void focusLost( FocusEvent event ) {
                list.setBackground( FormEditorToolkit.textBackground );
                site.fireEvent( this, IFormFieldListener.FOCUS_LOST, getSelectedValues() );
            }
            public void focusGained( FocusEvent event ) {
                list.setBackground( FormEditorToolkit.textBackgroundFocused );
                site.fireEvent( this, IFormFieldListener.FOCUS_GAINED, getSelectedValues() );
            }
        });
        
        return list;
    }


    /**
     * 
     * @return
     */
    private List<Object> getSelectedValues() {
        List<Integer> selections = Ints.asList( list.getSelectionIndices() );
        List<Object> results = new ArrayList<Object>();
        int i = 0;
        for (String key : values.get().keySet()) {
            if (selections.contains( i )) {
                results.add( values.get().get( key ) );
            }
            i++;
        }
        return results;
    }


    private void fillList() {
        for (Map.Entry<String, Object> entry : values.get().entrySet()) {
            list.add( labelProvider.getText( entry.getKey(), entry.getValue() ) );
        }
    }


    public IFormField setEnabled( boolean enabled ) {
        list.setEnabled( enabled );
        return this;
    }


    public IFormField setValue( Object value ) {
        list.deselectAll();

        if (value != null && value instanceof Collection) {
            // find index in keyset for selected value
            List<Integer> selections = new ArrayList<Integer>();
            Collection<Object> selectedValues = (Collection<Object>)value;
            int i=0;
            for (Map.Entry<String, Object> entry : values.get().entrySet()) {
                if (selectedValues.contains( entry.getValue() )) {
                    selections.add( i );
                }
                i++;
            }
            list.setSelection( Ints.toArray( selections ) );
        }
        return this;
    }


    /**
     * Returns the current value depending on the {@link #forceTextMatch} flag. If
     * true, then the current text of the {@link #list} is returned only if it
     * matches one of the labels. Otherwise the text is returned as is.
     * 
     * @return
     */
    protected Object getValue() {
        return getSelectedValues();        
    }


    public void load()
            throws Exception {
        assert list != null : "Control is null, call createControl() first.";

        loadedValue = site.getFieldValue();
        setValue( loadedValue );

        // int i = 0;
        // for (Iterator it=values.values().iterator(); it.hasNext(); i++) {
        // if (it.next().equals( loadedValue )) {
        // combo.select( i );
        // return;
        // }
        // }
    }


    public void store()
            throws Exception {
        site.setFieldValue( getValue() );
    }


    public void reloadValues() {
        if (list == null) {
            throw new IllegalStateException( "createControl must be called before" );
        }
        list.removeAll();
        fillList();
    }


    public interface ValueProvider {

        SortedMap<String, Object> get();
    }


    public static class DefaultValueProvider
            implements ValueProvider {

        private final SortedMap<String, Object> values = new TreeMap<String, Object>();


        @Override
        public SortedMap<String, Object> get() {
            return values;
        }
    }
}
