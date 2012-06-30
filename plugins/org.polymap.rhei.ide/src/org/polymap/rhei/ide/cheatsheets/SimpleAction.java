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

import org.w3c.dom.Node;

import org.eclipse.ui.internal.cheatsheets.data.AbstractExecutable;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetManager;

import org.eclipse.core.runtime.IStatus;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public abstract class SimpleAction
        extends AbstractExecutable {

    public abstract IStatus execute( CheatSheetManager csm );
    
    public boolean isCheatSheetManagerUsed() {
        return false;
    }

    public boolean hasParams() {
        return false;
    }
    
    public boolean handleAttribute( Node attribute ) {
        return false;
    }
    
    public String checkAttributes( Node node ) {
        return null;
    }

}
