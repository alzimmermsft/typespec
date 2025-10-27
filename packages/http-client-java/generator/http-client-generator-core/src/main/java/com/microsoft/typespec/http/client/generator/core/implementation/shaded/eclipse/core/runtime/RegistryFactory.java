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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.registry.RegistryProviderFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.spi.IRegistryProvider;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.spi.RegistryStrategy;

/**
 * Use this class to create or obtain an extension registry.
 * <p>
 * The following methods can be used without OSGi running:
 * </p>
 * <ul>
 * <li>{@link #getRegistry()}</li>
 * </ul>
 * <p>
 * This class is not intended to be subclassed or instantiated.
 * </p>
 *
 * @since org.eclipse.equinox.registry 3.2
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class RegistryFactory {

	/**
	 * Returns the default extension registry specified by the registry provider.
	 * May return <code>null</code> if the provider has not been set or if the
	 * registry has not been created.
	 *
	 * @return existing extension registry or <code>null</code>
	 */
	public static IExtensionRegistry getRegistry() {
		IRegistryProvider defaultRegistryProvider = RegistryProviderFactory.getDefault();
		if (defaultRegistryProvider == null) {
			return null;
		}
		return defaultRegistryProvider.getRegistry();
	}
}
