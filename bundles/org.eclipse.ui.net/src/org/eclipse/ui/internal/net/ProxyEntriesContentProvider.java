/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.net;

import java.util.Collection;

import org.eclipse.core.internal.net.ProxyData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ProxyEntriesContentProvider implements IStructuredContentProvider {

	public ProxyEntriesContentProvider() {
		super();
	}

	@Override
	public void dispose() {
		// Do nothing
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object[] getElements(Object inputElement) {
		Collection coll = (Collection) inputElement;
		return coll.toArray(new ProxyData[0]);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Do nothing
	}

}