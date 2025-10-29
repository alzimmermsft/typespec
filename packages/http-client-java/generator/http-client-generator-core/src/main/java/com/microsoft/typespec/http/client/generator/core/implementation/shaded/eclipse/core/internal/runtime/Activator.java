/*******************************************************************************
 * Copyright (c) 2005, 2020 IBM Corporation and others.
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
 *     Sergey Prigogin (Google) - use parameterized types (bug 442021)
 *     Christoph Laeubrich - Bug 567344 - Support registration of IAdapterFactory as OSGi Service
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.ServiceCaller;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.framework.log.FrameworkLog;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.datalocation.Location;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.urlconversion.URLConverter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleActivator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Filter;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkUtil;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.InvalidSyntaxException;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.packageadmin.PackageAdmin;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.util.tracker.ServiceTracker;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * The Common runtime plugin class.
 *
 * This class can only be used if OSGi plugin is available.
 */
public class Activator implements BundleActivator {
    public static final String PLUGIN_ID
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.common"; //$NON-NLS-1$

    /**
     * Table to keep track of all the URL converter services.
     */
    private static final Map<String, ServiceTracker<Object, URLConverter>> urlTrackers = new HashMap<>();
    private static BundleContext bundleContext;
    private static Activator singleton;
    private final ServiceCaller<Location> instanceLocationTracker
        = new ServiceCaller<>(getClass(), Location.class, Location.INSTANCE_FILTER);
    @SuppressWarnings("deprecation")
    private final ServiceCaller<PackageAdmin> bundleTracker = new ServiceCaller<>(getClass(), PackageAdmin.class);
    private final ServiceCaller<FrameworkLog> logTracker = new ServiceCaller<>(getClass(), FrameworkLog.class);

    /*
     * Returns the singleton for this Activator. Callers should be aware that this
     * will return null if the bundle is not active.
     */
    public static Activator getDefault() {
        return singleton;
    }

    /*
     * Return the framework log service, if available.
     */
    public FrameworkLog getFrameworkLog() {
        return logTracker.current().orElse(null);
    }

    /*
     * Return the instance location service, if available.
     */
    public Location getInstanceLocation() {
        return instanceLocationTracker.current().orElse(null);
    }

    /**
     * Return the resolved bundle with the specified symbolic name.
     *
     * @see PackageAdmin#getBundles(String, String)
     */
    public Bundle getBundle(String symbolicName) {
        PackageAdmin admin = getBundleAdmin();
        if (admin == null) {
            return null;
        }
        Bundle[] bundles = admin.getBundles(symbolicName, null);
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

    /*
     * Return the package admin service, if available.
     */
    private PackageAdmin getBundleAdmin() {
        return bundleTracker.current().orElse(null);
    }

    /*
     * Return an array of fragments for the given bundle host.
     */
    public Bundle[] getFragments(Bundle host) {
        PackageAdmin admin = getBundleAdmin();
        if (admin == null) {
            return new Bundle[0];
        }
        return admin.getFragments(host);
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

    /*
     * Return this bundle's context.
     */
    static BundleContext getContext() {
        return bundleContext;
    }

    /*
     * Return the URL Converter for the given URL. Return null if we can't find one.
     */
    public static URLConverter getURLConverter(URL url) {
        BundleContext ctx = getContext();
        if (url == null || ctx == null) {
            return null;
        }
        String protocol = url.getProtocol();
        synchronized (urlTrackers) {
            ServiceTracker<Object, URLConverter> tracker = urlTrackers.get(protocol);
            if (tracker == null) {
                // get the right service based on the protocol
                String FILTER_PREFIX = "(&(objectClass=" + URLConverter.class.getName() + ")(protocol="; //$NON-NLS-1$ //$NON-NLS-2$
                String FILTER_POSTFIX = "))"; //$NON-NLS-1$
                Filter filter = null;
                try {
                    filter = ctx.createFilter(FILTER_PREFIX + protocol + FILTER_POSTFIX);
                } catch (InvalidSyntaxException e) {
                    return null;
                }
                tracker = new ServiceTracker<>(getContext(), filter, null);
                tracker.open();
                // cache it in the registry
                urlTrackers.put(protocol, tracker);
            }
            return tracker.getService();
        }
    }

}
