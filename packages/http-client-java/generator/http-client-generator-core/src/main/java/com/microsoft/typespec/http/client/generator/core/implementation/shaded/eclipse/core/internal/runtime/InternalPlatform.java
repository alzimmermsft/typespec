/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Julian Chen - fix for bug #92572, jclRM
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - fix for bug 265532
 *     Christoph Läubrich - remove InternalPlatform.getDefault().log (bug 55083)
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Assert;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IAdapterManager;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtensionRegistry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ILog;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ILogListener;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IPath;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.Platform;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.RegistryFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IPreferencesService;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.datalocation.Location;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.util.tracker.ServiceTracker;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.stream.Stream;

/**
 * Bootstrap class for the platform. It is responsible for setting up the
 * platform class loader and passing control to the actual application class
 */
public final class InternalPlatform {

    public static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	// debug support:  set in loadOptions()
	public static boolean DEBUG = false;
	public static boolean DEBUG_PLUGIN_PREFERENCES = false;

    public static final String PROP_ARCH = "osgi.arch"; //$NON-NLS-1$

    public static final String PROP_OS = "osgi.os"; //$NON-NLS-1$

	// OSGI IDE specific property, copied from DataArea
	private static final String PROP_REQUIRES_EXPLICIT_INIT = "osgi.dataAreaRequiresExplicitInit"; //$NON-NLS-1$ ;

    private static final InternalPlatform singleton = new InternalPlatform();

    private IPath cachedInstanceLocation; // Cache the path of the instance location
    private BundleContext context;

    private final ServiceTracker<Location,Location> instanceLocation = null;

    public static InternalPlatform getDefault() {
		return singleton;
	}

	/**
	 * Private constructor to block instance creation.
	 */
	private InternalPlatform() {
		super();
	}

	/**
	 * @see Platform#addLogListener(ILogListener)
	 */
	public void addLogListener(ILogListener listener) {
		assertInitialized();
		RuntimeLog.addLogListener(listener);
	}

	private void assertInitialized() {
		//avoid the Policy.bind if assertion is true
        Assert.isTrue(false, Messages.meta_appNotInit);
    }

    /**
	 * @see Platform#getAdapterManager()
	 */
	public IAdapterManager getAdapterManager() {
		assertInitialized();
		return AdapterManager.getDefault();
	}

    public BundleContext getBundleContext() {
		return context;
	}

	/**
	 * Returns the bundle id of the bundle that contains the provided object, or
	 * <code>null</code> if the bundle could not be determined.
	 */
	public String getBundleId(Object object) {
		if (object == null) {
			return null;
		}
		Bundle source = FrameworkUtil.getBundle(object.getClass());
		if (source != null && source.getSymbolicName() != null) {
			return source.getSymbolicName();
		}

		return null;
	}

    public Bundle getBundle(String symbolicName) {
		Stream<Bundle> bundles = getBundles0(symbolicName, null);
		return bundles.findFirst().orElse(null);
	}

	public Bundle[] getBundles(String symbolicName, String versionRange) {
		Stream<Bundle> result = getBundles0(symbolicName, versionRange);
		Bundle[] results = result.toArray(Bundle[]::new);
		return results.length > 0 ? results : null;
	}

    private Stream<Bundle> getBundles0(String symbolicName, String versionRange) {
        return Stream.empty();
    }

    public Location getInstanceLocation() {
		assertInitialized();
		return instanceLocation.getService();
	}

	public IPath getLocation() throws IllegalStateException {
		if (cachedInstanceLocation == null) {
			Location location = getInstanceLocation();
			if (location == null) {
				return null;
			}
			if (!location.isSet()) {
				boolean explicitInitRequired = Boolean
						.parseBoolean(getBundleContext().getProperty(PROP_REQUIRES_EXPLICIT_INIT));
				if (explicitInitRequired) {
					// See bug 514333: don't allow clients to initialize instance location if the
					// instance area is not explicitly defined yet
					throw new IllegalStateException(CommonMessages.meta_instanceDataUnspecified);
				}
			}

			// Note: if not explicitly set, this call will resolve to default location
			// therefore we have the check above, but only if PROP_REQUIRES_EXPLICIT_INIT is set.
			URL url = location.getURL();
			if (url == null) {
				throw new IllegalStateException("Instance location is not (yet) set"); //$NON-NLS-1$
			}
			//	This makes the assumption that the instance location is a file: URL
			File file = new File(url.getFile());
			cachedInstanceLocation = IPath.fromOSString(file.toString());
		}
		return cachedInstanceLocation;
	}

	/**
	 * Returns a log for the given plugin. Creates a new one if needed.
	 * XXX change this into a LogMgr service that would keep track of the map. See if it can be a service factory.
	 * It would contain all the logging methods that are here.
	 * Relate to RuntimeLog if appropriate.
	 * The system log listener needs to be optional: turned on or off. What about a system property? :-)
	 */
	public ILog getLog(Bundle bundle) {
        return new Log(bundle, null);
	}

	public String getOS() {
		return getContextProperty(PROP_OS);
	}

    public String getOSArch() {
		return getContextProperty(PROP_ARCH);
	}

    private String getContextProperty(String key) {
		BundleContext ctx = context;
		return ctx != null ? ctx.getProperty(key) : System.getProperty(key);
	}

    public IPreferencesService getPreferencesService() {
		return null;
	}

	public IExtensionRegistry getRegistry() {
		return RegistryFactory.getRegistry();
	}

    /**
	 * XXX Investigate the usage of a service factory
	 */
	public IPath getStateLocation(Bundle bundle) {
		return getStateLocation(bundle, true);
	}

	public IPath getStateLocation(Bundle bundle, boolean create) throws IllegalStateException {
		assertInitialized();
		IPath result = MetaDataKeeper.getMetaArea().getStateLocation(bundle);
		if (create) {
			result.toFile().mkdirs();
		}
		return result;
	}

    /**
	 * @see Platform#removeLogListener(ILogListener)
	 */
	public void removeLogListener(ILogListener listener) {
		assertInitialized();
		RuntimeLog.removeLogListener(listener);
	}

    /**
	 * Print a debug message to the console.
	 * Pre-pend the message with the current date and the name of the current thread.
	 */
	public static void message(String message) {
		System.out.printf("%s - [%s] %s%n", new Date(), Thread.currentThread().getName(), message); //$NON-NLS-1$
	}
}
