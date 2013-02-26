package org.polymap.rhei.birt.mapImage;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.extension.IReportItemPresentation;
import org.eclipse.birt.report.engine.extension.IRowSet;
import org.eclipse.birt.report.engine.extension.ReportItemPresentationBase;
import org.eclipse.birt.report.model.api.DimensionHandle;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.util.DimensionUtil;
import org.eclipse.draw2d.IFigure;

import org.polymap.rhei.birt.DesignerPlugin;

/**
 * This class provides the rendering capabilities for the report item. When the
 * report item is used in a report design, and the report is viewed, the methods in
 * this class are invoked to perform the actual rendering of the user defined report
 * item.
 * 
 * @author <a href="mailto:falko@polymap.de">Falko Bräutigam</a>
 */
public class MapImagePresentation
        extends ReportItemPresentationBase
        implements IReportItemPresentation {

    private int             dpi = 96;
    
    
    public void setModelObject( ExtendedItemHandle modelHandle ) {
        this.modelHandle = modelHandle;
    }

    public int getOutputType() {
        return OUTPUT_AS_IMAGE;
    }

    /**
     * @return the image MIME type (e.g. "image/svg+xml")
     */
    public String getImageMIMEType() {
        return "image/jpg";
    }

    
//    public Size getSize() {
//        // XXX Auto-generated method stub
//        throw new RuntimeException( "not yet implemented." );
//    }

    public void setResolution( int dpi ) {
        System.out.println( "DPI: " + dpi );
        this.dpi = dpi;
    }

    
    @Override
    public Object onRowSets( IRowSet[] rowSets ) throws BirtException {
        if (modelHandle == null) {
            return null;
        }
        try {
//            int pixelWidth = convertToPixels( modelHandle.getWidth(), dpi, null );
//            System.out.println( "pixelWidth:" + pixelWidth );
//
//            int pixelHeight = convertToPixels( modelHandle.getHeight(), dpi, null );
//            System.out.println( "pixelHeight:" + pixelHeight );

            
            WorkbenchConnector connector = new WorkbenchConnector( "http://localhost:10080", "admin", "login" );
            return connector.mapImage( 300, 300 );

//            // inside polymap
//            if (PolymapAdapter.runsInsidePolymap()) {
//                System.out.println( "Inside Polymap..." );
//                return PolymapAdapter.currentMapImage( pixelWidth, pixelHeight );
//            }
//            // inside designer
//            else {
//                System.out.println( "Not inside Polymap..." );
//                DesignerRepresentation dr = new DesignerRepresentation( null );
//                dr.updateImage( new Dimension( pixelWidth, pixelHeight) );
//                
//                ImageLoader imageLoader = new ImageLoader();
//                imageLoader.data = new ImageData[] { dr.getImage().getImageData() };
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                imageLoader.save( baos, SWT.IMAGE_JPEG );
//
//                return baos.toByteArray();
//            }
        }
        catch (Exception e) {
            return new BirtException( DesignerPlugin.PLUGIN_ID, "POLYMAP_", new Object[] {}, e );
        }
    }

    
    public void finish() {
        super.finish();
    }

    
    /**
     *
     * @param dim
     * @param dpi
     * @param df
     * @return
     */
    public static int convertToPixels( DimensionHandle dim, int dpi, IFigure df ) {
        double value = dim.getMeasure();
        String units = dim.getUnits();
        System.out.println( "---dim: " + value + " (" + units + ")" );

        int result = -1;
        // absolute units
        if (DimensionUtil.isAbsoluteUnit( units )) {
            double inchValue = DimensionUtil.convertTo( value, units, DesignChoiceConstants.UNITS_IN ).getMeasure();
            result = (int)(inchValue * dpi);
        }
        // % units
        else if (units == DesignChoiceConstants.UNITS_PERCENTAGE) {
            if (df == null) {
                System.out.println( "df==null is not allowed for percentage dim." );
                throw new IllegalArgumentException( "df==null is not allowed for percentage dim." );
            }
            IFigure parentFigure = df.getParent();
            if (parentFigure != null) {
                int parentPixels = parentFigure.getSize().width - parentFigure.getInsets().getWidth();
                System.out.println( "parent pixels: " + parentPixels );
                //double parentInchValue = DimensionUtil.convertTo( parentPixels, DesignChoiceConstants.UNITS_PT, DesignChoiceConstants.UNITS_IN ).getMeasure();
                //System.out.println( "parent inch: " + parentInchValue );
                
                result = (int)(parentPixels * value / 100);
                
//                int parentHeight = (int)((parentFigure.getSize().height - parentFigure.getInsets().getHeight())
//                        * dOriginalHeight / 100);
//                dOriginalHeight = ChartUtil.convertPixelsToPoints( idsSWT, height );
//                sHeightUnits = DesignChoiceConstants.UNITS_PT;
            }
        }
        // unsupported units
        else {
            throw new IllegalArgumentException( "Not supported: relativ dimension unit: " + units );
        }

        System.out.println( "pixels:" + result );
        return result;
    }
    
}
