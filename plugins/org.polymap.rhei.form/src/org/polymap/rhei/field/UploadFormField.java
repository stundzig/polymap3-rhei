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

import java.util.Date;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.lf5.util.StreamUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.rwt.widgets.Upload;
import org.eclipse.rwt.widgets.UploadEvent;
import org.eclipse.rwt.widgets.UploadItem;
import org.eclipse.rwt.widgets.UploadListener;

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

    private static Log     log     = LogFactory.getLog( UploadFormField.class );

    private IFormFieldSite site;

    private Upload         upload;

    private boolean        enabled = true;

    private UploadedImage  uploadedValue;

    /** Special value representing a "null" as the propety value. */
    private Date           nullValue;

    private final File     uploadDir;


    public UploadFormField( File uploadDir ) {
        super();
        this.uploadDir = uploadDir;
    }


    public void init( IFormFieldSite _site ) {
        this.site = _site;
    }


    public void dispose() {
        upload.dispose();
    }


    public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
         Composite fileSelectionArea = toolkit.createComposite( parent, SWT.NONE );
         FormLayout layout = new FormLayout();
         layout.spacing = 5;
         fileSelectionArea.setLayout( layout );

        upload = new Upload( fileSelectionArea, SWT.BORDER , Upload.SHOW_PROGRESS | Upload.SHOW_UPLOAD_BUTTON );
        upload.setBrowseButtonText( "Browse" );
        upload.setUploadButtonText( "Upload" );
        upload.setEnabled( enabled );
        upload.setBackground( enabled ? FormEditorToolkit.textBackground
                : FormEditorToolkit.textBackgroundDisabled );
        FormData data = new FormData();
        data.left = new FormAttachment( 0 );
        data.right = new FormAttachment( 100 );
        upload.setLayoutData( data );

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

                    // dont use the filename here
                    String fileName = item.getFileName();
                    int index = fileName.lastIndexOf( '.' );
                    String extension = ".jpg";
                    if (index != -1) {
                        extension = fileName.substring( index );
                    }

                    String internalFileName = System.currentTimeMillis() + extension;
                    File dbFile = new File( uploadDir, internalFileName );
                    FileOutputStream out = new FileOutputStream( dbFile );
                    StreamUtils.copyThenClose( item.getFileInputStream(), out );
                    log.info( "### copied to: " + dbFile );

                    uploadedValue = new UploadedImageImpl( fileName, item.getFilePath(), item
                            .getContentType(), internalFileName, dbFile.length() );

                }
                catch (IOException e) {
                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, UploadFormField.this,
                            "Fehler beim Upload der Daten.", e );
                }

                site.fireEvent( UploadFormField.this, IFormFieldListener.VALUE_CHANGE,
                        uploadedValue == null ? null : uploadedValue );
            }
        } );
        // focus listener
        upload.addFocusListener( new FocusListener() {

            public void focusLost( FocusEvent event ) {
                upload.setBackground( FormEditorToolkit.textBackground );
                site.fireEvent( UploadFormField.this, IFormFieldListener.FOCUS_LOST, null );
            }


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
        }
        return this;
    }


    public IFormField setValue( Object value ) {
        UploadedImage image = (UploadedImage)value;

        // TODO add decorator or label with the last uploaded file and a link to the
        // image here

        // the above calls does not seem to fire events
        site.fireEvent( UploadFormField.this, IFormFieldListener.VALUE_CHANGE, image != null
                && !image.equals( uploadedValue ) ? null : image );
        return this;
    }


    public void load()
            throws Exception {
        if (upload != null) {

            if (site.getFieldValue() == null) {
                uploadedValue = null;
                setValue( nullValue );

                // // from the source of DateTime.setDate()
                // datetime.setDate( 9996, 0, 1 );
                // datetime.setTime( 0, 0, 0 );
            }
            else if (site.getFieldValue() instanceof UploadedImage) {
                uploadedValue = (UploadedImage)site.getFieldValue();
            }
            else {
                log.warn( "Unknown value type: " + site.getFieldValue() );
            }
        }
    }


    public void store()
            throws Exception {
        if (uploadedValue != null) {
            site.setFieldValue( uploadedValue );
        }
    }


    public interface UploadedImage {

        String contentType();


        String originalFileName();


        String originalFilePath();


        Long fileSize();


        String internalFileName();
    }


    public static class UploadedImageImpl
            implements UploadedImage {

        private final String fileName;

        private final String filePath;

        private final String contentType;

        private final String internalFileName;

        private final Long   fileSize;


        public UploadedImageImpl( String fileName, String filePath, String contentType,
                String internalFileName, Long fileSize ) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.contentType = contentType;
            this.internalFileName = internalFileName;
            this.fileSize = fileSize;
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
    }
}
