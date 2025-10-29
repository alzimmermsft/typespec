/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
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
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxContainer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The hook registry is used to store all the hooks which are configured by the
 * hook configurators.
 * 
 * @see HookConfigurator
 */
public final class HookRegistry {

    private final EquinoxContainer container;
    private final List<ClassLoaderHook> classLoaderHooks = new ArrayList<>();
    private final List<ClassLoaderHook> classLoaderHooksRO = Collections.unmodifiableList(classLoaderHooks);
    private final List<StorageHookFactory<?, ?, ?>> storageHookFactories = new ArrayList<>();
    private final List<StorageHookFactory<?, ?, ?>> storageHookFactoriesRO
        = Collections.unmodifiableList(storageHookFactories);
    private final List<BundleFileWrapperFactoryHook> bundleFileWrapperFactoryHooks = new ArrayList<>();
    private final List<BundleFileWrapperFactoryHook> bundleFileWrapperFactoryHooksRO
        = Collections.unmodifiableList(bundleFileWrapperFactoryHooks);

    public HookRegistry(EquinoxContainer container) {
        this.container = container;
    }

    /**
     * Returns the list of configured class loading hooks.
     * 
     * @return the list of configured class loading hooks.
     */
    public List<ClassLoaderHook> getClassLoaderHooks() {
        return classLoaderHooksRO;
    }

    /**
     * Returns the list of configured storage hooks.
     * 
     * @return the list of configured storage hooks.
     */
    public List<StorageHookFactory<?, ?, ?>> getStorageHookFactories() {
        return storageHookFactoriesRO;
    }

    /**
     * Returns the configured bundle file wrapper factories
     * 
     * @return the configured bundle file wrapper factories
     */
    public List<BundleFileWrapperFactoryHook> getBundleFileWrapperFactoryHooks() {
        return bundleFileWrapperFactoryHooksRO;
    }

    /**
     * Returns the configuration associated with this hook registry.
     * 
     * @return the configuration associated with this hook registry.
     */
    public EquinoxConfiguration getConfiguration() {
        return container.getConfiguration();
    }

    /**
     * Returns the equinox container associated with this hook registry.
     * 
     * @return the equinox container associated with this hook registry.
     */
    public EquinoxContainer getContainer() {
        return container;
    }
}
