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
package org.polymap.rhei.ide.navigator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;

import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NewFileAction
        implements IViewActionDelegate, IObjectActionDelegate {

    private static Log log = LogFactory.getLog( NewFileAction.class );


    @Override
    public void init( IViewPart view ) {
    }


    @Override
    public void run( IAction action ) {
        ISelection sel = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
        
        if (sel instanceof IStructuredSelection) {
            BasicNewFileResourceWizard wizard = new BasicNewFileResourceWizard();
            wizard.init( PlatformUI.getWorkbench(), (IStructuredSelection)sel );
            wizard.setNeedsProgressMonitor( true );
            WizardDialog dialog = new WizardDialog( PolymapWorkbench.getShellToParentOn(), wizard );
            dialog.create();
            dialog.getShell().setText( wizard.getWindowTitle() );
            dialog.open();
        }
    }


    @Override
    public void selectionChanged( IAction action, ISelection sel ) {
        // get selection from selection service
    }


    @Override
    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }
    
}
