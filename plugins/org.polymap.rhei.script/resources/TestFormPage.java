import org.polymap.rhei.form.*;
import org.geotools.data.*;
import org.opengis.feature.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Label;

//Ausgabe aller Variabler für Testzwecke
for (int i=0; i<this.variables.length; i++) {
    System.out.println( "    variable: " + this.variables[i] );
}
System.out.println( "feature: " + _feature );
System.out.println( "fs: " + _fs );

public class TestFormPage
        extends DefaultFormEditorPage {

    public TestFormPage() {
        super( "__beanshell__", "BeanShell!", _feature, _fs );
    }

    public void createFormContent( IFormEditorPageSite site ) {
        site.setFormTitle( feature.getIdentifier().getID() );
        site.getPageBody().setLayout( new FormLayout() );

        Label l = new Label( site.getPageBody(), SWT.NONE );
        l.setText( "Hello from BeanShell!" );
        //Composite field = site.newFormFiel( null, "", new TextFormField(), null );
        //field.setLayoutData( layoutData );
    }

    public void finalize() {
        System.out.println( "TestFormPage: FINALIZED!");
    }
}

IFormEditorPage result = new TestFormPage();