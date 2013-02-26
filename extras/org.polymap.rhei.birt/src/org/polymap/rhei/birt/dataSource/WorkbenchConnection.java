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

import java.util.Properties;

import com.ibm.icu.util.ULocale;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.OdaException;

import org.polymap.rhei.birt.mapImage.WorkbenchConnector;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WorkbenchConnection
        implements IConnection {

    private WorkbenchConnector  connector;
    
    
    @Override
    public void setAppContext( Object context ) throws OdaException {
    }

    @Override
    public void open( Properties props ) throws OdaException {
        System.out.println( "Props: " + props );
        String url = props.getProperty( "url" );
        String username = props.getProperty( "username" );
        String password = props.getProperty( "password" );
        connector = new WorkbenchConnector( url, username, password );
    }

    @Override
    public void close() throws OdaException {
        connector = null;
    }

    @Override
    public boolean isOpen() throws OdaException {
        return connector != null;
    }

    @Override
    public IQuery newQuery( String dataSetType ) throws OdaException {
        return new Query( connector );
    }

    @Override
    public int getMaxQueries() throws OdaException {
        return 0;
    }

    @Override
    public void commit() throws OdaException {
        throw new RuntimeException( "Modifications are not supported.." );
    }

    @Override
    public void rollback() throws OdaException {
        throw new RuntimeException( "Modifications are not supported.." );
    }

    @Override
    public void setLocale( ULocale locale ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public IDataSetMetaData getMetaData( String dataSetType ) throws OdaException {
        // assumes that this driver supports only one type of data set,
        // ignores the specified dataSetType
        return new IDataSetMetaData() {
            @Override            
            public IConnection getConnection() throws OdaException {
                return WorkbenchConnection.this;
            }
            @Override
            public IResultSet getDataSourceObjects( String catalog, String schema, String object,
                    String version ) throws OdaException {
                throw new UnsupportedOperationException();
            }
            @Override
            public int getDataSourceMajorVersion() throws OdaException {
                return 1;
            }
            @Override
            public int getDataSourceMinorVersion() throws OdaException {
                return 0;
            }
            @Override
            public String getDataSourceProductName() throws OdaException {
                return "Workbench Data Source";
            }
            @Override
            public String getDataSourceProductVersion() throws OdaException {
                return new StringBuilder()
                        .append( getDataSourceMajorVersion() )
                        .append( '.' )
                        .append( getDataSourceMinorVersion() ).toString();
            }
            @Override
            public int getSQLStateType() throws OdaException {
                return IDataSetMetaData.sqlStateSQL99;
            }
            @Override
            public boolean supportsMultipleResultSets() throws OdaException {
                return false;
            }
            @Override
            public boolean supportsMultipleOpenResults() throws OdaException {
                return false;
            }
            @Override
            public boolean supportsNamedResultSets() throws OdaException {
                return false;
            }
            @Override
            public boolean supportsNamedParameters() throws OdaException {
                return false;
            }
            @Override
            public boolean supportsInParameters() throws OdaException {
                return true;
            }
            @Override
            public boolean supportsOutParameters() throws OdaException {
                return false;
            }
            @Override
            public int getSortMode() {
                return IDataSetMetaData.sortModeNone;
            }
        };
    }
    
}
