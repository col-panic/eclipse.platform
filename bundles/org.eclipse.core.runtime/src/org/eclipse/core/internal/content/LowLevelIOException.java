/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.content;

import java.io.IOException;
import org.eclipse.core.internal.runtime.Assert;

/**
 * A wrapper for IOExceptions, throw by LazyInputStream/Reader. 
 * Its purpose is to allow one to differentiate
 * between IOExceptions thrown by the base stream/reader from those 
 * thrown by streams/readers built on top of LazyInputStream/Reader.
 * 
 * @see LazyInputStream
 * @see LazyReader
 */
/* package */class LowLevelIOException extends IOException {

	private IOException actual;

	public LowLevelIOException(IOException actual) {
		// ensure we don't wrap more than once 
		Assert.isLegal(!(actual instanceof LowLevelIOException));
		this.actual = actual;
	}

	public IOException getActualException() {
		return actual;
	}
}
