/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.birt.mapImage;

import java.util.List;

import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.AttributePage;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.WidgetUtil;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.LibraryDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.TextPropertyDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.UnitPropertyDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.SeperatorSection;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.TextSection;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.UnitSection;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.model.api.elements.ReportDesignConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MapImageGeneralPage
        extends AttributePage {

    private TextSection         librarySection;

    private SeperatorSection    separatorSection;

    
    public void refresh() {
        if (input instanceof List
                && DEUtil.getMultiSelectionHandle( (List)input ).isExtendedElements()) {
            librarySection.setHidden( false );
            separatorSection.setHidden( false );
            librarySection.load();
        }
        else {
            librarySection.setHidden( true );
            separatorSection.setHidden( true );
        }
        container.layout( true );
        container.redraw();
        super.refresh();
    }

    
    /**
     * Builds the UI in Property Editor General category page Adds controls for : -
     * Display text - Rotation Angle - Font Family - Font Size
     */
    public void buildUI( Composite parent ) {
        super.buildUI( parent );
        container.setLayout( WidgetUtil.createGridLayout( 5, 15 ) );

//        System.out.println( "Properties: " + propertiesMap.size() );    
//        for (Iterator it=propertiesMap.entrySet().iterator(); it.hasNext(); ) {
//            Map.Entry entry = (Map.Entry)it.next();
//            System.out.println( "Property: " + entry.getKey());    
//        }
//        System.out.println( "input: " + input );
//        ExtendedItemHandle reportItem = (ExtendedItemHandle)((List)input).get( 0 );
//        DimensionHandle dim = reportItem.getWidth();
//        System.out.println( "input.getWidth: " + dim.getDisplayValue() );
//        System.out.println( "input.getWidth: " + dim.getMeasure() );
        
        LibraryDescriptorProvider provider = new LibraryDescriptorProvider();
        librarySection = new TextSection( provider.getDisplayName(), container, true );
        librarySection.setProvider( provider );
        librarySection.setGridPlaceholder( 2, true );
        addSection( MapImagePageSectionID.LIBRARY, librarySection );

        separatorSection = new SeperatorSection( container, SWT.HORIZONTAL );
        addSection( MapImagePageSectionID.SEPARATOR, separatorSection );
        
        // display text property
        TextPropertyDescriptorProvider nameProvider = new TextPropertyDescriptorProvider(
                "displayText", //$NON-NLS-1$
                ReportDesignConstants.EXTENDED_ITEM );
        TextSection nameSection = new TextSection( "Description:", //$NON-NLS-1$
                container, true );
        nameSection.setProvider( nameProvider );
        nameSection.setGridPlaceholder( 3, true );
        nameSection.setWidth( 200 );
        addSection( MapImagePageSectionID.DISPLAY_TEXT, nameSection );
    
//        // rotation angle property
//        TextPropertyDescriptorProvider angleProvider = new TextPropertyDescriptorProvider(
//                "rotationAngle",
//                ReportDesignConstants.EXTENDED_ITEM );
//        TextSection angleSection = new TextSection( "Angle:",
//                container, true );
//        angleSection.setProvider( angleProvider );
//        angleSection.setGridPlaceholder( 3, true );
//        angleSection.setWidth( 50 );
//        addSection( MapImagePageSectionID.ROTATION_ANGLE, angleSection );
        
//        // width property
//        TextPropertyDescriptorProvider widthProvider = new TextPropertyDescriptorProvider(
//                "width", ReportDesignConstants.EXTENDED_ITEM );
//        TextSection widthSection = 
//                new TextSection( "Width:", container, true );
//        widthSection.setProvider( widthProvider );
//        widthSection.setGridPlaceholder( 3, true );
//        widthSection.setWidth( 100 );
//        addSection( MapImagePageSectionID.WIDTH, widthSection );
        
        // width units
        UnitPropertyDescriptorProvider widthUnitProvider = new UnitPropertyDescriptorProvider(
                "width", ReportDesignConstants.EXTENDED_ITEM );
        UnitSection widthUnitSection = 
                new UnitSection( "Width:", container, true );
        widthUnitSection.setProvider( widthUnitProvider );
        widthUnitSection.setGridPlaceholder( 1, true );
        widthUnitSection.setWidth( 10 );
        addSection( MapImagePageSectionID.WIDTH, widthUnitSection );
        
//        // height property
//        TextPropertyDescriptorProvider heightProvider = new TextPropertyDescriptorProvider(
//                "height", ReportDesignConstants.EXTENDED_ITEM );
//        TextSection heightSection = 
//                new TextSection( "Height:", container, true );
//        heightSection.setProvider( heightProvider );
//        heightSection.setGridPlaceholder( 3, true );
//        heightSection.setWidth( 100 );
//        addSection( MapImagePageSectionID.HEIGHT, heightSection );
        
        // height unit
        UnitPropertyDescriptorProvider heightUnitProvider = new UnitPropertyDescriptorProvider(
                "height", ReportDesignConstants.EXTENDED_ITEM );
        UnitSection heightUnitSection = 
                new UnitSection( "Height:", container, true );
        heightUnitSection.setProvider( heightUnitProvider );
        heightUnitSection.setGridPlaceholder( 3, true );
        heightUnitSection.setWidth( 100 );
        addSection( MapImagePageSectionID.HEIGHT, heightUnitSection );
        
//        // font family property
//        ComboPropertyDescriptorProvider fontFamilyProvider = new ComboPropertyDescriptorProvider(
//                StyleHandle.FONT_FAMILY_PROP, ReportDesignConstants.STYLE_ELEMENT );
//        ComboSection fontFamilySection = new ComboSection( fontFamilyProvider.getDisplayName(),
//                container, true );
//        fontFamilySection.setProvider( fontFamilyProvider );
//        fontFamilySection.setLayoutNum( 2 );
//        fontFamilySection.setWidth( 200 );
//        addSection( PageSectionId.LABEL_FONT_FAMILY, fontFamilySection );
        
//        // font size property
//        FontSizePropertyDescriptorProvider fontSizeProvider = new FontSizePropertyDescriptorProvider(
//                StyleHandle.FONT_SIZE_PROP, ReportDesignConstants.STYLE_ELEMENT );
//        FontSizeSection fontSizeSection = new FontSizeSection( fontSizeProvider.getDisplayName(),
//                container, true );
//        fontSizeSection.setProvider( fontSizeProvider );
//        fontSizeSection.setLayoutNum( 4 );
//        fontSizeSection.setGridPlaceholder( 2, true );
//        fontSizeSection.setWidth( 200 );
//        addSection( PageSectionId.LABEL_FONT_SIZE, fontSizeSection );

        createSections();
        layoutSections();
    }
}
