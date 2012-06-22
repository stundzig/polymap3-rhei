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

import java.io.Reader;

import org.apache.commons.io.IOUtils;
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
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.JavadocContentAccess;

import org.polymap.core.runtime.Polymap;
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

    private Browser             browser;
    
    private ISelectionListener  selectionListener;
    

    public JavadocView2() {
    }


    public void init( IViewSite site, IMemento memento )
            throws PartInitException {
        super.init( site, memento );
        
        selectionListener = new ISelectionListener() {
            public void selectionChanged( IWorkbenchPart part, ISelection sel ) {
                log.info( "selection: " + sel + " on: " + part );
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
                Reader reader = JavadocContentAccess.getHTMLContentReader( member, true, true );
                String html = reader != null ? IOUtils.toString( reader ) : "";

                final String pageHtml = createPageHtml( html, member );

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
        
        protected String createPageHtml( String docHtml, IMember member ) {
            String icon = "unknown_obj.gif";
            if (member instanceof IType) {
                icon = "class_obj.gif";
            }
            else if (member instanceof IMethod) {
                icon = "methpub_obj.gif";
            }
            else if (member instanceof IField) {
                icon = "field_public_obj.gif";
            }

            StringBuilder buf = new StringBuilder( 4*1024 );
            buf.append( "<head>\n" );
            buf.append( "<style TYPE=\"text/css\">\n" );
            buf.append( "  body {margin:10px; font-family:\"Segoe UI\",Tahoma,sans-serif;}\n" );
            buf.append( "  dt {font-weight:bold;}\n" );
            buf.append( "  code {border-bottom:1px dotted black;}\n" );
            buf.append( "  pre {margin:0px 0px;}\n" );
            buf.append( "  h3 {vertical-align:middle; font-size:1.0em;}\n" );
            buf.append( "  img {vertical-align:middle; margin-right:5px;}\n" );
            buf.append( "</style>\n" );
            buf.append( "</head>\n" );
            buf.append( "<body>" );
            buf.append( "  <h3><img src=\"../../rhei-ide-icons/obj16/").append( icon ).append( "\">" );
            try {
                if (member instanceof IMethod) {
                    IMethod m = (IMethod)member;
                    buf.append( Signature.getSignatureSimpleName( m.getReturnType() ) ).append( " " );
                    buf.append( m.getElementName() ).append( "(" );
                    
                    String[] paramTypes = m.getParameterTypes();
                    String[] paramNames = m.getParameterNames();
                    
                    for (int i=0; i<paramTypes.length; i++) {
                        buf.append( Signature.getSignatureSimpleName( paramTypes[i] ) );
                        buf.append( " " ).append( paramNames[i] );
                        buf.append( i+1 < paramTypes.length ? ", " : "" );                        
                    }
                    buf.append( ")" );
                } 
                else if (member instanceof IField ) {
                    buf.append( Signature.toString( ((IField)member).getTypeSignature() ) );
                    buf.append( " " ).append( member.getElementName() );
                }
                else if (member instanceof IType) {
                    buf.append( ((IType)member).getFullyQualifiedName( '.' ) );
                }
                else {
                    buf.append( member.getElementName() );
                }
            }
            catch (Exception e) {
                log.warn( "", e );
                buf.append( member.getElementName() );
            }
            buf.append( "  </h3>" );
//            buf.append( "  <h4>" );
//            buf.append(      member.get
//            buf.append( "  </h4>" );
            buf.append(    docHtml );
            buf.append( "</body>" );
            return buf.toString();
        }
        
    }
    
}
