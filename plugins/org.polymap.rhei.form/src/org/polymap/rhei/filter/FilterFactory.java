/*
 * polymap.org
 * Copyright 2011, Falko Bräutigam, and other contributors as indicated by
 * the @authors tag.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.polymap.rhei.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.ILayer;
import org.polymap.core.workbench.PolymapWorkbench;
import org.polymap.rhei.RheiFormPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FilterFactory {

    private static Log log = LogFactory.getLog( FilterFactory.class );
    
    private static FilterFactory        instance = new FilterFactory();
    
    public static FilterFactory instance() {
        // no session state currently
        return instance;
    }
    
    
    // instance *******************************************
    
    public List<IFilter> filtersForLayer( ILayer layer ) {
        List<IFilter> filters = new ArrayList();
        List<IFilter> standardFilters = new ArrayList();
        for (FilterProviderExtension ext : FilterProviderExtension.allExtensions()) {
            try {
                // FIXME IFilter is stateful but currently the same IFilter might be subject
                // to subsequent openDialog/View... request! Creating IFilter instances with
                // every getChildren() here might help but does not cure the problem
                List<? extends IFilter> candidates = ext.newFilterProvider().addFilters( layer );
                if (candidates == null) {
                    continue;
                }
                else if (ext.isStandard()) {
                    standardFilters.addAll( candidates );
                }
                else {
                    filters.addAll( candidates );
                }
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( RheiFormPlugin.PLUGIN_ID, null, e.getLocalizedMessage(), e );                
            }
        }
        return Collections.unmodifiableList( !filters.isEmpty() ? filters : standardFilters );
    }

    
    public IFilter filterForLayer( ILayer layer, String filterId ) {
        for (IFilter filter : filtersForLayer( layer )) {
            if (filter.getId().equals( filterId )) {
                return filter;
            }
        }
        return null;
    }
    
}
