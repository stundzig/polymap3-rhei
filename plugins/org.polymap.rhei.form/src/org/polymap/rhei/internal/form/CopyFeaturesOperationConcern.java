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
package org.polymap.rhei.internal.form;

import java.util.Collections;
import java.util.List;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.filter.identity.FeatureId;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.operation.FeatureOperationContainer;
import org.polymap.core.data.operations.feature.CopyFeaturesOperation2;
import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.Messages;
import org.polymap.rhei.form.FormEditor;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CopyFeaturesOperationConcern
        extends IOperationConcernFactory {
    
    @Override
    public IUndoableOperation newInstance( final IUndoableOperation op, final OperationInfo info ) {
        if (op instanceof FeatureOperationContainer
                && ((FeatureOperationContainer)op).getDelegate() instanceof CopyFeaturesOperation2) {

            return new OperationConcernAdapter() {
                
                @Override
                public IStatus execute( IProgressMonitor monitor, IAdaptable _info )
                throws ExecutionException {
                    IStatus result = info.next().execute( monitor, _info );
                    
                    if (!result.isOK()) {
                        return result;
                    }

                    CopyFeaturesOperation2 cop = (CopyFeaturesOperation2)((FeatureOperationContainer)op).getDelegate();
                    final List<FeatureId> fids = cop.getCreatedFeatureIds();
                    final FeatureStore fs = cop.getDestFs();
                    
                    if (fids.size() < 4) {
                        Display display = (Display)_info.getAdapter( Display.class );
                        display.asyncExec( new Runnable() {
                            public void run() {
                                try {
                                    if (MessageDialog.openQuestion( 
                                            PolymapWorkbench.getShellToParentOn(), 
                                            Messages.get( "NewFeatureOperationConcern_dialogTitle", fids.size() ),
                                            Messages.get( "NewFeatureOperationConcern_dialogMsg" ) )) {

                                        for (FeatureId fid : fids) {
                                            FeatureCollection destFeature = fs.getFeatures( 
                                                    DataPlugin.ff.id( Collections.singleton( fid ) ) );
                                            
                                            destFeature.accepts( new FeatureVisitor() {
                                                public void visit( Feature feature ) {
                                                    FormEditor.open( fs, feature, null, true );
                                                }                                                
                                            }, null );
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    PolymapWorkbench.handleError( DataPlugin.PLUGIN_ID, this, "Das Formular zum Bearbeiten des neuen Objektes konnte nicht geöffnet werden.", e );
                                }
                            }
                        });
                    }
                    return result;
                }

                @Override
                public IStatus redo( IProgressMonitor monitor, IAdaptable _info ) throws ExecutionException {
                    return info.next().redo( monitor, info );
                }

                @Override
                public IStatus undo( IProgressMonitor monitor, IAdaptable _info ) throws ExecutionException {
                    return info.next().undo( monitor, info );
                }

                @Override
                protected OperationInfo getInfo() {
                    return info;
                }
            };
        }
        return null;
    }
    
}
