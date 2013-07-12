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
package org.polymap.rhei.script.extensions;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.OutputStreamWriter;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import bsh.EvalError;
import bsh.TargetError;

import org.eclipse.core.resources.IFile;

import org.polymap.rhei.script.java.ScriptParams;

/**
 * 
 * @param <T> The type of the delegate the interpreter provides.
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ScriptRunner<T> {

    private static Log log = LogFactory.getLog( ScriptRunner.class );
    
    private static final ScriptEngineManager    manager = new ScriptEngineManager();
    
    public static final Pattern                 javaClassPattern = Pattern.compile( "class ([a-zA-Z0-9_]+)" );

    protected IFile                       src;
    
    protected Map<String,Object>          params;
    
    protected T                           delegate;


    public ScriptRunner( IFile src, Map<String,Object> params ) throws Exception {
        this.src = src;
        this.params = params;
        evalScript();
    }

    
    protected void evalScript() throws Exception {
        String code = IOUtils.toString( src.getContents(), src.getCharset() );
    
        code = tweakCode( code, src.getFileExtension() );
    
        ScriptEngine engine = manager.getEngineByExtension( src.getFileExtension() );
        if (engine != null) {
            engine.getContext().setWriter( new OutputStreamWriter( System.out, "UTF-8" ) );
            engine.getContext().setErrorWriter( new OutputStreamWriter( System.err, "UTF-8" ) );
    
            ScriptParams scriptParams = ScriptParams.init();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                engine.put( "_" + param.getKey(), param.getValue() );
                scriptParams.put( param.getKey(), param.getValue() );
            }
    
            try {
                engine.eval( code );
    
                // XXX make this independent of the script language
                //WantsToBeShown scripted = ((Invocable)engine).getInterface( WantsToBeShown.class );
                delegate  = (T)engine.getContext().getAttribute( "result" );
            }
            // XXX Beanshell should not throw custom exceptions but ScriptExceptions
            catch (Exception e) {
                if (e instanceof EvalError) {
                    EvalError ee = (EvalError)e;                    
                    ScriptException se = new ScriptException( ee.getMessage(), ee.getErrorSourceFile(), ee.getErrorLineNumber() );
                    
                    if (e instanceof TargetError) {
                        StackTraceElement[] st = ((TargetError)e).getTarget().getStackTrace();
                        se.setStackTrace( st );
                    }
                    throw se;
                }
                else {
                    throw e;
                }
            }
            finally {
                ScriptParams.dispose();
            }
        }
        else {
            log.warn( "No ScriptEngine for extension: " + src.getFileExtension() );
        }
    }


    /**
     * 
     */
    protected String tweakCode( String code, String codeType ) {
        String result = code;
        // java: add result bottom line
        if (codeType.equals( "java" )) {
            Matcher matcher = javaClassPattern.matcher( code );
            if (matcher.find()) {
                String classname = matcher.group( 1 );
                result = code + "\nObject result = new " + classname + "();";
            }
        }
        return result;
    }

}
