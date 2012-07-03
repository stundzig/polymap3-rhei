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

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.filters.StringInputStream;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.CheatSheetListener;
import org.eclipse.ui.cheatsheets.ICheatSheetEvent;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.cheatsheets.data.CheatSheet;
import org.eclipse.ui.internal.cheatsheets.data.ICheatSheet;
import org.eclipse.ui.internal.cheatsheets.data.Item;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.MessagesImpl;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.ide.RheiIdePlugin;
import org.polymap.rhei.script.RheiScriptPlugin;

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

    private MessagesImpl    messages = new MessagesImpl( 
            NewFormCheatSheet.class.getPackage().getName() + ".NewForm",
            NewFormCheatSheet.class.getClassLoader() );

    protected String i18n( String key, Object... args ) {
        return messages.get( key, args );    
    }
    
    
    // instance *******************************************
    
    private String          scriptName;
    
    
    public NewFormCheatSheet() {
        super();
        setTitle( i18n( "title" ) );
        
        // intro
        setIntroItem( new Item( 
                i18n( "intro_title" ), i18n( "intro_description" ),
                null, null, false, false ) );
        // items
        addItem( createNameItem() );
        addItem( createCtorItem() );
        addItem( createShownItem() );
        addItem( createFieldsItem() );
        addItem( createFields2Item() );
    }
    

    /**
     * nameItem: name of script and class
     */
    protected Item createNameItem() {
        Item item1 = new Item( 
                i18n( "nameItem_title" ), i18n( "nameItem_description" ),
                null, null, false, false );
        
        item1.setExecutable( new SimpleAction() {
            public IStatus execute( CheatSheetManager csm ) {
                // get script name from user
                InputDialog dialog = new InputDialog( PolymapWorkbench.getShellToParentOn(), 
                        i18n( "nameItem_dialogTitle" ), i18n( "nameItem_dialogMsg" ),
                        "NewFormPage", new IInputValidator() {
                            public String isValid( String text ) {
                                if (!StringUtils.isAlphanumeric( text )) {
                                    return "Name must contain only: [A-Za-z_]";
                                }
                                else if (Character.isDigit( text.charAt( 0 ) )) {
                                    return "Name must not start with a digit";
                                }
                                return null;
                            }
                        });
                dialog.setBlockOnOpen( true );
                
                // create new script
                if (dialog.open() == Window.OK) {
                    InputStream in = null;
                    try {
                        // read code
                        scriptName = dialog.getValue();
                        String srcResName = i18n( "nameItem_srcRes" );
                        URL srcRes = RheiIdePlugin.getDefault().getBundle().getResource( srcResName );
                        String code = IOUtils.toString( srcRes.openStream(), "ISO-8859-1" );
                        
                        // replace name
                        code = StringUtils.replace( code, "NewFormPage", scriptName );
                        
                        // create file in project
                        IProject project = RheiScriptPlugin.getOrCreateScriptProject();
                        IFile file = project.getFile( "src/forms/" + scriptName + ".java" );
                        if (file.exists()) {
                            throw new IllegalStateException( "Dieses Script existiert bereits: " + file );                            
                        }
                        file.create( new StringInputStream( code, "ISO-8859-1" ), 0, null );

                        // open editor
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        IDE.openEditor( page, file );
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                    finally {
                        IOUtils.closeQuietly( in );
                    }
                }
                return Status.OK_STATUS;
            }
        });
        return item1;
    }


    /**
     * ctorItem: name of script and class
     */
    protected Item createCtorItem() {
        Item item = new Item( 
                i18n( "ctorItem_title" ), i18n( "ctorItem_description" ),
                null, null, false, false );
        return item;
    }

    
    /**
     * fieldsItem: name of script and class
     */
    protected Item createFieldsItem() {
        Item item = new Item( 
                i18n( "fieldsItem_title" ), i18n( "fieldsItem_description" ),
                null, null, false, false );
        return item;
    }


    /**
     * fields2Item: field and validators
     */
    protected Item createFields2Item() {
        Item item = new Item( 
                i18n( "fields2Item_title" ), i18n( "fields2Item_description" ),
                null, null, false, false );
        return item;
    }

    
    /**
     * shownItem: name of script and class
     */
    protected Item createShownItem() {
        Item item = new Item( 
                i18n( "shownItem_title" ), i18n( "shownItem_description" ),
                null, null, false, false );
        return item;
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
