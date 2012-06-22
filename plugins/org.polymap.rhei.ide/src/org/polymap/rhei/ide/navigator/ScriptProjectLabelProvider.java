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

import org.eclipse.core.resources.IResource;

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
//            if (elm instanceof IResource) {
//                //            LayerIconImageDescriptor imageDescr = new LayerIconImageDescriptor();
//                //            Image result = RheiPlugin.getDefault().imageForDescriptor( imageDescr, "layerIcon" );
//                Image result = RheiIdePlugin.getDefault().imageForName( "icons/obj16/layer_disabled_obj.gif" );
                return PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJ_FOLDER );
//                return result;
//            }
//            return null;
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