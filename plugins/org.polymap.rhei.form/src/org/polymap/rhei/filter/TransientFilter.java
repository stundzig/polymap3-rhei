/*
 * polymap.org
 * Copyright 2010, Falko Bräutigam, and other contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 */
package org.polymap.rhei.filter;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.opengis.filter.Filter;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class TransientFilter
        implements IFilter {

    private static final AtomicInteger PROP_NAME_COUNT = new AtomicInteger( 0 );
    
    private String                  id;

    private ILayer                  layer;

    private String                  label;

    private Set<String>             keywords;

    private Filter                  filter;

    private int                     maxResults;

    private IFilterEditorSite       site;


    public TransientFilter( String id, ILayer layer, String label, Set<String> keywords, Filter filter,
            int maxResults ) {
        this.id = id;
        this.layer = layer;
        this.label = label;
        this.keywords = keywords;
        this.filter = filter;
        this.maxResults = maxResults;
    }

    protected TransientFilter setSite( IFilterEditorSite site ) {
        this.site = site;
        return this;
    }
    
    public String getId() {
        return id;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public String getLabel() {
        return label;
    }

    public Filter createFilter( IFilterEditorSite _site ) {
        return filter;
    }

    public boolean hasControl() {
        return false;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public ILayer getLayer() {
        return layer;
    }

    /**
     * 
     * 
     * @param parent
     * @param propertyName
     * @return A new builder that can be used to set several aspects of the form
     *         field and to actually {@link FilterFieldBuilder#create()} the field
     *         inthe form.
     */
    protected FilterFieldBuilder newFormField( Class propType ) {
        assert site != null : "This TransientFilter is not initialized: call setSite() from sub-class";
        return new FilterFieldBuilder( propType );
    }

    
    /**
     * This field builder allows to create a new form field. It provides a simple,
     * chainable API that allows to set several aspects of the result. If an aspect
     * is not set then a default is computed.
     */
    public class FilterFieldBuilder {
        
        private String              propName;

        private Class               propType;
        
        private Composite           parent;
        
        private String              label;
        
        private String              tooltip;
        
        private IFormField          field;
        
        private IFormFieldValidator validator;
        
        private boolean             enabled = true;

        private Object              layoutData;
        
        
        public FilterFieldBuilder( Class propType ) {
            this.propName = String.valueOf( PROP_NAME_COUNT.getAndIncrement() );
            this.propType = propType;
            this.label = propName;
        }

        public FilterFieldBuilder( Class propType, Composite parent ) {
            this( propType );
            this.parent = parent;
        }

//        public FormFieldBuilder setFocus( boolean focus ) {
//            this.focus = focus;
//            return this;
//        }
        
        
        public String getPropName() {
            return propName;
        }

        public FilterFieldBuilder setPropName( String propName ) {
            this.propName = propName;
            return this;
        }

        public FilterFieldBuilder setParent( Composite parent ) {
            this.parent = parent instanceof Section 
                    ? (Composite)((Section)parent).getClient() : parent;
            return this;
        }

        public FilterFieldBuilder setLabel( String label ) {
            this.label = label;
            return this;
        }
        
        public FilterFieldBuilder setToolTipText( String tooltip ) {
            this.tooltip = tooltip;
            return this;
        }

        public FilterFieldBuilder setField( IFormField field ) {
            this.field = field;
            return this;
        }

        public FilterFieldBuilder setValidator( IFormFieldValidator validator ) {
            this.validator = validator;
            return this;
        }

        public FilterFieldBuilder setEnabled( boolean enabled ) {
            this.enabled = enabled;
            return this;
        }
        
        public FilterFieldBuilder setLayoutData( Object data ) {
            this.layoutData = data;
            return this;
        }
        
        public Composite create() {
            if (parent == null) {
                parent = site.getPageBody();
            }
            if (field == null) {
                // Number
                if (Number.class.isAssignableFrom( propType )) {
                    field = new StringFormField();
                    validator = new NumberValidator( propType, Polymap.getSessionLocale() );
                }
                // Date
                else if (Date.class.isAssignableFrom( propType )) {
                    field = new DateTimeFormField();
                }
                // Boolean
                else if (Date.class.isAssignableFrom( propType )) {
                    field = new CheckboxFormField();
                }
                // default: String
                else {
                    field = new StringFormField();
                }
            }
            Composite result = site.newFormField( parent, propName, propType, field, validator, label );
            // layoutData
            if (layoutData != null) {
                result.setLayoutData( layoutData );
            }
            else {
                site.addStandardLayout( result );
            }
            // tooltip
            if (tooltip != null) {
                result.setToolTipText( tooltip );
            }
            else if (!label.equals( IFormFieldLabel.NO_LABEL )) {
                result.setToolTipText( label );                
            }
//            // editable
//            if (!enabled) {
//                site.setFieldEnabled( propName, enabled );
//            }
            return result;
        }
        
        
        public <T> T getValue() {
            return site.getFieldValue( propName );
        }
        
        public boolean isSet() {
            return site.getFieldValue( propName ) != null;
        }
    }

}
