/*
 * polymap.org Copyright 2013 Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.rhei.field;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.lf5.util.StreamUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.widgets.Upload;
import org.eclipse.rwt.widgets.UploadEvent;
import org.eclipse.rwt.widgets.UploadItem;
import org.eclipse.rwt.widgets.UploadListener;

import org.eclipse.jface.dialogs.MessageDialog;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.form.FormEditorToolkit;

/**
 * A upload form field based on the {@link Upload} widget.
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class UploadFormField
        implements IFormField {

    private static Log     log           = LogFactory.getLog( UploadFormField.class );

    private IFormFieldSite site;

    private Upload         upload;

    private boolean        enabled       = true;

    private UploadedImage  uploadedValue;

    private final File     uploadDir;

//    private Button         viewImageButton;

    private final boolean  showProgress;

    private int            thumbnailSize = 250;


    public UploadFormField( File uploadDir ) {
        this( uploadDir, false );
    }


    public UploadFormField( File uploadDir, boolean showProgress ) {
        super();
        this.uploadDir = uploadDir;
        this.showProgress = showProgress;
    }


    public void init( IFormFieldSite _site ) {
        this.site = _site;
    }


    public void dispose() {
        upload.dispose();
    }


    public Control createControl( final Composite parent, IFormEditorToolkit toolkit ) {
        Composite fileSelectionArea = toolkit.createComposite( parent, SWT.NONE );
        FormLayout layout = new FormLayout();
        layout.spacing = 5;
        fileSelectionArea.setLayout( layout );
        int style = Upload.SHOW_UPLOAD_BUTTON;
        if (showProgress) {
            style |= Upload.SHOW_PROGRESS;
        }
        upload = toolkit.createUpload( fileSelectionArea, SWT.BORDER, style );
        upload.setBrowseButtonText( "Datei..." );
        upload.setUploadButtonText( "Laden" );
        upload.setEnabled( enabled );
        upload.setBackground( enabled ? FormEditorToolkit.textBackground
                : FormEditorToolkit.textBackgroundDisabled );
        FormData data = new FormData();
        data.left = new FormAttachment( 0 );
        data.right = new FormAttachment( 100 );
        upload.setLayoutData( data );

//        viewImageButton = toolkit.createButton( fileSelectionArea, "Anzeigen", SWT.NONE );
//        data = new FormData();
//        data.left = new FormAttachment( 80 );
//        data.right = new FormAttachment( 100 );
//        viewImageButton.setLayoutData( data );
//        enableViewButton( enabled );

        final Shell shell = parent.getShell();

//        viewImageButton.addSelectionListener( new SelectionListener() {
//
//            @Override
//            public void widgetSelected( SelectionEvent e ) {
//                String url = DownloadServiceHandler.registerContent( new ContentProvider() {
//
//                    @Override
//                    public String getFilename() {
//                        return uploadedValue.originalFileName();
//                    }
//
//
//                    @Override
//                    public String getContentType() {
//                        return uploadedValue.contentType();
//                    }
//
//
//                    @Override
//                    public InputStream getInputStream()
//                            throws Exception {
//                        return new FileInputStream( new File( uploadDir, uploadedValue
//                                .internalFileName() ) );
//                    }
//
//
//                    @Override
//                    public boolean done( boolean success ) {
//                        return true;
//                    }
//                } );
//                ExternalBrowser.open( "download_window", url, ExternalBrowser.NAVIGATION_BAR
//                        | ExternalBrowser.STATUS );
//
//                // Browser browser = new Browser( shell, SWT.NONE );
//                // // create the image
//                // BufferedImage image = createImage();
//                // // store the image in the SessionStore for the service handler
//                // RWT.getSessionStore().setAttribute( IMAGE_KEY, image );
//                // create the HTML with a single <img> tag.
//                // browser.setText( "<img src=\"" + url + "\"/>" );
//
//                // // open a dialog with the image preview
//                // final MessageDialog dialog = new MessageDialog( PolymapWorkbench
//                // .getShellToParentOn(), uploadedValue.originalFileName(), null, "",
//                // MessageDialog.INFORMATION, new String[] { "Schlieï¿½en" }, 0 );
//                // dialog.setBlockOnOpen( true );
//                // dialog.open();
//            }
//
//
//            @Override
//            public void widgetDefaultSelected( SelectionEvent e ) {
//            }
//        } );

        // uploadlistener
        upload.addUploadListener( new UploadListener() {

            @Override
            public void uploadInProgress( UploadEvent uploadEvent ) {
                // TODO show a progress monitor here
            }


            @Override
            public void uploadFinished( UploadEvent uploadEvent ) {
                UploadItem item = upload.getUploadItem();

                try {
                    log.info( "Uploaded: " + item.getFileName() + ", path=" + item.getFilePath() );

                    // check for images
                    String contentType = item.getContentType();
                    if (!("image/jpeg".equalsIgnoreCase( contentType )
                            || "image/png".equalsIgnoreCase( contentType ) || "image/gif"
                            .equalsIgnoreCase( contentType ))) {
                        new MessageDialog(
                                shell,
                                "Fehler beim Upload der Daten",
                                null,
                                "Es können nur Bilder vom Typ JPG, GIF oder PNG hochgeladen werden.",
                                MessageDialog.ERROR, new String[] { "Ok" }, 0 ).open();
                        return;
                    }

                    // dont use the filename here
                    String fileName = item.getFileName();
                    int index = fileName.lastIndexOf( '.' );
                    String extension = ".jpg";
                    if (index != -1) {
                        extension = fileName.substring( index );
                    }

                    String id = System.currentTimeMillis() + "";
                    String internalFileName = id + extension;
                    File dbFile = new File( uploadDir, internalFileName );
                    FileOutputStream out = new FileOutputStream( dbFile );
                    StreamUtils.copyThenClose( item.getFileInputStream(), out );
                    log.info( "### copied to: " + dbFile );

                    // create a thumbnail
                    Image image = Graphics
                            .getImage( internalFileName, new FileInputStream( dbFile ) );
                    int x = image.getBounds().width;
                    int newX = x;
                    int y = image.getBounds().height;
                    int newY = y;

                    if (x >= y && x > thumbnailSize) {
                        newX = thumbnailSize;
                        newY = y * thumbnailSize / x;
                    }
                    else if (y > x && y > thumbnailSize) {
                        newY = thumbnailSize;
                        newX = x * thumbnailSize / y;
                    }
                    // else no need to scale
                    String thumbnailFileName = id + "_thumb" + extension;
                    dbFile = new File( uploadDir, thumbnailFileName );
                    out = new FileOutputStream( dbFile );
                    ImageLoader loader = new ImageLoader();
                    loader.data = new ImageData[] { image.getImageData().scaledTo( newX, newY ) };
                    loader.save( out, SWT.IMAGE_COPY );
                    log.info( "### thumbnail copied to: " + dbFile );

                    // image.dispose();

                    uploadedValue = new DefaultUploadedImage( fileName, item.getFilePath(),
                            contentType, internalFileName, thumbnailFileName, dbFile.length() );

//                    enableViewButton( true );
                }
                catch (IOException e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, UploadFormField.this,
                            "Fehler beim Upload der Daten.", e );
                }

                site.fireEvent( UploadFormField.this, IFormFieldListener.VALUE_CHANGE,
                        uploadedValue );
            }
        } );
        // focus listener
        upload.addFocusListener( new FocusListener() {

            @Override
            public void focusLost( FocusEvent event ) {
                upload.setBackground( FormEditorToolkit.textBackground );
                site.fireEvent( UploadFormField.this, IFormFieldListener.FOCUS_LOST, null );
            }


            @Override
            public void focusGained( FocusEvent event ) {
                upload.setBackground( FormEditorToolkit.textBackgroundFocused );
                site.fireEvent( UploadFormField.this, IFormFieldListener.FOCUS_GAINED, null );
            }
        } );
        return fileSelectionArea;
    }


    public IFormField setEnabled( boolean enabled ) {
        this.enabled = enabled;
        if (upload != null) {
            upload.setEnabled( enabled );
            upload.setBackground( enabled ? FormEditorToolkit.textBackground
                    : FormEditorToolkit.textBackgroundDisabled );

//            enableViewButton( enabled );
        }
        return this;
    }


//    /**
//     * 
//     * @param enabled
//     */
//    private void enableViewButton( boolean enabled ) {
//        enabled = uploadedValue != null && enabled;
//        viewImageButton.setEnabled( enabled );
//        viewImageButton.setBackground( enabled ? FormEditorToolkit.textBackground
//                : FormEditorToolkit.textBackgroundDisabled );
//    }


    public IFormField setValue( Object value ) {
        uploadedValue = (UploadedImage)value;

        // TODO add decorator or label with the last uploaded file and a link to the
        // image here
//        enableViewButton( true );

        // the above calls does not seem to fire events
        site.fireEvent( UploadFormField.this, IFormFieldListener.VALUE_CHANGE, uploadedValue );
        return this;
    }


    public void load()
            throws Exception {
        if (upload != null) {

            if (site.getFieldValue() instanceof UploadedImage) {
                uploadedValue = (UploadedImage)site.getFieldValue();
            }
        }
    }


    public void store()
            throws Exception {
        site.setFieldValue( uploadedValue );
    }


    public interface UploadedImage {

        String contentType();


        String originalFileName();


        String originalFilePath();


        Long fileSize();


        String internalFileName();
        
        String thumbnailFileName();
    }


    public static class DefaultUploadedImage
            implements UploadedImage {

        private final String fileName;

        private final String filePath;

        private final String contentType;

        private final String internalFileName;

        private final String thumbnailFileName;

        private final Long   fileSize;


        public DefaultUploadedImage( String fileName, String filePath, String contentType,
                String internalFileName, String thumbnailFileName, Long fileSize ) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.contentType = contentType;
            this.internalFileName = internalFileName;
            this.fileSize = fileSize;
            this.thumbnailFileName = thumbnailFileName;
        }


        @Override
        public String contentType() {
            return contentType;
        }


        @Override
        public String originalFileName() {
            return fileName;
        }


        @Override
        public String originalFilePath() {
            return filePath;
        }


        @Override
        public Long fileSize() {
            return fileSize;
        }


        @Override
        public String internalFileName() {
            return internalFileName;
        }

        @Override
        public String thumbnailFileName() {
            return thumbnailFileName;
        }

        @Override
        public boolean equals( Object obj ) {
            if (UploadedImage.class.isAssignableFrom( obj.getClass() )) {
                String fileName = originalFileName();
                String otherName = ((UploadedImage)obj).originalFileName();
                return (fileName == null && otherName == null)
                        || (fileName != null && fileName.equals( otherName ));
            }
            return false;
        }
    }
}
