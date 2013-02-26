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

import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.AdvancePropertyPage;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.AlterPage;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.BookMarkExpressionPage;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.BordersPage;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.ItemMarginPage;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.NamedExpressionsPage;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.SectionPage;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.TOCExpressionPage;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.UserPropertiesPage;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.page.VisibilityPage;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.CategoryProvider;
import org.eclipse.birt.report.designer.ui.views.attributes.providers.CategoryProviderFactory;
import org.eclipse.birt.report.designer.ui.views.attributes.providers.ICategoryProvider;
import org.eclipse.birt.report.designer.ui.views.attributes.providers.ICategoryProviderFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MapImageCategoryProviderFactory
        extends CategoryProviderFactory {

    private static ICategoryProviderFactory instance = new MapImageCategoryProviderFactory();

    /**
     * 
     * @return The unique CategoryProviderFactory instance
     */
    public static ICategoryProviderFactory getInstance() {
        return instance;
    }

    
    // instance *******************************************
    
    protected MapImageCategoryProviderFactory() {
    }


    public ICategoryProvider getCategoryProvider( Object input ) {
		CategoryProvider provider = new CategoryProvider( new String[]{
				CategoryProviderFactory.CATEGORY_KEY_GENERAL,
				CategoryProviderFactory.CATEGORY_KEY_BORDERS,
				CategoryProviderFactory.CATEGORY_KEY_MARGIN,
				CategoryProviderFactory.CATEGORY_KEY_ALTTEXT,
				CategoryProviderFactory.CATEGORY_KEY_SECTION,
				CategoryProviderFactory.CATEGORY_KEY_VISIBILITY,
				CategoryProviderFactory.CATEGORY_KEY_TOC,
				CategoryProviderFactory.CATEGORY_KEY_BOOKMARK,
				CategoryProviderFactory.CATEGORY_KEY_USERPROPERTIES,
				CategoryProviderFactory.CATEGORY_KEY_NAMEDEXPRESSIONS,
				CategoryProviderFactory.CATEGORY_KEY_ADVANCEPROPERTY,
		}, new String[]{
				"DataPageGenerator.List.General", //$NON-NLS-1$
				"DataPageGenerator.List.Borders", //$NON-NLS-1$
				"DataPageGenerator.List.Margin", //$NON-NLS-1$
				"ImagePageGenerator.List.AltText", //$NON-NLS-1$
				"DataPageGenerator.List.Section", //$NON-NLS-1$
				"DataPageGenerator.List.Visibility", //$NON-NLS-1$
				"DataPageGenerator.List.TOC", //$NON-NLS-1$
				"DataPageGenerator.List.Bookmark", //$NON-NLS-1$
				"ReportPageGenerator.List.UserProperties", //$NON-NLS-1$
				"ReportPageGenerator.List.NamedExpressions", //$NON-NLS-1$
				"ReportPageGenerator.List.AdvancedProperty", //$NON-NLS-1$
		}, new Class[]{
				MapImageGeneralPage.class,
				BordersPage.class,
				ItemMarginPage.class,
				AlterPage.class,
				SectionPage.class,
				VisibilityPage.class,
				TOCExpressionPage.class,
				BookMarkExpressionPage.class,
				UserPropertiesPage.class,
				NamedExpressionsPage.class,
				AdvancePropertyPage.class,
		} );
		return provider;
	}
}
