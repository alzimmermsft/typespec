/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.registry.osgi;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.registry.IRegistryConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleActivator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleContext;

/**
 * The extension registry bundle. This activator will create the default OSGi
 * registry unless told otherwise by setting the following system property to
 * false: <code>eclipse.createRegistry=false</code>
 *
 * The default registry will be stopped on the bundle shutdown.
 *
 * @see IRegistryConstants#PROP_DEFAULT_REGISTRY
 */
public class Activator implements BundleActivator {

	private static BundleContext bundleContext;

    public static BundleContext getContext() {
		return bundleContext;
	}

}
