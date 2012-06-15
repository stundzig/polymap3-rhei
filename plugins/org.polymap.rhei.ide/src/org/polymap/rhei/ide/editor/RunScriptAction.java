package org.polymap.rhei.ide.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.polymap.rhei.ide.Messages;
import org.polymap.rhei.ide.RheiIdePlugin;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class RunScriptAction
        extends Action {

    private final ScriptEditor          editor;
    

    public RunScriptAction(ScriptEditor scriptEditor) {
        super( Messages.get( "ScriptEditor_run" ) );
        editor = scriptEditor;
        setImageDescriptor( ImageDescriptor.createFromURL( 
                RheiIdePlugin.getDefault().getBundle().getResource( "icons/etool16/run.gif" ) ) );
        setToolTipText( Messages.get( "ScriptEditor_runTip" ) );
    }

    public void run() {
        ScriptEditor.log.debug( "runAction.run(): ..." );
        //            doSave( new NullProgressMonitor() );

        throw new RuntimeException( "not yet implemented" );
        
//        ICalculator calculator = CalculatorSupport.instance().newCalculator( editor.text.getText(), editor.input.getLang() );
//        if (calculator == null) {
//            PolymapWorkbench.handleError( RheiIdePlugin.PLUGIN_ID, editor, 
//                    "No such calculator support for language: " + editor.input.getLang(), new Exception() );
//            return;
//        }
//
//        ConsoleView console = ConsoleView.open();
//        console.clear();
//        console.getOut().println( "*** Script startet at " + new Date() + " ..." );
//        calculator.setOut( console.getOut() );
//        calculator.setErr( console.getErr() );
//
//        for (Map.Entry<String,Object> entry : editor.calculatorParams.entrySet()) {
//            calculator.setParam( entry.getKey(), entry.getValue() );
//        }
//
//        try {
//            calculator.eval();
//        }
//        catch (Exception e) {
//            ScriptEditor.log.debug( "Script eval error: ", e );
//            e.printStackTrace( console.getErr() );
//        }
    }

}