/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleWire;

/**
 * An implementation of {@link BundleWire}.
 * 
 * @since 3.10
 */
public final class ModuleWire implements BundleWire {
    private final ModuleCapability capability;
    private final ModuleRevision hostingProvider;
    private final ModuleRequirement requirement;
    private final ModuleRevision hostingRequirer;
    // indicates that the wire points to valid wirings
    // technically this should be a separate flag for requirer vs provider but that
    // seems like overkill

    ModuleWire(ModuleCapability capability, ModuleRevision hostingProvider, ModuleRequirement requirement,
        ModuleRevision hostingRequirer) {
        super();
        this.capability = capability;
        this.hostingProvider = hostingProvider;
        this.requirement = requirement;
        this.hostingRequirer = hostingRequirer;
    }

    @Override
    public ModuleCapability getCapability() {
        return capability;
    }

    @Override
    public ModuleRequirement getRequirement() {
        return requirement;
    }

    @Override
    public ModuleWiring getProviderWiring() {
        return hostingProvider.getWiring();
    }

    @Override
    public ModuleWiring getRequirerWiring() {
        return hostingRequirer.getWiring();
    }

    @Override
    public ModuleRevision getProvider() {
        return hostingProvider;
    }

    @Override
    public ModuleRevision getRequirer() {
        return hostingRequirer;
    }

    @Override
    public String toString() {
        return getRequirement() + " -> " + getCapability(); //$NON-NLS-1$
    }

}
