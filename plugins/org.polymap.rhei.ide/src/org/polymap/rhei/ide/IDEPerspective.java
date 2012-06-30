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
package org.polymap.rhei.ide;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.cheatsheets.OpenCheatSheetAction;

import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.ide.java.JavadocView2;
import org.polymap.rhei.ide.navigator.ProjectNavigator;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class IDEPerspective
        implements IPerspectiveFactory {

    public void createInitialLayout( IPageLayout layout ) {
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible( true );

        IFolderLayout topLeft = layout.createFolder(
                "topLeft", IPageLayout.LEFT, 0.17f, editorArea );
//        IFolderLayout bottomLeft = layout.createFolder(
//                "bottomLeft", IPageLayout.BOTTOM, 0.50f, "topLeft" );
        IFolderLayout topRight = layout.createFolder(
                "topRight", IPageLayout.RIGHT, 0.70f, editorArea );
        IFolderLayout bottomRight = layout.createFolder(
                "bottomRight", IPageLayout.BOTTOM, 0.70f, "topRight" );
        IPlaceholderFolderLayout bottom = layout.createPlaceholderFolder(
                "bottom", IPageLayout.BOTTOM, 0.70f, editorArea );

        topLeft.addView( ProjectNavigator.ID );
        bottomRight.addView( "org.eclipse.ui.views.AllMarkersView" );
        topRight.addView( JavadocView2.ID );
        
        topRight.addPlaceholder( "org.eclipse.ui.cheatsheets.views.CheatSheetView:*" );
        topRight.addPlaceholder( "org.eclipse.ui.*" );
        
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                new OpenCheatSheetAction( "org.polymap.rhei.ide.cheatsheet.welcome" ).run();
            }
        });
//        topRight.addView( "org.eclipse.ui.cheatsheets.views.CheatSheetView:org.polymap.rhei.ide.cheatsheet1" );
        
        layout.addShowViewShortcut( "org.eclipse.ui.views.AllMarkersView" );
        layout.addShowViewShortcut( JavadocView2.ID );
        layout.addShowViewShortcut( "org.eclipse.ui.cheatsheets.views.CheatSheetView" );


//        bottomLeft.addView( LayerNavigator.ID );
//        bottomLeft.addPlaceholder( "org.polymap.rhei.FilterView:*" );
//
//        bottom.addPlaceholder( "org.polymap.*:*" );
//        bottom.addPlaceholder( "org.polymap.*" );
//        bottom.addPlaceholder( "org.eclipse.*" );
//
//        topRight.addView( "net.refractions.udig.catalog.ui.CatalogView" );
//        topRight.addPlaceholder( "org.polymap.geocoder.*" );
//
//
//        bottom.addPlaceholder( "org.polymap.core.data.ui.featureTable.view:*" );
//        bottom.addPlaceholder( "org.polymap.*" );
//        bottom.addPlaceholder( "org.eclipse.*" );
//
//        // add shortcuts to show view menu
//        layout.addShowViewShortcut( "net.refractions.udig.catalog.ui.CatalogView" );
////      layout.addShowViewShortcut( "net.refractions.udig.project.ui.projectExplorer" );
//
//      // add shortcut for other perspective
////      layout.addPerspectiveShortcut( "org.eclipse.rap.demo.perspective.planning" );
    }
}
