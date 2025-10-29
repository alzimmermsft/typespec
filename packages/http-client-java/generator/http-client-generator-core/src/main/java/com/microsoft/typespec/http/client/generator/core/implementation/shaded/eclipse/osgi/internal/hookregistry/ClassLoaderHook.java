/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.hookregistry;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxConfiguration;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.loader.BundleLoader;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.storage.BundleInfo.Generation;
import org.eclipse.osgi.internal.loader.ModuleClassLoader;

/**
 * A class loading hook that hooks into a module class loader
 */
public abstract class ClassLoaderHook {

    /**
     * Gets called by a bundle loader when the first time in order to allow a hook to create the class loader.
     * This should rarely, if ever be overridden. The default implementation returns
     * null indicating the built-in implementation should be used. Only one hook is
     * able to provide the implementation of the module class loader and the first
     * one to return non-null wins.
     *
     * @param parent the parent classloader
     * @param configuration the equinox configuration
     * @param delegate the delegate for this classloader
     * @param generation the generation for this class loader
     * @return returns an implementation of a module class loader or
     * <code>null</code> if the built-in implemention is to be used.
     */
    public ModuleClassLoader createClassLoader(ClassLoader parent, EquinoxConfiguration configuration,
        BundleLoader delegate, Generation generation) {
        // do nothing
        return null;
    }

    /**
     * Gets called by a classpath manager at the end of the first time and a class
     * loader is created.
     * 
     * @param classLoader the newly created bundle classloader
     */
    public void classLoaderCreated(ModuleClassLoader classLoader) {
        // do nothing
    }

    /**
     * Returns the parent class loader to be used by all ModuleClassLoaders. A
     * {@code null} value may be returned if this hook does not supply the parent.
     * Only one hook is able to provide the implementation of the parent class
     * loader and the first one to return non-null wins.
     * 
     * @param configuration the equinox configuration
     * @return the parent class loader to be used by all ModuleClassLoaders
     */
    public ClassLoader getModuleClassLoaderParent(EquinoxConfiguration configuration) {
        // do nothing by default
        return null;
    }

}
