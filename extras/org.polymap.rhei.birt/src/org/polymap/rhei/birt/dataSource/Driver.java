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
package org.polymap.rhei.birt.dataSource;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDriver;
import org.eclipse.datatools.connectivity.oda.LogConfiguration;
import org.eclipse.datatools.connectivity.oda.OdaException;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Driver
        implements IDriver {

    static String ODA_DATA_SOURCE_ID = "org.polymap.rhei.birt.selectedFeatures";

    private LogConfiguration logConfig;

    @Override
    public IConnection getConnection( String dataSourceId ) throws OdaException {
        return new WorkbenchConnection();
    }

    @Override
    public void setLogConfiguration( LogConfiguration logConfig ) throws OdaException {
        this.logConfig = logConfig;
    }

    @Override
    public int getMaxConnections() throws OdaException {
        return 0;
    }

    @Override
    public void setAppContext( Object context ) throws OdaException {
    }

}
