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
package org.polymap.rhei.birt.dataSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.datatools.connectivity.oda.design.ui.wizards.DataSetWizardPage;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CustomDataSetWizardPage
        extends DataSetWizardPage {

    public CustomDataSetWizardPage( String pageName ) {
        super( pageName );
        setTitle( pageName );
        setMessage( "CustomDataSetWizardPage..." );
    }


    public CustomDataSetWizardPage( String pageName, String title, ImageDescriptor titleImage ) {
        super( pageName, title, titleImage );
        setMessage( "CustomDataSetWizardPage..." );
    }


    @Override
    public void createPageCustomControl( Composite parent ) {
        setControl( createPageControl( parent ) );
        //initializeControl();
    }
    
    protected Control createPageControl( Composite parent ) {
        Label l = new Label( parent, SWT.None );
        l.setText( "CustomDataSetWizardPage..." );
        return l;
    }
    
}
