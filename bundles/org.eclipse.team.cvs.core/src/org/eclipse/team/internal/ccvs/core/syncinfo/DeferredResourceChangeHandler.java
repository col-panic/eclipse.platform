/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.syncinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;
import org.eclipse.team.internal.core.BackgroundEventHandler;

/**
 * This class handles resources changes that are reported in deltas
 * in a deferred manner (i.e. in a background job)
 */
public class DeferredResourceChangeHandler extends BackgroundEventHandler {

	public DeferredResourceChangeHandler() {
		super(CVSMessages.DeferredResourceChangeHandler_0, CVSMessages.DeferredResourceChangeHandler_1);
	}

	private static final int IGNORE_FILE_CHANGED = 1;
	private static final int RECREATED_CVS_RESOURCE = 2;
	private static final int CONFLICTING_DELETION =3;
	
	private Set changedIgnoreFiles = new HashSet();
	private Set recreatedResources = new HashSet();
	private Set conflictingDeletion = new HashSet();

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.BackgroundEventHandler#processEvent(org.eclipse.team.core.subscribers.BackgroundEventHandler.Event, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void processEvent(Event event, IProgressMonitor monitor) throws TeamException {
		int type = event.getType();
		switch (type) {
			case IGNORE_FILE_CHANGED :
				changedIgnoreFiles.add(event.getResource());
				break;
			case RECREATED_CVS_RESOURCE :
				recreatedResources.add(event.getResource());
				break;
			case CONFLICTING_DELETION :
				conflictingDeletion.add(event.getResource());
				break;
		}				
	}
	
	private IContainer[] getParents(Set files) {
		Set parents = new HashSet();
		for (Iterator iter = files.iterator(); iter.hasNext();) {
			IFile file = (IFile) iter.next();
			parents.add(file.getParent());
		}
		return (IContainer[]) parents.toArray(new IContainer[parents.size()]);
	}

	public void ignoreFileChanged(IFile file) {
		//if (isSharedWithCVS(file))
			queueEvent(new ResourceEvent(file, IGNORE_FILE_CHANGED, IResource.DEPTH_ZERO), false);
	}
	
	/**
	 * The resource has been added and has sync info that has not been written to disk. 
	 * Queue an event to ensure that the CVS directory files
	 * are written to disk.
	 * @param resource the recently add resource
	 */
	public void recreated(IResource resource) {
		if (isSharedWithCVS(resource))
			queueEvent(new ResourceEvent(resource, RECREATED_CVS_RESOURCE, IResource.DEPTH_ZERO), false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.BackgroundEventHandler#dispatchEvents()
	 */
	protected boolean doDispatchEvents(IProgressMonitor monitor) {
		// Handle ignore file changes
		boolean workDone = !changedIgnoreFiles.isEmpty() || !recreatedResources.isEmpty();
		try {
            EclipseSynchronizer.getInstance().ignoreFilesChanged(getParents(changedIgnoreFiles));
        } catch (CVSException e) {
            // Log and continue
            CVSProviderPlugin.log(e);
        }
		changedIgnoreFiles.clear();
		// Handle recreations by project to reduce locking granularity
		Map recreations = getResourcesByProject((IResource[]) recreatedResources.toArray(new IResource[recreatedResources.size()]));
		recreatedResources.clear();
		for (Iterator iter = recreations.values().iterator(); iter.hasNext();) {
			List resources = (List) iter.next();
			try {
				EclipseSynchronizer.getInstance().resourcesRecreated((IResource[]) resources.toArray(new IResource[resources.size()]), monitor);
			} catch (CVSException e) {
				// Log and continue
				CVSProviderPlugin.log(e);
			}
		}
		IResource[] deletions = (IResource[]) conflictingDeletion.toArray(new IResource[conflictingDeletion.size()]);
		conflictingDeletion.clear();
		for (int i = 0; i < deletions.length; i++) {
			IResource resource = deletions[i];
			ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
			try {
				if(!cvsResource.isFolder() && cvsResource.isManaged()) {
					cvsResource.unmanage(monitor);
				}
			} catch (CVSException e) {
				// Log and continue
				CVSProviderPlugin.log(e);
			}
		}
		return workDone;
	}
	
	private Map getResourcesByProject(IResource[] resources) {
		Map result = new HashMap();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			IProject project = resource.getProject();
			List projectResources = (List)result.get(project);
			if (projectResources == null) {
				projectResources = new ArrayList();
				result.put(project, projectResources);
			}
			projectResources.add(resource);
		}
		return result;
	}

	public void handleConflictingDeletion(IResource local) {
		if (isSharedWithCVS(local))
			queueEvent(new ResourceEvent(local, CONFLICTING_DELETION, IResource.DEPTH_ZERO), false);
	}
	
	private boolean isSharedWithCVS(IResource resource) {
		IProject project = resource.getProject();
		if (project.isAccessible()) {
			RepositoryProvider provider = RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId());
			return provider != null;
		}
		return false;
	}

	protected Object getJobFamiliy() {
		return this;
	}

}
