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
package org.polymap.rhei.ide.java;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.JavadocContentAccess;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.UIJob;

/**
 * This view shows to JavaDoc of the current selected {@link IMember}.
 * <p/>
 * This is inspired by the JavadocView of the jdt.ui package. But is might be
 * extended to show more detail of the elements.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class JavadocView2
        extends ViewPart {

    private static Log log = LogFactory.getLog( JavadocView2.class );

    public static final String  ID = "org.polymap.rhei.ide.JavadocView";
    
    public static final String  METHOD_ICON = "methpub_obj.gif";
    public static final String  CLASS_ICON = "class_obj.gif";
    public static final String  FIELD_ICON = "field_public_obj.gif";

    private Browser             browser;
    
    private ISelectionListener  selectionListener;
    

    public JavadocView2() {
    }


    public void init( IViewSite site, IMemento memento )
            throws PartInitException {
        super.init( site, memento );
        
        selectionListener = new ISelectionListener() {
            public void selectionChanged( IWorkbenchPart part, ISelection sel ) {
                log.debug( "selection: " + sel + " on: " + part );
                if (sel instanceof IStructuredSelection) {
                    for (Object elm : ((IStructuredSelection)sel).toList()) {
                        UpdateContentJob job = new UpdateContentJob( elm );
                        if (job.canRun()) {
                            job.schedule();
                            break;  // show just the first found IMember
                        }
                    }
                }
            }
        };
    }


    public void createPartControl( Composite parent ) {
        browser = new Browser( parent, SWT.NONE );

        Display display = getSite().getShell().getDisplay();
        browser.setForeground( display.getSystemColor( SWT.COLOR_INFO_FOREGROUND ) );
        browser.setBackground( display.getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );

        getSite().getPage().addSelectionListener( selectionListener );
    }


    public void dispose() {
        if (selectionListener != null) {
            getSite().getPage().removeSelectionListener( selectionListener );
            selectionListener = null;
        }
        super.dispose();
    }


    public void setFocus() {
        getSite().getShell().setFocus();
    }


    /**
     * Updates the browser widget with the JavaDoc of the given selection element.
     * 
     * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
     */
    class UpdateContentJob
            extends UIJob {

        private IMember     member;
        
        
        /**
         * Finds the {@link IMember} for the given element.
         */
        public UpdateContentJob( Object elm ) {
            super( "JavaDoc" );
            setPriority( SHORT );

            // find IMember for selection
            if (elm instanceof IMember) {
                member = (IMember)elm;
            }
            else if (elm instanceof IFile) {
                IJavaElement javaElm = JavaCore.create( (IFile)elm );
                if (javaElm instanceof IMember) {
                    member = (IMember)javaElm;
                }
                else if (javaElm instanceof ICompilationUnit) {
                    member = ((ICompilationUnit)javaElm).findPrimaryType();
                }
            }
        }

        
        public boolean canRun() { return member != null; }
        
        
        /**
         * Updates the browser content. 
         */
        protected void runWithException( IProgressMonitor monitor )
                throws Exception {
            assert member != null;
            try {
                Timer timer = new Timer();
                final String pageHtml = createPageHtml();
                log.info( "Javadoc: " + pageHtml.length() + " bytes generated in " + timer.elapsedTime() + "ms" );

                Polymap.getSessionDisplay().asyncExec( new Runnable() {
                    public void run() {
                        browser.setText( pageHtml );
                    }
                });
            }
            catch (Exception e) {
                log.warn( e.getLocalizedMessage(), e );
            }
        }
        
        
        private String readJavadoc( IMember elm )
        throws JavaModelException, IOException {
            Reader reader = JavadocContentAccess.getHTMLContentReader( elm, true, true );
            return reader != null ? IOUtils.toString( reader ) : "";
        }
        
        
        protected String createPageHtml() {
            StringBuilder buf = new StringBuilder( 4*1024 );
            buf.append( "<head>\n" );
            buf.append( "<style TYPE=\"text/css\">\n" );
            buf.append( "  body {margin:10px; font-family:\"Segoe UI\",Tahoma,sans-serif; font-size:11px;}\n" );
            buf.append( "  dt {font-weight:bold;}\n" );
            buf.append( "  code {border-bottom:1px dotted black;}\n" );
            buf.append( "  pre {margin:0px 0px;}\n" );
            buf.append( "  h3 {vertical-align:middle; font-size:1.0em; /*text-shadow: 0px 1px 1px #bfbfbf;*/}\n" );
            buf.append( "  h2 {vertical-align:middle; font-size:1.2em;}\n" );
            buf.append( "  img {vertical-align:middle; margin-right:5px;}\n" );
            buf.append( "</style>\n" );
            buf.append( "</head>\n" );
            buf.append( "<body>" );
            try {
                if (member instanceof IMethod) {
                    createMethodHtml( buf, (IMethod)member, true );
                } 
                else if (member instanceof IField ) {
                    createFieldHtml( buf, (IField)member );
                }
                else if (member instanceof IType) {
                    createClassHtml( buf, (IType)member );
                }
                else {
                    buf.append( member.getElementName() );
                }
            }
            catch (Exception e) {
                log.warn( "", e );
                buf.append( member.getElementName() );
            }
            buf.append( "</body>" );
            return buf.toString();
        }
        
        
        protected void createClassHtml( StringBuilder buf, IType elm ) 
        throws JavaModelException, IOException {
            buf.append( "   <h3><img src=\"../../rhei-ide-icons/obj16/").append( CLASS_ICON ).append( "\">" );

            // signature
            buf.append( ((IType)member).getFullyQualifiedName( '.' ) );
            buf.append( "  </h3>" );
            
            // description
            buf.append( readJavadoc( elm ) );
            
            // methods
            buf.append( "<h2 style=\"margin-top:20px; border-bottom:1px solid #b4b4b4; color: #949494;\">Methods</h2>" );
            for (IMethod m : elm.getMethods()) {
                createMethodHtml( buf, m, false );
            }
        }
        

        protected void createMethodHtml( StringBuilder buf, IMethod m, boolean showParams ) 
        throws JavaModelException, IOException {
            buf.append( "   <h3><img src=\"../../rhei-ide-icons/obj16/").append( METHOD_ICON ).append( "\">" );

            // signature
            buf.append( Signature.getSignatureSimpleName( m.getReturnType() ) ).append( " " );
            buf.append( m.getElementName() ).append( "(" );

            String[] paramTypes = m.getParameterTypes();
            String[] paramNames = m.getParameterNames();

            for (int i=0; i<paramTypes.length; i++) {
                buf.append( Signature.getSignatureSimpleName( paramTypes[i] ) );
                buf.append( " " ).append( paramNames[i] );
                buf.append( i+1 < paramTypes.length ? ", " : "" );                        
            }
            buf.append( ")\n" );
            buf.append( "  </h3>" );
            String html = readJavadoc( m );
            buf.append( showParams ? html : StringUtils.substringBefore( html, "<dl><dt>" ) );
        }
        

        protected void createFieldHtml( StringBuilder buf, IField f ) 
        throws JavaModelException, IOException {
            buf.append( "   <h3><img src=\"../../rhei-ide-icons/obj16/").append( FIELD_ICON ).append( "\">" );

            buf.append( Signature.toString( f.getTypeSignature() ) );
            buf.append( " " ).append( f.getElementName() );
            buf.append( "  </h3>" );
            buf.append( readJavadoc( f ) );
        }
        
    }
    
}
