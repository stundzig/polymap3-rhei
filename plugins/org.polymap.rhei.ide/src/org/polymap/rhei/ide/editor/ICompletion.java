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
package org.polymap.rhei.ide.editor;

/**
 * Represents a completion proposal.
 * <p/>
 * This interface keeps 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ICompletion
        extends org.eclipse.rwt.widgets.codemirror.ICompletion {

    public int getRelevance();
    
    // org.eclipse.rwt.widgets.codemirror.ICompletion *****
    
    public String getCompletion();
    
    public int getReplaceStart();
    
    public int getReplaceEnd();
    
}
