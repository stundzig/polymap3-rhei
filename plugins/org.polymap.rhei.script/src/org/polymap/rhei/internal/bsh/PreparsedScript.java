/*****************************************************************************
 *                                                                           *
 *  This is BeanShell2, a fork of beanshell found at                         *
 *        http://www.beanshell.org/                                          *
 *                                                                           *
 *  BeanShell2 is hosted at Google-Code at                                   *
 *        http://code.google.com/p/beanshell2                                *
 *                                                                           *
 *  BeanShell2 is licensed under GNU Lesser GPL v3                           *
 *  See http://www.gnu.org/licenses/lgpl.html                                *
 *                                                                           *
 *****************************************************************************/
/*
 * Taken from: http://code.google.com/p/beanshell2/source/browse/trunk/src/bsh/
 */
package org.polymap.rhei.internal.bsh;

import java.util.Map;

import java.io.PrintStream;
import java.io.StringReader;

import bsh.BshMethod;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.NameSpace;
import bsh.Primitive;
import bsh.This;
import bsh.UtilEvalError;
import bsh.classpath.ClassManagerImpl;

/**
 * With this class the script source is only parsed once and the resulting AST is
 * used for {@link #invoke(java.util.Map) every invocation}. This class is designed
 * to be thread-safe.
 */
public class PreparsedScript {

    private final BshMethod   _method;

    private final Interpreter _interpreter;


    public PreparsedScript( final String source )
            throws EvalError {
        this( source, getDefaultClassLoader() );
    }


    private static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (final SecurityException e) {
            // ignore
        }
        if (cl == null) {
            cl = PreparsedScript.class.getClassLoader();
        }
        if (cl != null) {
            return cl;
        }
        return ClassLoader.getSystemClassLoader();
    }


    public PreparsedScript( final String source, final ClassLoader classLoader )
            throws EvalError {
        final ClassManagerImpl classManager = new ClassManagerImpl();
        classManager.setClassLoader( classLoader );
        final NameSpace nameSpace = new NameSpace( classManager, "global" );
        _interpreter = new Interpreter( new StringReader( "" ), System.out, System.err, false,
                nameSpace, null, null );
        try {
            final This callable = (This)_interpreter.eval( "__execute() { " + source + "\n" + "}\n"
                    + "return this;" );
            _method = callable.getNameSpace().getMethod( "__execute", new Class[0], false );
        }
        catch (final UtilEvalError e) {
            throw new IllegalStateException( e );
        }
    }


    public Object invoke( final Map<String, ?> context )
            throws EvalError {
        final NameSpace nameSpace = new NameSpace( _interpreter.getClassManager(),
                "BeanshellExecutable" );
        nameSpace.setParent( _interpreter.getNameSpace() );
        final BshMethod method = new BshMethod( _method.getName(), _method.getReturnType(), _method
                .getParameterNames(), _method.getParameterTypes(), _method.methodBody, nameSpace,
                _method.getModifiers() );
        for (final Map.Entry<String, ?> entry : context.entrySet()) {
            try {
                nameSpace.setVariable( entry.getKey(), entry.getValue(), false );
            }
            catch (final UtilEvalError e) {
                throw new EvalError( "cannot set variable '" + entry.getKey() + '\'', null, null, e );
            }
        }
        final Object result = method.invoke( new Object[0], _interpreter );
        if (result instanceof Primitive) {
            if (((Primitive)result).getType() == Void.TYPE) {
                return null;
            }
            return ((Primitive)result).getValue();
        }
        return result;
    }


    public void setOut( final PrintStream value ) {
        _interpreter.setOut( value );
    }


    public void setErr( final PrintStream value ) {
        _interpreter.setErr( value );
    }

}
