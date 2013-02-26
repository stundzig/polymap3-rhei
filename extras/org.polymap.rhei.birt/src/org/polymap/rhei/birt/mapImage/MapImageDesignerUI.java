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

import java.io.ByteArrayInputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import org.eclipse.birt.report.designer.ui.extensions.IReportItemImageProvider;
import org.eclipse.birt.report.designer.ui.extensions.IReportItemLabelProvider;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.extension.IReportItem;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MapImageDesignerUI
        implements IReportItemImageProvider, IReportItemLabelProvider {

    public MapImageDesignerUI() {
    }

    
    @Override
    public String getLabel( ExtendedItemHandle handle ) {
        return "MapImage";
    }

    
    @Override
    public Image getImage( ExtendedItemHandle handle ) {
        WorkbenchConnector connector = new WorkbenchConnector( "http://localhost:10080", "admin", "login" );
        byte[] bytes = connector.mapImage( 300, 300 );
        
        ImageLoader imageLoader = new ImageLoader();
        Image image = new Image( Display.getDefault(), new ByteArrayInputStream( bytes ) );
        return image;
    }


    @Override
    public void disposeImage( ExtendedItemHandle handle, Image image ) {
    }


//    @Override
//    public IFigure createFigure( ExtendedItemHandle handle ) {
//        System.out.println( "createFigure(): ..." );
//        try {
//            handle.loadExtendedElement();
//            IReportItem reportItem = handle.getReportItem();
//            
////          final DesignerRepresentation dr = new DesignerRepresentation( reportItem );
//
//            return new DesignerFigure( reportItem );
//        }
//        catch (ExtendedElementException e) {
//            return null;
//        }
//    }
//
//    
//    @Override
//    public void updateFigure( ExtendedItemHandle handle, IFigure figure ) {
//        System.out.println( "updateFigure(): ..." );
//    }
//
//
//    @Override
//    public void disposeFigure( ExtendedItemHandle handle, IFigure figure ) {
//        System.out.println( "disposeFigure(): ..." );
//    }

    

    /**
     * 
     * 
     */
    protected class DesignerFigure
            extends Figure {
        
        private IReportItem         reportItem;
        
        private boolean             painting;

        
        public DesignerFigure( IReportItem reportItem ) {
            super();
            this.reportItem = reportItem;
            setBounds( new Rectangle( 0, 0, 100, 100 ) );
        }

        
        @Override
        public void paintClientArea( Graphics g ) {
            if (painting) {
                return;
            }
            final Rectangle r = getClientArea().getCopy();
            System.out.println( "paintClientArea(): " + r.width + "*" + r.height );
            if (r.width <= 0 || r.height <= 0) {
                return;
            }
            
            try {
                painting = true;

                Display display = Display.getDefault();
                setBackgroundColor( display.getSystemColor( SWT.COLOR_BLUE ) );
                setForegroundColor( display.getSystemColor( SWT.COLOR_DARK_GRAY ) );

                g.setForegroundColor( getForegroundColor() );
                g.drawString( "juhuundjuchhei", 50, 50 );
            }
            finally {
                painting = false;
            }
        }

    }
    
}
