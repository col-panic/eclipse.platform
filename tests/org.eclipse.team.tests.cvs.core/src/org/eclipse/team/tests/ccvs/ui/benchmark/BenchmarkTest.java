/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui.benchmark;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.tests.ccvs.core.EclipseTest;
import org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource;
import org.eclipse.team.tests.ccvs.ui.SynchronizeViewTestAdapter;
import org.eclipse.test.performance.*;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;

/**
 * Benchmark test superclass
 */
public abstract class BenchmarkTest extends EclipseTest {

	private HashMap groups;
    private PerformanceMeter currentMeter;
    private static SyncInfoSource source = new SynchronizeViewTestAdapter();

    protected BenchmarkTest() {
	}

	protected BenchmarkTest(String name) {
		super(name);
	}

	protected IProject createUniqueProject(File zipFile) throws TeamException, CoreException, ZipException, IOException, InterruptedException, InvocationTargetException {
		return createAndImportProject(getName(), zipFile);
	}
	
	protected IProject createAndImportProject(String prefix, File zipFile) throws TeamException, CoreException, ZipException, IOException, InterruptedException, InvocationTargetException {
		// create a project with no contents
		IProject project = getUniqueTestProject(prefix);
		Util.importZip(project, zipFile);
		return project;
	}
	
    /**
     * @param string
     */
    protected void startTask(String string) {
        // TODO Auto-generated method stub
        
    }
    
	/**
     * 
     */
	protected void endTask() {
        // TODO Auto-generated method stub
        
    }
	
    /**
     * Create a set of perforance meters that can be started with the
     * startGroup method.
     * @param performance_groups
     */
	protected void setupGroups(String[] performance_groups) {
        setupGroups(performance_groups, null, false);
    }
	
	protected void setupGroups(String[] performance_groups, String globalName, boolean global) {
        groups = new HashMap();
	    Performance perf = Performance.getDefault();
        for (int i = 0; i < performance_groups.length; i++) {
            String suffix = performance_groups[i];
		    PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this) + suffix);
            groups.put(suffix, meter);
            if(globalName != null) {
            	if(global)
            		perf.tagAsGlobalSummary(meter, globalName, Dimension.CPU_TIME);
            	else 
            		perf.tagAsSummary(meter, globalName, Dimension.CPU_TIME);
    	    }
        }
    }
    
    /**
     * Commit the performance meters that were created by setupGroups and started and
     * stoped using startGroup/endGroup
     */
    protected void commitGroups() {
        for (Iterator iter = groups.values().iterator(); iter.hasNext();) {
            PerformanceMeter meter = (PerformanceMeter) iter.next();
            meter.commit();
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.tests.ccvs.core.EclipseTest#tearDown()
     */
    protected void tearDown() throws Exception {
        try {
            if (groups != null) {
                Performance perf = Performance.getDefault();
                try {
                    for (Iterator iter = groups.values().iterator(); iter.hasNext();) {
                        PerformanceMeter meter = (PerformanceMeter) iter.next();
                        perf.assertPerformance(meter);
                    }
                } finally {
                    for (Iterator iter = groups.values().iterator(); iter.hasNext();) {
                        PerformanceMeter meter = (PerformanceMeter) iter.next();
                        meter.dispose();
                    }
                }
                groups = null;
            }
        } finally {
            super.tearDown();
        }
    }
    
    /**
     * Start the meter that was created for the given key
     * @param string
     */
    protected void startGroup(String key) {
        assertNull(currentMeter);
        currentMeter = (PerformanceMeter)groups.get(key);
        currentMeter.start();
    }
    
	/**
     * End the current meter
     */
	protected void endGroup() {
        currentMeter.stop();
        currentMeter = null;
    }
	
	protected void disableLog() {
	    // TODO:
	}
	
	protected void enableLog() {
	    // TODO:
	}
	
	protected void syncResources(Subscriber subscriber, IResource[] resources) throws TeamException {
	    startTask("Synchronize with Repository action");
	    getSyncInfoSource().refresh(subscriber, resources);
	    endTask();
	}

    /**
     * @param resources
     * @param string
     * @throws CoreException
     * @throws TeamException
     */
    protected void syncCommitResources(IResource[] resources, String comment) throws TeamException, CoreException {
       startTask("Synchronize outgoing changes");
       syncResources(CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber(), resources);
       endTask();
       // TODO: Commit all outgoing changes that are children of the given resource
       // by extracting them from the subscriber sync set
       startTask("Commit outgoing changes");
       commitResources(resources, IResource.DEPTH_INFINITE);
       endTask();
    }
    
    /**
     * @param resources
     * @throws TeamException
     */
    protected void syncUpdateResources(IResource[] resources) throws TeamException {
        startTask("Synchronize incoming changes");
        syncResources(CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber(), resources);
        endTask();
        // TODO: Update all incoming changes that are children of the given resource
        // by extracting them from the subscriber sync set
        startTask("Update incoming changes");
        updateResources(resources, false);
        endTask();
    }

    /**
     * @return
     */
    private SyncInfoSource getSyncInfoSource() {
        return source;
    }
}
