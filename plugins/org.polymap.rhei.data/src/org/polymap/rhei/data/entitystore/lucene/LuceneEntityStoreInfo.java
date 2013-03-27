package org.polymap.rhei.data.entitystore.lucene;

import java.io.File;
import java.io.Serializable;


public final class LuceneEntityStoreInfo
        implements Serializable {

    private File        dir;

    public LuceneEntityStoreInfo( File dir ) {
        this.dir = dir;
    }

    /**
     * Build a store wich holds index in RAM.
     */
    public LuceneEntityStoreInfo() {
    }
    
    /**
     * The directory of the index files, or null if index files should be stored in
     * RAM.
     */
    public File getDir() {
        return dir;
    }
    
}