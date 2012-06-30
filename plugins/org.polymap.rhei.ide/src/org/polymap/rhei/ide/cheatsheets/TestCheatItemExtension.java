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
package org.polymap.rhei.ide.cheatsheets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.eclipse.ui.cheatsheets.AbstractItemExtensionElement;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class TestCheatItemExtension
        extends AbstractItemExtensionElement {

    private static Log log = LogFactory.getLog( TestCheatItemExtension.class );

    private String          value;
    

    public TestCheatItemExtension( String attributeName ) {
        super( attributeName );
        log.info( "name: " + attributeName );
    }


    public void handleAttribute( String attributeValue ) {
        log.info( "value: " + attributeValue );
        this.value = attributeValue;
    }


    public void createControl( Composite parent ) {
        log.info( "label: " + value );
        Text c = new Text( parent, SWT.BORDER );
        c.setText( value );
        
    }


    public void dispose() {
    }
    
}
