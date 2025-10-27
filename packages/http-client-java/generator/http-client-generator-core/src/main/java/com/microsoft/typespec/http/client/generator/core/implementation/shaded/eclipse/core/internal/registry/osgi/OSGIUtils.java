/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.registry.RegistryMessages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime.RuntimeLog;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IStatus;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Status;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.packageadmin.PackageAdmin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.util.tracker.ServiceTracker;

/**
 * The class contains a set of helper methods for the runtime content plugin.
 * The closeServices() method should be called before the plugin is stopped.
 *
 * @since org.eclipse.equinox.registry 3.2
 */
public class OSGIUtils {
	private ServiceTracker<?, ?> bundleTracker = null;
	private ServiceTracker<?, ?> configurationLocationTracker = null;

	// OSGI system properties. Copied from EclipseStarter
	public static final String PROP_CONFIG_AREA = "osgi.configuration.area"; //$NON-NLS-1$

    private static final OSGIUtils singleton = new OSGIUtils();

	public static OSGIUtils getDefault() {
		return singleton;
	}

	/**
	 * Private constructor to block instance creation.
	 */
	private OSGIUtils() {
		super();
		initServices();
	}

	private void initServices() {
		BundleContext context = Activator.getContext();
		if (context == null) {
			RuntimeLog.log(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, 0,
					RegistryMessages.bundle_not_activated, null));
			return;
		}

		bundleTracker = new ServiceTracker<>(context, PackageAdmin.class.getName(), null);
		bundleTracker.open();

		// locations
		final String FILTER_PREFIX = "(&(objectClass=com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.datalocation.Location)(type="; //$NON-NLS-1$
		Filter filter = null;
		try {
			filter = context.createFilter(FILTER_PREFIX + PROP_CONFIG_AREA + "))"); //$NON-NLS-1$
		} catch (InvalidSyntaxException e) {
			// ignore this. It should never happen as we have tested the above format.
		}
		configurationLocationTracker = new ServiceTracker<>(context, filter, null);
		configurationLocationTracker.open();

	}

    public PackageAdmin getPackageAdmin() {
		if (bundleTracker == null) {
			RuntimeLog.log(new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, 0,
					RegistryMessages.bundle_not_activated, null));
			return null;
		}
		return (PackageAdmin) bundleTracker.getService();
	}

	public Bundle getBundle(String bundleName) {
		PackageAdmin packageAdmin = getPackageAdmin();
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

    public boolean isFragment(Bundle bundle) {
		PackageAdmin packageAdmin = getPackageAdmin();
		if (packageAdmin == null) {
			return false;
		}
		return (packageAdmin.getBundleType(bundle) & PackageAdmin.BUNDLE_TYPE_FRAGMENT) > 0;
	}

	public Bundle[] getHosts(Bundle bundle) {
		PackageAdmin packageAdmin = getPackageAdmin();
		if (packageAdmin == null) {
			return null;
		}
		return packageAdmin.getHosts(bundle);
	}

}
