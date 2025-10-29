/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.preferences;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleActivator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * The Preferences bundle activator.
 */
public class Activator implements BundleActivator, ServiceTrackerCustomizer<Object, Object> {

    /**
     * The bundle associated this plug-in
     */
    private static BundleContext bundleContext;

    @Override
    public synchronized Object addingService(ServiceReference<Object> reference) {
        // this check is important as it avoids early loading of
        // PreferenceServiceRegistryHelper and allows
        // this bundle to operate with out necessarily resolving against the registry
        // return the registry service so we track it
        return bundleContext.getService(reference);
    }

    @Override
    public synchronized void removedService(ServiceReference<Object> reference, Object service) {
        bundleContext.ungetService(reference);
    }

}
