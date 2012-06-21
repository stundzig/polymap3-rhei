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
package org.polymap.rhei.script.java;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a helper for BeanShell scripts to get script params in a way the Java
 * compiler does not complain about. Use this if you want to develop the script in an
 * IDE, like the Rhei IDE, to avoid compiler error.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ScriptParams {

    private static ThreadLocal<ScriptParams>    threadLocal = new ThreadLocal();

    
    public static ScriptParams init() {
        assert threadLocal.get() == null;
        ScriptParams result = new ScriptParams();
        threadLocal.set( result );
        return result;
    }

    public static void dispose() {
        threadLocal.remove();
    }

    /**
     * Return the script parameter for the given name.
     * 
     * @param <T>
     * @param name The name of the parameter.
     * @return The value for the given name, or null if there is no such paramter.
     */
    public static <T> T get( String name ) {
        ScriptParams params = threadLocal.get();
        assert params != null: "No script params initialized for this thread.";
        return (T)params.params.get( name );
    }
    
    
    // instance *******************************************
    
    private Map<String,Object>      params = new HashMap();
    
    public ScriptParams put( String name, Object value ) {
        params.put( name, value );
        return this;
    }
    
}
