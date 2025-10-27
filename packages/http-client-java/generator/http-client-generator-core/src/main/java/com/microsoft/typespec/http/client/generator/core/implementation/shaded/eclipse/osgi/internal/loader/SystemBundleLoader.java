/*******************************************************************************
 * Copyright (c) 2003, 2017 IBM Corporation and others.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.loader;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleRevision;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.ModuleWiring;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxContainer;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.BundleInfo.Generation;

/**
 * The System Bundle's BundleLoader. This BundleLoader is used by
 * ImportClassLoaders to load a resource that is exported by the System Bundle.
 */
public class SystemBundleLoader extends BundleLoader {
	public static final String EQUINOX_EE = "x-equinox-ee"; //$NON-NLS-1$
	final ClassLoader classLoader;
	final ModuleClassLoader moduleClassLoader;

	public SystemBundleLoader(ModuleWiring wiring, EquinoxContainer container, ClassLoader frameworkLoader) {
		super(wiring, container, frameworkLoader.getParent());
		this.classLoader = frameworkLoader;
		this.moduleClassLoader = new SystemModuleClassLoader(classLoader.getParent(), container.getConfiguration(),
				this, (Generation) wiring.getRevision().getRevisionInfo());
	}

	/**
	 * The ClassLoader that loads OSGi framework classes is used to find the class.
	 */
	@Override
	public Class<?> findLocalClass(String name) {
		try {
			return classLoader.loadClass(name);
		} catch (ClassNotFoundException e) {
			// do nothing
			return null;
		}
	}

	/**
	 * The ClassLoader that loads OSGi framework classes is used to find the
	 * resource.
	 */
	@Override
	public URL findLocalResource(String name) {
		return classLoader.getResource(name);
	}

	/**
	 * The ClassLoader that loads OSGi framework classes is used to find the
	 * resource.
	 */
	@Override
	public Enumeration<URL> findLocalResources(String name) {
		try {
			return classLoader.getResources(name);
		} catch (IOException e) {
			// do nothing
			return null;
		}
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public ModuleClassLoader getModuleClassLoader() {
		return moduleClassLoader;
	}

	@Override
	void loadClassLoaderFragments(Collection<ModuleRevision> fragments) {
		moduleClassLoader.loadFragments(fragments);
	}

	class SystemModuleClassLoader extends EquinoxClassLoader {

		public SystemModuleClassLoader(ClassLoader parent, EquinoxConfiguration configuration, BundleLoader delegate,
				Generation generation) {
			super(parent, configuration);
		}
    }
}
