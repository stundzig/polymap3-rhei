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
package org.polymap.rhei.ide.cheatsheets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.cheatsheets.CheatSheetListener;
import org.eclipse.ui.cheatsheets.ICheatSheetEvent;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheet;
import org.eclipse.ui.internal.cheatsheets.data.ICheatSheet;
import org.eclipse.ui.internal.cheatsheets.data.Item;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetManager;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.workbench.PolymapWorkbench;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings("restriction")
public class NewFormCheatSheet
        extends CheatSheet
        implements ICheatSheet, CheatSheetListener {

    private static Log log = LogFactory.getLog( NewFormCheatSheet.class );

    private String      scriptName;
    
    
    public NewFormCheatSheet() {
        super();
        setTitle( "Neues Formular anlegen" );
        
        // intro
        Item intro = new Item( 
                "Überblick", 
                "Anlegen eines neues Scriptes für ein Formular.", 
                "href", "contextid", false, false );
//        intro.setExecutable( new SimpleAction() {
//            public IStatus execute( CheatSheetManager csm ) {
//                log.info( "Intro execute(): manager=" + csm );
//                return Status.OK_STATUS;
//            }
//        });
        setIntroItem( intro );
        
        //
        Item item1 = new Item( "item1", "<ul><li>prima</li></ul>", "href", "contextid", false, false );
        item1.setExecutable( new SimpleAction() {
            public IStatus execute( CheatSheetManager csm ) {
                log.info( "Action execute(): manager=" + csm );
                InputDialog dialog = new InputDialog( PolymapWorkbench.getShellToParentOn(), "Name des Scripts",
                        "Geben Sie de Namen des neuen Scripts an. Die Endung muss zum Typ des Scriptes passen. Möglich sind: *.java und *.js.",
                        "NeuesFormular.java", new IInputValidator() {
                            
                            public String isValid( String newText ) {
                                return newText;
                            }
                        });
                dialog.setBlockOnOpen( true );
                if (dialog.open() == Window.OK) {
                    scriptName = dialog.getValue();
                }
                return Status.OK_STATUS;
            }
        });
        addItem( item1 );
        
        addItem( new Item( "item2", "<p>Jetzt mal im Paragraph</p>", "href", "contextid", false, false ) );
    }


    public void cheatSheetEvent( ICheatSheetEvent ev ) {
        String typeName = "-";
        switch (ev.getEventType()) {
            case ICheatSheetEvent.CHEATSHEET_OPENED: typeName = "cs-opened"; break;
            case ICheatSheetEvent.CHEATSHEET_CLOSED: typeName = "cs-closed"; break;
            case ICheatSheetEvent.CHEATSHEET_COMPLETED: typeName = "cs-completed"; break;
            case ICheatSheetEvent.CHEATSHEET_STARTED: typeName = "cs-started"; break;
            case ICheatSheetEvent.CHEATSHEET_RESTORED: typeName = "cs-restored"; break;
            case ICheatSheetEvent.CHEATSHEET_RESTARTED: typeName = "cs-restarted"; break;
        }
        log.info( "cheatsheet: " + ev.getCheatSheetID() + " " + typeName );
    }
    
}
