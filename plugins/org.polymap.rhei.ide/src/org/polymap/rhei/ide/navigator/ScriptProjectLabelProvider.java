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
package org.polymap.rhei.ide.navigator;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.polymap.rhei.ide.RheiIdePlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ScriptProjectLabelProvider
        extends DecoratingLabelProvider {

    public ScriptProjectLabelProvider() {
        super( new BaseLabelProvider(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator() );
    }

    /*
     * 
     */
    static class BaseLabelProvider
            extends LabelProvider {

        public Image getImage( Object elm ) {
            if (elm instanceof IContainer) {
                return PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJ_FOLDER );
            }
            else if (elm instanceof IFile) {
                return RheiIdePlugin.getDefault().imageForName( "icons/obj16/file_obj.gif" );
            }
            else {
                return null;
            }
        }


        public String getText( Object elm ) {
            if (elm instanceof IResource) {
                return ((IResource)elm).getName();
            }
            else {
                return elm.toString();
            }
        }

    }
    
}