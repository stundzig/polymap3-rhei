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

import org.json.JSONArray;
import org.json.JSONObject;

import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.IClob;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import org.polymap.rhei.birt.mapImage.WorkbenchConnector;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ResultSet
        implements IResultSet {

    private JSONArray           features;
    
    private int                 cursor;

    private JSONObject          schema;
    
    private JSONArray           names;
    
    
    protected ResultSet( WorkbenchConnector connector ) {
        assert connector != null;
        JSONObject data = connector.selectedFeatures();
        this.features = data.getJSONArray( "features" );
        if (features.length() > 0) {
            this.schema = features.getJSONObject( 0 ).getJSONObject( "properties" );
            this.names = schema.names();
        }
        else {
            this.schema = new JSONObject().put( "keine-Daten", "keine-Daten" );
            this.names = new JSONArray().put( "keine-Daten" );
        }
    }

    @Override
    public IResultSetMetaData getMetaData() throws OdaException {
        return new IResultSetMetaData() {
            
            @Override
            public int getColumnCount() throws OdaException {
                return schema.length();
            }
            @Override
            public String getColumnName( int index ) throws OdaException {
                return names.getString( index - 1 );
            }
            @Override
            public String getColumnLabel( int index ) throws OdaException {
                return getColumnName( index );
            }
            @Override
            public int getColumnType( int index ) throws OdaException {
                return 1;
            }
            @Override
            public String getColumnTypeName( int index ) throws OdaException {
                return "String";
            }
            @Override
            public int getColumnDisplayLength( int index ) throws OdaException {
                return -1;
            }
            @Override
            public int getPrecision( int index ) throws OdaException {
                return -1;
            }
            @Override
            public int getScale( int index ) throws OdaException {
                return -1;
            }
            @Override
            public int isNullable( int index ) throws OdaException {
                return columnNullableUnknown;
            }
        };
    }

    @Override
    public void close() throws OdaException {
        features = null;
    }

    @Override
    public void setMaxRows( int max ) throws OdaException {
    }

    @Override
    public boolean next() throws OdaException {
        return ++cursor < features.length();
    }

    @Override
    public int getRow() throws OdaException {
        return cursor + 1;
    }

    @Override
    public boolean wasNull() throws OdaException {
        // the optXXX() methods always return a value
        return false;
    }

    @Override
    public int findColumn( String columnName ) throws OdaException {
        for (int count=1; count<names.length(); count++) {
            if (names.getString( count ).equals( columnName )) {
                return count;
            }
        }
        throw new OdaException( "Column name not found: " + columnName );
    }

    protected JSONObject featureProperties( int featureIndex ) {
        return features.getJSONObject( featureIndex ).getJSONObject( "properties" );    
    }
    
    @Override
    public String getString( int index ) throws OdaException {
        return getString( names.getString( index - 1 ) );
    }

    @Override
    public String getString( String columnName ) throws OdaException {
        return featureProperties( cursor ).optString( columnName );
    }

    @Override
    public int getInt( int index ) throws OdaException {
        return getInt( names.getString( index - 1 ) );
    }

    @Override
    public int getInt( String columnName ) throws OdaException {
        return featureProperties( cursor ).optInt( columnName );
    }

    @Override
    public double getDouble( int index ) throws OdaException {
        return getDouble( names.getString( index - 1 ) );
    }

    @Override
    public double getDouble( String columnName ) throws OdaException {
        return featureProperties( cursor ).optDouble( columnName );
    }

    @Override
    public BigDecimal getBigDecimal( int index ) throws OdaException {
        return getBigDecimal( names.getString( index - 1 ) );
    }

    @Override
    public BigDecimal getBigDecimal( String columnName ) throws OdaException {
        return new BigDecimal( featureProperties( cursor ).optInt( columnName ) );
    }

    @Override
    public Date getDate( int index ) throws OdaException {
        return getDate( names.getString( index - 1 ) );
    }

    @Override
    public Date getDate( String columnName ) throws OdaException {
        return new Date( featureProperties( cursor ).optLong( columnName ) );
    }

    @Override
    public Time getTime( int index ) throws OdaException {
        return getTime( names.getString( index - 1 ) );
    }

    @Override
    public Time getTime( String columnName ) throws OdaException {
        return new Time( featureProperties( cursor ).optLong( columnName ) );
    }

    @Override
    public Timestamp getTimestamp( int index ) throws OdaException {
        return getTimestamp( names.getString( index - 1 ) );
    }

    @Override
    public Timestamp getTimestamp( String columnName ) throws OdaException {
        return new Timestamp( featureProperties( cursor ).optLong( columnName ) );
    }

    @Override
    public IBlob getBlob( int index ) throws OdaException {
        return getBlob( names.getString( index - 1 ) );
    }

    @Override
    public IBlob getBlob( String columnName ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public IClob getClob( int index ) throws OdaException {
        return getClob( names.getString( index - 1 ) );
    }

    @Override
    public IClob getClob( String columnName ) throws OdaException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean getBoolean( int index ) throws OdaException {
        return getBoolean( names.getString( index - 1 ) );
    }

    @Override
    public boolean getBoolean( String columnName ) throws OdaException {
        return featureProperties( cursor ).optBoolean( columnName );
    }

    @Override
    public Object getObject( int index ) throws OdaException {
        return getObject( names.getString( index - 1 ) );
    }

    @Override
    public Object getObject( String columnName ) throws OdaException {
        return featureProperties( cursor ).opt( columnName );
    }
    
}
