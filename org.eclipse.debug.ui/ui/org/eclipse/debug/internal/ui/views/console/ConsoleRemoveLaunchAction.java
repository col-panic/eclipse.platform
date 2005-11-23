/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;

/**
 * ConsoleRemoveTerminatedAction
 */
public class ConsoleRemoveLaunchAction extends Action implements IViewActionDelegate, IConsoleListener, ILaunchesListener2 {

    private ILaunch fLaunch;

    public ConsoleRemoveLaunchAction(ILaunch launch) {
        super(ConsoleMessages.ConsoleRemoveTerminatedAction_0);
        setToolTipText(ConsoleMessages.ConsoleRemoveTerminatedAction_1);

        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.CONSOLE_REMOVE_LAUNCH);
        setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_LCL_REMOVE));
        setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE));
        setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE));
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
        ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(this);
        fLaunch = launch;
        update();
    }

    public void dispose() {
    	DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
        ConsolePlugin.getDefault().getConsoleManager().removeConsoleListener(this);
    }

    public synchronized void update() {
    	setEnabled(fLaunch.isTerminated());
    }

    public synchronized void run() {
        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        launchManager.removeLaunch(fLaunch);
    }

    public void init(IViewPart view) {
    }

    public void run(IAction action) {
        run();
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

    public void consolesAdded(IConsole[] consoles) {
    }

    public void consolesRemoved(IConsole[] consoles) {
        update();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesTerminated(ILaunch[] launches) {
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesRemoved(ILaunch[] launches) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesAdded(ILaunch[] launches) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesChanged(ILaunch[] launches) {
	}
}
