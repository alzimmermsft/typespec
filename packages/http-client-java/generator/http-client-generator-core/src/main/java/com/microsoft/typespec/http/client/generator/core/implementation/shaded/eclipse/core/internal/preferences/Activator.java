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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime.RuntimeLog;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleActivator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.ServiceReference;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * The Preferences bundle activator.
 */
public class Activator implements BundleActivator, ServiceTrackerCustomizer<Object, Object> {

    public static final String PI_PREFERENCES
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.preferences"; //$NON-NLS-1$

    /**
     * The bundle associated this plug-in
     */
    private static BundleContext bundleContext;

    static BundleContext getContext() {
        return bundleContext;
    }

    @Override
    public synchronized Object addingService(ServiceReference<Object> reference) {
        Object service = bundleContext.getService(reference);
        // this check is important as it avoids early loading of
        // PreferenceServiceRegistryHelper and allows
        // this bundle to operate with out necessarily resolving against the registry
        if (service != null) {
            try {
                Object helper = new PreferenceServiceRegistryHelper(PreferencesService.getDefault(), service);
                PreferencesService.getDefault().setRegistryHelper(helper);
            } catch (Exception e) {
                RuntimeLog.log(new Status(IStatus.ERROR, PI_PREFERENCES, 0, PrefsMessages.noRegistry, e));
            } catch (NoClassDefFoundError error) {
                // Normally this catch would not be needed since we should never see the
                // IExtensionRegistry service without resolving against registry.
                // However, the check is very lenient with split packages and this can happen
                // when
                // the preferences bundle is already resolved at the time the registry bundle is
                // installed.
                // For this case we ignore the error. When refreshed the bundle will be rewired
                // correctly.
                // null is returned because we don't want to track this particular service
                // reference.
                return null;
            }
        }
        // return the registry service so we track it
        return service;
    }

    @Override
    public synchronized void removedService(ServiceReference<Object> reference, Object service) {
        PreferencesService.getDefault().setRegistryHelper(null);
        bundleContext.ungetService(reference);
    }

}
