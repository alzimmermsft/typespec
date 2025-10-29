/*******************************************************************************
 * Copyright (c) 2012, 2020 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleRevision;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.wiring.BundleRevisions;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link BundleRevisions} which represent a {@link Module}
 * installed in a {@link ModuleContainer container}. The ModuleRevisions
 * provides a bridge between the revisions, the module and the container they
 * are associated with. The ModuleRevisions holds the information about the
 * installation of a module in a container such as the module id and location.
 * 
 * @since 3.10
 */
public final class ModuleRevisions implements BundleRevisions {
    private final Object monitor = new Object();
    private final Module module;
    private final ModuleContainer container;
    /* @GuardedBy("monitor") */
    private final List<ModuleRevision> revisions = new ArrayList<>(1);
    /* @GuardedBy("monitor") */
    private final boolean uninstalled = false;
    /* @GuardedBy("monitor") */
    private ModuleRevision uninstalledCurrent;

    ModuleRevisions(Module module, ModuleContainer container) {
        this.module = module;
        this.container = container;
    }

    public Module getModule() {
        return module;
    }

    ModuleContainer getContainer() {
        return container;
    }

    @Override
    public Bundle getBundle() {
        return module.getBundle();
    }

    @Override
    public List<BundleRevision> getRevisions() {
        synchronized (monitor) {
            return new ArrayList<>(revisions);
        }
    }

    /**
     * Same as {@link ModuleRevisions#getRevisions()} except it returns a list of
     * {@link ModuleRevision}.
     * 
     * @return the list of module revisions
     */
    public List<ModuleRevision> getModuleRevisions() {
        synchronized (monitor) {
            return new ArrayList<>(revisions);
        }
    }

    /**
     * Returns the current {@link ModuleRevision revision} associated with this
     * revisions.
     *
     * @return the current {@link ModuleRevision revision} associated with this
     * revisions or {@code null} if the current revision does not exist.
     */
    ModuleRevision getCurrentRevision() {
        synchronized (monitor) {
            if (uninstalled) {
                return uninstalledCurrent;
            }
            if (revisions.isEmpty()) {
                return null;
            }
            return revisions.get(0);
        }
    }

    boolean isUninstalled() {
        synchronized (monitor) {
            return uninstalled;
        }
    }

    @Override
    public String toString() {
        return "moduleID=" + module.getId(); //$NON-NLS-1$
    }
}
