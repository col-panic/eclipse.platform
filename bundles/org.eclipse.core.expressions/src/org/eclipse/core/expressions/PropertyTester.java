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
package org.eclipse.core.expressions;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.internal.expressions.ExpressionMessages;
import org.eclipse.core.internal.expressions.ExpressionPlugin;
import org.eclipse.core.internal.expressions.Messages;
import org.eclipse.core.internal.expressions.PropertyTesterDescriptor;

/**
 * Abstract superclass of all property testers. Implementation classes of
 * the extension point <code>org.eclipse.core.expresssions.propertyTesters
 * </code> must extend <code>PropertyTester</code>.
 * <p>
 * A property tester implements the property tests enumerated in the property
 * tester extension point. For the following property test extension
 * <pre>
 *   &lt;propertyTester
 *     	 namespace="org.eclipse.jdt.core"
 *       id="org.eclipse.jdt.core.IPackageFragmentTester"
 *       properties="isDefaultPackage"
 *       type="org.eclipse.jdt.core.IPackageFragment"
 *       class="org.eclipse.demo.MyPackageFragmentTester"&gt;
 *     &lt;/propertyTester&gt;
 * </pre>
 * the corresponding implementation class looks like:
 * <pre>
 *   public class MyPackageFragmentTester {
 *       public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
 *           IPackageFragment fragement= (IPackageFragment)receiver;
 *	         if ("isDefaultPackage".equals(property)) { 
 *               return expectedValue == null
 *               	? fragement.isDefaultPackage()
 *               	: fragement.isDefaultPackage() == ((Boolean)expectedValue).booleanValue();
 *           }
 *           Assert.isTrue(false);
 *           return false;
 *       }
 *   }
 * </pre>
 * The property can then be used in a test expression as follows:
 * <pre>
 *   &lt;instanceof value="org.eclipse.core.IPackageFragment"/&gt;
 *   &lt;test property="org.eclipse.jdt.core.isDefaultPackage"/&gt;
 * </pre>
 * </p>
 * @since 3.0 
 */
public abstract class PropertyTester implements IPropertyTester {
	
	private IConfigurationElement fConfigElement;
	private String fNamespace;
	private String fProperties;
	
	/**
	 * Initialize the property tester with the given name space and property.
	 * <p>
	 * Note: this method is for internal use only. Clients must not call 
	 * this method.
	 * </p>
	 * @param descriptor the descriptor object for this tester
	 */
	public final void internalInitialize(PropertyTesterDescriptor descriptor) { 
		fProperties= descriptor.getProperties();
		fNamespace= descriptor.getNamespace();
		fConfigElement= descriptor.getConfigurationElement();
	}
	
	/**
	 * Note: this method is for internal use only. Clients must not call 
	 * this method.
	 * 
	 * @return the property tester descriptor
	 */
	public final PropertyTesterDescriptor internalCreateDescriptor() {
		return new PropertyTesterDescriptor(fConfigElement, fNamespace, fProperties);
	}
	
	/**
	 * Note: this method is for internal use only. Clients must not call 
	 * this method.
	 * 
	 * @throws CoreException if the plugin can't be activated
	 * @since 3.2
	 */
	public final void internalActivateDeclaringPlugin() throws CoreException {
		String pluginName= fConfigElement.getContributor().getName();
		Bundle bundle= Platform.getBundle(pluginName);
		try {
			bundle.start();
		} catch (BundleException e) {
			throw new CoreException(new Status(IStatus.ERROR, ExpressionPlugin.getPluginId(), IStatus.ERROR,
				Messages.format(ExpressionMessages.PropertyTester_error_activating_plugin, pluginName), 
				e));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean handles(String namespace, String property) {
		return fNamespace.equals(namespace) && fProperties.indexOf("," + property + ",") != -1;  //$NON-NLS-1$//$NON-NLS-2$
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final boolean isInstantiated() {
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isDeclaringPluginActive() {
		Bundle bundle= Platform.getBundle(fConfigElement.getContributor().getName());
		return bundle.getState() == Bundle.ACTIVE;		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final IPropertyTester instantiate() {
		return this;
	}
}