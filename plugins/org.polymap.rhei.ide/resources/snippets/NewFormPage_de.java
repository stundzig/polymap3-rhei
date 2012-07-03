package forms;

import org.geotools.data.*;
import org.opengis.feature.*;
import org.eclipse.ui.forms.widgets.*;

import org.polymap.rhei.form.*;
import org.polymap.rhei.field.*;
import org.polymap.rhei.script.java.*;

/**
 * Das ist ein Beispiel für ein Formular. 
 */ 
public class NewFormPage 
        extends ScriptedFormEditorPage {

    public NewFormPage() {
        // ID und Titel dieses Formulars setzen; 
        // der Titel wird im Reiter unterhalb des Formulars angezeigt
        super( "__NewFormPage__", "NewFormPage" );
    }


    /**
     * Prüfen ob dieses Formular mit dem übergebenen Feature arbeiten kann.
     */
    public boolean wantsToBeShown() {
        return true;
        //return featureNameContains( new String[] {"Wind", "polymap"}, true, true );
    }


    /**
     * Anlegen des Formulars mit allen Feldern.
     */
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site ); 

        // den Titel des gesamten Formulareditors setzen; 
        // dieser Titel wird oberhalb des Formulars angezeigt
        site.setFormTitle( "NewFormPage (" + feature.getIdentifier().getID() + ")" );

        // den Bereich für die "Basisdaten" anlegen
        Section section1 = newSection( "Basisdaten", false, null );
        
        // eine neues Feld anlegen; implizit wird ein Text ohne Prüfung erzeugt
        newFormField( "Attributnamen_eintragen" )   // Attributname
                .setParent( section1 )              // Bereich: Basisdaten
                .setLabel( "Label" ).create();      // Bezeichner        
    }

}
