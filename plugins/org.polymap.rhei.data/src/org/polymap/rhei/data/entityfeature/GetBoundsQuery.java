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
package org.polymap.rhei.data.entityfeature;

import org.qi4j.api.query.grammar.NamedQueryExpression;

/**
 * Special query that retrieves the geometry bounds of entities with Geometry
 * attribute. The result contains 4 entities that represent minX, maxX, minY, maxY of
 * the bounding box.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GetBoundsQuery
        implements NamedQueryExpression {

    private String              geomName;
    
    
    public GetBoundsQuery( String geomName ) {
        this.geomName = geomName;
    }

    public String getGeomName() {
        return geomName;
    }

    @Override
    public boolean eval( Object target ) {
        return true;
    }

    @Override
    public String name() {
        return "GetBounds";
    }
    
}
