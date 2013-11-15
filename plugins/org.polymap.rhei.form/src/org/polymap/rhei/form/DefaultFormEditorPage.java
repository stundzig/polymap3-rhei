/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
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
package org.polymap.rhei.form;

import java.util.Date;

import org.geotools.data.FeatureStore;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.FormDataFactory;

import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;

/**
 * Provides some defaults to implement a form editor page.
 * <p/>
 * The <b>layout</b> is based on {@link FormLayout}. In order to get this working the
 * {@link #createFormContent(IFormEditorPageSite)} should be called from sub-classes.
 * <p/>
 * The default priority is 1, showing "above" any unspecific standard page. The priority
 * might be adjusted by overriding {@link #getPriority()}.  
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultFormEditorPage
        implements IFormEditorPage {

    private static Log log = LogFactory.getLog( DefaultFormEditorPage.class );
    
    /** The default space between the sections of the page. */
    public static final int         SECTION_SPACING = 6;

    /**
     * The ID of this form.
     */
    protected String                id;
    
    /**
     * The title of this form.
     */
    protected String                title;
    
    /**
     * The feature that is shown in this form.
     */
    protected Feature               feature;

    /**
     * The {@link FeatureStore} of the {@link #feature} of this form.
     */
    protected FeatureStore          fs;
    
    protected IFormEditorPageSite   pageSite;

    protected Control               lastLayoutElm;
    
    
    public DefaultFormEditorPage( String id, String title, Feature feature, FeatureStore fs ) {
        this.id = id;
        this.title = title;
        this.feature = feature;
        this.fs = fs;
    }

    /**
     * Initializes the layout. Sub-classes must call this.
     */
    public void createFormContent( IFormEditorPageSite site ) {
        site.getPageBody().setLayout( new FormLayout() );
        this.pageSite = site;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public byte getPriority() {
        return 1;
    }

    public Action[] getEditorActions() {
        return null;
    }


    /**
     * Creates a new section in the form. The section is expanded by default.
     * 
     * @param sectionTitle The title of this section, can be null or empty.
     * @param rightOf True indicates that the section is placed right of the
     *        <code>relative</code>, or at the bottom otherwise.
     * @param relative
     * @return The newly created section.
     */
    protected Section newSection( String sectionTitle, boolean rightOf, Composite relative ) {
        Composite parent = pageSite.getPageBody();
        IFormEditorToolkit tk = pageSite.getToolkit();
        
        Section section = null;
        if (sectionTitle == null || sectionTitle.isEmpty()) {
            section = tk.createSection( parent, Section.NO_TITLE );
        } else {
            section = tk.createSection( parent, Section.TITLE_BAR /*| Section.TREE_NODE*/ );
            section.setText( sectionTitle );
        }    
        section.setExpanded( true );
        //section.setLayout( new FillLayout() );

        Composite client = tk.createComposite( section );
        FillLayout layout = new FillLayout( SWT.VERTICAL );
        layout.spacing = 1;
        layout.marginWidth = 2;
        layout.marginHeight = 2;
        client.setLayout( layout );
        section.setClient( client );
        
        if (relative == null) {
            section.setLayoutData( new SimpleFormData( SECTION_SPACING )
                    .left( 0 ).right( 100 ).top( 0, 0 ).create() );
        }
        else if (rightOf) {
            FormData data = (FormData)relative.getLayoutData();
            data.right = new FormAttachment( section );
            
            section.setLayoutData( new SimpleFormData( SECTION_SPACING )
                    .left( 50 ).right( 100 ).top( 0, 0 ).create() );
        }
        return section;
    }


    protected Control applyLayout( Control field ) {
        // RowLayout
        if (field.getParent().getLayout() instanceof RowLayout) {
            // width defines the minimum width of the entire form
            // before horiz. scrollbar starts to appear
            RowData layoutData = new RowData( 300, SWT.DEFAULT );
            field.setLayoutData( layoutData );
        }

        // FormLayout
        else if (field.getParent().getLayout() instanceof FormLayout) {
            // width defines the minimum width of the entire form
            // before horiz. scrollbar starts to appear
            FormDataFactory formData = new SimpleFormData().width( 40 ).left( 0, 3 ).right( 100, -3 ).top( 0 );
            if (lastLayoutElm != null) {
                formData.top( lastLayoutElm, 3 );
            }
            field.setLayoutData( formData.create() );

            //        FormData layoutData = new FormData( 40, SWT.DEFAULT );
            //        layoutData.left = new FormAttachment( 0, DEFAULT_FIELD_SPACING_H );
            //        layoutData.right = new FormAttachment( 100, -DEFAULT_FIELD_SPACING_H );
            //        layoutData.top = lastLayoutElm != null
            //                ? new FormAttachment( lastLayoutElm, DEFAULT_FIELD_SPACING_V )
            //                : new FormAttachment( 0 );
            //        field.setLayoutData( layoutData );
            //
            lastLayoutElm = field;
        }
        else {
            log.warn( "Unknown layout type: " + field.getParent().getLayout() );
        }
        return field;
    }

    /**
     * 
     * 
     * @param parent
     * @param propertyName
     * @return A new builder that can be used to set several aspects of the form
     *         field and to actually {@link FormFieldBuilder#create()} the field
     *         inthe form.
     */
    protected FormFieldBuilder newFormField( String propertyName ) {
        return new FormFieldBuilder( propertyName );
    }


    /**
     * This field builder allows to create a new form field. It provides a simple,
     * chainable API that allows to set several aspects of the result. If an aspect
     * is not set then a default is computed.
     */
    public class FormFieldBuilder {
        
        private String              propName;
        
        private Composite           parent;
        
        private String              label;
        
        private String              tooltip;
        
        private Feature             builderFeature;
        
        private Property            prop;
        
        private IFormField          field;
        
        private IFormFieldValidator validator;
        
        private boolean             enabled = true;

        private Object              layoutData;
        
        
        public FormFieldBuilder( String propName ) {
            this.propName = propName;
            this.label = propName;
            this.builderFeature = feature;
        }

        public FormFieldBuilder( String propName, Composite parent) {
            this( propName );
            this.parent = parent;
        }

//        public FormFieldBuilder setFocus( boolean focus ) {
//            this.focus = focus;
//            return this;
//        }
        
        public FormFieldBuilder setParent( Composite parent ) {
            this.parent = parent instanceof Section 
                    ? (Composite)((Section)parent).getClient() : parent;
            return this;
        }

        public FormFieldBuilder setProperty( Property prop ) {
            this.prop = prop;
            return this;
        }

        public FormFieldBuilder setFeature( Feature feature ) {
            this.builderFeature = feature;
            return this;
        }
        
        public FormFieldBuilder setLabel( String label ) {
            this.label = label;
            return this;
        }
        
        public FormFieldBuilder setToolTipText( String tooltip ) {
            this.tooltip = tooltip;
            return this;
        }

        public FormFieldBuilder setField( IFormField field ) {
            this.field = field;
            return this;
        }

        public FormFieldBuilder setValidator( IFormFieldValidator validator ) {
            this.validator = validator;
            return this;
        }

        public FormFieldBuilder setEnabled( boolean enabled ) {
            this.enabled = enabled;
            return this;
        }
        
        public FormFieldBuilder setLayoutData( Object data ) {
            this.layoutData = data;
            return this;
        }
        
        public Composite create() {
            if (parent == null) {
                parent = pageSite.getPageBody();
            }
            if (prop == null) {
                prop = builderFeature.getProperty( propName );
                if (prop == null) {
                    throw new IllegalStateException( "No such property: " + propName );
                }
            }
            if (field == null) {
                Class binding = prop.getType().getBinding();
                // Number
                if (Number.class.isAssignableFrom( binding )) {
                    field = new StringFormField();
                    validator = new NumberValidator( binding, Polymap.getSessionLocale() );
                }
                // Date
                else if (Date.class.isAssignableFrom( binding )) {
                    field = new DateTimeFormField();
                }
                // Boolean
                else if (Date.class.isAssignableFrom( binding )) {
                    field = new CheckboxFormField();
                }
                // default: String
                else {
                    field = new StringFormField();
                }
            }
            Composite result = pageSite.newFormField( parent, prop, field, validator, label );
            // layoutData
            if (layoutData != null) {
                result.setLayoutData( layoutData );
            }
            else {
                applyLayout( result );
            }
            // tooltip
            if (tooltip != null) {
                result.setToolTipText( tooltip );
            }
            else if (!label.equals( IFormFieldLabel.NO_LABEL )) {
                result.setToolTipText( label );                
            }
            // editable
            if (!enabled) {
                pageSite.setFieldEnabled( prop.getName().getLocalPart(), enabled );
            }
            return result;
        }
    }
    
    
    /**
     * Checks if the name of type of the {@link Feature} of this form contains one
     * of the given Strings. This method can be used to check if the page is compatible
     * to the given feature and its type.
     *
     * @param names The strings to check against the feature type name.
     * @param ignoreCase Ignore the character case of the strings.
     * @param considerNameSpace Compare with namespace of the feature type too.
     * @return
     */
    protected boolean featureNameContains( String[] names, boolean ignoreCase, boolean considerNameSpace ) {
        assert names != null;
        String typeName = feature.getType().getName().getLocalPart();
        String ns = feature.getType().getName().getNamespaceURI();
        
        for (String name : names) {
            name = ignoreCase ? name.toLowerCase() : name;
            if (ignoreCase && typeName.toLowerCase().contains( name )) {
                return true;
            }
            else if (!ignoreCase && typeName.contains( name )) {
                return true;
            }
            else if (considerNameSpace && ignoreCase && ns != null && ns.toLowerCase().contains( name )) {
                return true;
            }
            else if (considerNameSpace && !ignoreCase && ns != null && ns.contains( name )) {
                return true;
            }
        }
        return false;
    }
    
}
