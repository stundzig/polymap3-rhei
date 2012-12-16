/* 
 * polymap.org
 * Copyright 2010-2012, Falko Bräutigam. All rights reserved.
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
package org.polymap.rhei.field;

import java.util.EventListener;

import org.polymap.core.runtime.event.EventHandler;

/**
 * Implement this to get notified about changes of an {@link IFormField}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IFormFieldListener
        extends EventListener {

    public static final int     VALUE_CHANGE = 1;
    
    public static final int     FOCUS_GAINED = 2;
    
    public static final int     FOCUS_LOST = 3;
    

    @EventHandler(display=true)
    public void fieldChange( FormFieldEvent ev );
    
}
