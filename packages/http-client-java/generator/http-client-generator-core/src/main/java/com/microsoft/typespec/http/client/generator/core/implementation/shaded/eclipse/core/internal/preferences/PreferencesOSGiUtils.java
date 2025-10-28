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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.preferences.exchange.ILegacyPreferences;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.datalocation.Location;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.packageadmin.PackageAdmin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.util.tracker.ServiceTracker;

/**
 * This class contains a set of helper OSGI methods for the Preferences plugin.
 * The closeServices() method should be called before the plugin is stopped.
 *
 * @since org.eclipse.equinox.preferences 3.2
 */
public class PreferencesOSGiUtils {
    private ServiceTracker<?, ILegacyPreferences> initTracker;
    private ServiceTracker<?, PackageAdmin> bundleTracker;
    private ServiceTracker<?, ?> configurationLocationTracker;
    private ServiceTracker<?, ?> instanceLocationTracker;

    private static final PreferencesOSGiUtils singleton = new PreferencesOSGiUtils();

    public static PreferencesOSGiUtils getDefault() {
        return singleton;
    }

    /**
     * Private constructor to block instance creation.
     */
    private PreferencesOSGiUtils() {
        super();
    }

    public ILegacyPreferences getLegacyPreferences() {
        if (initTracker != null) {
            return initTracker.getService();
        }
        return null;
    }

    public Bundle getBundle(String bundleName) {
        if (bundleTracker == null) {
            return null;
        }
        PackageAdmin packageAdmin = bundleTracker.getService();
        if (packageAdmin == null) {
            return null;
        }
        Bundle[] bundles = packageAdmin.getBundles(bundleName, null);
        if (bundles == null) {
            return null;
        }
        // Return the first bundle that is not installed or uninstalled
        for (Bundle bundle : bundles) {
            if ((bundle.getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
                return bundle;
            }
        }
        return null;
    }

    public Location getConfigurationLocation() {
        if (configurationLocationTracker != null) {
            return (Location) configurationLocationTracker.getService();
        }
        return null;
    }

    public Location getInstanceLocation() {
        if (instanceLocationTracker != null) {
            return (Location) instanceLocationTracker.getService();
        }
        return null;
    }
}
