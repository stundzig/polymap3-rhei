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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;
import org.eclipse.datatools.connectivity.oda.spec.QuerySpecification;

import org.polymap.rhei.birt.mapImage.WorkbenchConnector;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Query
        implements IQuery {

    private WorkbenchConnector      connector;
    
    private ResultSet               last;
    
    
    protected Query( WorkbenchConnector connector ) {
        this.connector = connector;
    }

    @Override
    public void setAppContext( Object context ) throws OdaException {
    }

    @Override
    public void setProperty( String name, String value ) throws OdaException {
    }

    @Override
    public void close() throws OdaException {
    }

    @Override
    public void setMaxRows( int max ) throws OdaException {
    }

    @Override
    public int getMaxRows() throws OdaException {
        return 0;
    }

    @Override
    public IResultSetMetaData getMetaData() throws OdaException {
        assert last != null;
        return last.getMetaData();
    }

    @Override
    public void prepare( String queryText ) throws OdaException {
        last = new ResultSet( connector );
    }

    @Override
    public IResultSet executeQuery() throws OdaException {
        assert last != null : "Call prepare() first!";
        return last = new ResultSet( connector );
    }

    @Override
    public void cancel() throws OdaException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setSortSpec( SortSpec sortBy ) throws OdaException {
        throw new OdaException( "Sorting not supported: " + sortBy );
    }

    @Override
    public SortSpec getSortSpec() throws OdaException {
        return null;
    }

    @Override
    public void setSpecification( QuerySpecification querySpec ) throws OdaException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public QuerySpecification getSpecification() {
        return null;
    }

    @Override
    public String getEffectiveQueryText() {
        return null;
    }

    // parameters *****************************************
    
    @Override
    public void clearInParameters() throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void setInt( String parameterName, int value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setInt( int parameterId, int value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setDouble( String parameterName, double value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setDouble( int parameterId, double value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setBigDecimal( String parameterName, BigDecimal value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setBigDecimal( int parameterId, BigDecimal value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setString( String parameterName, String value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setString( int parameterId, String value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setDate( String parameterName, Date value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setDate( int parameterId, Date value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setTime( String parameterName, Time value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setTime( int parameterId, Time value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setTimestamp( String parameterName, Timestamp value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setTimestamp( int parameterId, Timestamp value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setBoolean( String parameterName, boolean value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setBoolean( int parameterId, boolean value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setObject( String parameterName, Object value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setObject( int parameterId, Object value ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setNull( String parameterName ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void setNull( int parameterId ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public int findInParameter( String parameterName ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public IParameterMetaData getParameterMetaData() throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
}
