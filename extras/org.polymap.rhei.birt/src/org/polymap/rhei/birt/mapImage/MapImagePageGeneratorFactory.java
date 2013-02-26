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

import org.eclipse.swt.widgets.Composite;

import org.eclipse.birt.report.designer.ui.views.IPageGenerator;
import org.eclipse.birt.report.designer.ui.views.attributes.AbstractPageGenerator;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * This class is used to create the IPageGenerator instance and need registered
 * through extension point
 * 
 * @author <a href="mailto:falko@polymap.de">Falko Bräutigam</a>
 */
public class MapImagePageGeneratorFactory
        implements IAdapterFactory {

    public Object getAdapter( Object adaptableObject, Class adapterType ) {
        return new MapImagePageGenerator();
    }

    public Class[] getAdapterList() {
        return new Class[] { IPageGenerator.class };
    }

    
    /**
     * 
     * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
     */
    public class MapImagePageGenerator
            extends AbstractPageGenerator {

        public void createControl( Composite parent, Object input0 ) {
            setCategoryProvider( MapImageCategoryProviderFactory.getInstance().getCategoryProvider( input0 ) );
            super.createControl( parent, input0 );
        }
    }

}
