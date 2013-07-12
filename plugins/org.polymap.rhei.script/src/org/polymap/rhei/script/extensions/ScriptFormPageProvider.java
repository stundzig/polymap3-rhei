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
package org.polymap.rhei.script.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.opengis.feature.Feature;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.rhei.form.FormEditor;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorPage2;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormPageProvider;
import org.polymap.rhei.script.Messages;
import org.polymap.rhei.script.RheiScriptPlugin;
import org.polymap.rhei.script.java.ScriptedFormEditorPage;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ScriptFormPageProvider
        implements IFormPageProvider {

    private static Log log = LogFactory.getLog( ScriptFormPageProvider.class );

    private static final ScriptEngineManager    manager = new ScriptEngineManager();
    
    public static final Pattern                 javaClassPattern = Pattern.compile( "class ([a-zA-Z0-9_]+)" );
    

    public ScriptFormPageProvider() {
        for (ScriptEngineFactory f : manager.getEngineFactories()) {
            log.info( "available ScriptEngineFactory: " + f );
        }
    }

    
    public List<IFormEditorPage> addPages( final FormEditor formEditor, final Feature feature ) {
        final List<IFormEditorPage> result = new ArrayList();

        try {
            IProject project = RheiScriptPlugin.getOrCreateScriptProject();
            IFolder formsFolder = project.getFolder( "src/forms" );
        
            formsFolder.accept( new IResourceVisitor() {
                public boolean visit( IResource resource ) throws CoreException {
                    if (resource instanceof IFile ) {
                        try {
                            IFile file = (IFile)resource;

                            Map<String,Object> params = new HashMap();
                            params.put( "feature", feature );
                            params.put( "fs", formEditor.getFeatureStore() );
                            
                            PageWrapper pageWrapper = new PageWrapper( (IFile)resource, params );
                            if (pageWrapper.delegate != null && pageWrapper.delegate.wantsToBeShown()) {
                                result.add( pageWrapper );
                            }
                        }
                        catch (ScriptException e) {
                            PolymapWorkbench.handleError( RheiScriptPlugin.PLUGIN_ID, ScriptFormPageProvider.this,
                                    "An error occured while executing a script.", e );
                        }
                        catch (Exception e) {
                            log.warn( "Script error: ", e );
                        }
                    }
                    return true;
                }
            });
        }
        catch (CoreException e) {
            // don't break everything if no forms dir or something
            log.error( "", e );
        }
        return result;
    }


    /**
     * Resetable wrapper for the scripted page implementation. Also provides the
     * reset/re-run Action.
     */
    class PageWrapper
            extends ScriptRunner<ScriptedFormEditorPage>
            implements IFormEditorPage2 {
        
        private IFormEditorPageSite     pageSite;

        
        public PageWrapper( IFile src, Map<String,Object> params ) throws Exception {
            super( src, params );
        }

        
        public void dispose() {
            try {
                evalScript();
            }
            catch (Exception e) {
                PolymapWorkbench.handleError( RheiScriptPlugin.PLUGIN_ID, ScriptFormPageProvider.this,
                        "An error occured while excuting a script.", e );
            }
        }

        
        public void createFormContent( IFormEditorPageSite site ) {
            this.pageSite = site;
            delegate.createFormContent( site );
        }

        
        public Action[] getEditorActions() {
            // reload action
            Action rerunAction = null;
            if (SecurityUtils.isAdmin( Polymap.instance().getUser() )) {
                rerunAction = new Action( "Re-run" ) {
                    public void run() {
                        try {
                            pageSite.clearFields();
                            createFormContent( pageSite );
                            pageSite.reloadEditor();
                        }
                        catch (Exception e) {
                            PolymapWorkbench.handleError( RheiScriptPlugin.PLUGIN_ID, this, e.getLocalizedMessage(), e );
                        }
                    }
                };
                rerunAction.setImageDescriptor( ImageDescriptor.createFromURL(
                        RheiScriptPlugin.getDefault().getBundle().getResource( "icons/runlast_script.gif" ) ) );
                rerunAction.setToolTipText( Messages.get( "ScriptedFormEditorPage_resetTip" ) );
                rerunAction.setEnabled( true );
            }
            
            Action[] delegateActions = delegate.getEditorActions();
            delegateActions = delegateActions != null ? delegateActions : new Action[0];
            if (rerunAction != null) {
                delegateActions = (Action[])ArrayUtils.add( delegateActions, rerunAction );
            }
            return delegateActions;
        }

        public String getId() {
            return delegate.getId();
        }

        public byte getPriority() {
            return delegate.getPriority();
        }

        public String getTitle() {
            return delegate.getTitle();
        }

        
        public void doLoad( IProgressMonitor monitor ) throws Exception {
            if (delegate instanceof IFormEditorPage2) {
                ((IFormEditorPage2)delegate).doLoad( monitor );
            }
        }

        public void doSubmit( IProgressMonitor monitor ) throws Exception {
            if (delegate instanceof IFormEditorPage2) {
                ((IFormEditorPage2)delegate).doSubmit( monitor );
            }
        }

        public boolean isDirty() {
            return delegate instanceof IFormEditorPage2
                ? ((IFormEditorPage2)delegate).isDirty() : false;
        }


        public boolean isValid() {
            return delegate instanceof IFormEditorPage2
                    ? ((IFormEditorPage2)delegate).isValid() : false;
        }

    }

}
