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
package org.polymap.rhei.birt.mapImage;

import java.util.HashMap;
import java.util.Map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONObject;

import org.apache.xerces.impl.dv.util.Base64;

/**
 * Connects to a running Polymap server via WebDAV and retrieves information
 * about running workbench session.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WorkbenchConnector {

    /** The base WebDAV URI including /webdav/Workbench path. */
    private String          baseUri;
    
    private String          username;
    
    private String          password;
    

    public WorkbenchConnector( String baseUri, String username, String password ) {
        super();
        this.baseUri = baseUri + "/webdav/Workbench";
        this.username = username;
        this.password = password;
    }
    
    
    public byte[] mapImage( int width, int height ) {
        Map<String,String> params = new HashMap();
        params.put( "width", String.valueOf( width ) );
        params.put( "height", String.valueOf( height ) );
        return request( "/MapImage", params );
    }


    public JSONObject selectedFeatures() {
        try {
            byte[] bytes = request( "/SelectedFeatures.json", new HashMap() );
            return new JSONObject( new String( bytes, "UTF-8" ) );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    protected byte[] request( String path, Map<String,String> params ) {
        assert path.startsWith( "/" );
        InputStream in = null;
        try {
            String authString = username + ":" + password;
            System.out.println( "auth string: " + authString );
            String authStringEnc = Base64.encode( authString.getBytes( "UTF-8" ) );
            System.out.println( "Base64 encoded auth string: " + authStringEnc );

            StringBuilder urlbuf = new StringBuilder( 128 )
                    .append( baseUri ).append( path ).append( '?' );
            
            for (Map.Entry<String,String> param : params.entrySet()) {
                urlbuf.append( param.getKey() ).append( '=' ).append( param.getValue() );
                urlbuf.append( '&' );
            }
            URL url = new URL( urlbuf.toString() );
            URLConnection conn = url.openConnection();
            conn.setRequestProperty( "Authorization", "Basic " + authStringEnc );
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            in = conn.getInputStream();
            
            byte[] buf = new byte[4096];
            int len = -1;
            while ((len = in.read( buf )) > 0) {
                out.write( buf, 0, len );
            }
            return out.toByteArray(); 
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        finally {
            if (in != null) {
                try { in.close(); } catch (IOException e) { throw new RuntimeException( e ); }
            }
        }
    }

}
