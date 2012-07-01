package forms;

import org.polymap.rhei.form.*;
import org.polymap.rhei.field.*;
import org.geotools.data.*;
import org.opengis.feature.*;

import org.eclipse.swt.*;
//import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.*;

import org.polymap.rhei.script.java.*;

/**
 * Das ist ein Beispiel für ein Formular. 
 */ 
public class TestFormPage 
        extends ScriptedFormEditorPage {

    /** Möglihe Werte für das Status-Feld. */
    public static final String[]    STATUS = new String[] {"genehmigt", "errichtet", "beantragt" };
    
        
    public TestFormPage() {
        // ID und Titel dieses Formulars setzen; 
        // der Titel wird im Reiter unterhalb des Formulars angezeigt
        super( "__wea__", "WEA-Basisdaten" );
    }


    /**
     * Prüfen ob dieses Formular mit dem übergebenen Feature arbeiten kann.
     */
    public boolean wantsToBeShown() {
        return featureNameContains( new String[] {"genehmigt", "polymap"}, true, true );
    }


    /**
     * Anlegen des Formulars mit allen Feldern.
     */
    public void createFormContent( IFormEditorPageSite site ) {
        super.createFormContent( site ); 

        FeatureStore fs;
        Feature f;
        
        // den Titel des gesamten Formulareditors setzen; 
        // dieser Titel wird oberhalb des Formulars angezeigt
        site.setFormTitle( "WEA (" + feature.getIdentifier().getID() + ")" );

        // den Bereich für die Basisdaten anlegen; 
        Section section1 = newSection( "Basisdaten", false, null );
        newFormField( "WEA_NR" )
                .setParent( section1 )
                .setLabel( "Nummer" ).create();
        
        newFormField( "LAGE" )
                .setParent( section1 )
                .setLabel( "Lage" ).create();
        
        newFormField( "ANTR_STELL" )
                .setParent( section1 )
                .setLabel( "Antragsteller" ).create();
        
        newFormField( "ANTR_DAT" )
                .setParent( section1 )
                .setLabel( "gestellt am" )
                .setField( new DateTimeFormField() ).create();
        
        newFormField( "STATUS" )
                .setParent( section1 )
                .setLabel( "Status" )
                .setField( new PicklistFormField( STATUS, new int[0] ) )
                .create();

        // den Bereich für Technische Daten;
        // etwas kompaktere Schreibweise
        Section section2 = newSection( "Technische Daten", true, section1 );
        newFormField( "NAB_HOEHE" ).setParent( section2 ).setLabel( "Nabenhöhe (m)" ).create();
        newFormField( "ROTOR" ).setParent( section2 ).setLabel( "Durchmesser (m)" ).create();
    }


    public void finalize() {
        System.out.println( "TestFormPage: FINALIZED!");
    }
}
