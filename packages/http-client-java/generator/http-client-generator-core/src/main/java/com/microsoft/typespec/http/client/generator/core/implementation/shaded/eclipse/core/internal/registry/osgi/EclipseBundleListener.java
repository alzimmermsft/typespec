/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.registry.ExtensionRegistry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.registry.RegistryMessages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime.ResourceTranslator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime.RuntimeLog;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.ManifestElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.*;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * A listener for bundle events. When a bundles come and go we look to see if
 * there are any extensions or extension points and update the registry
 * accordingly. Using a Synchronous listener here is important. If the bundle
 * activator code tries to access the registry to get its extension points, we
 * need to ensure that they are in the registry before the bundle start is
 * called. By listening sync we are able to ensure that happens.
 */
public class EclipseBundleListener implements SynchronousBundleListener {
    private static final String PLUGIN_MANIFEST = "plugin.xml"; //$NON-NLS-1$
    private static final String FRAGMENT_MANIFEST = "fragment.xml"; //$NON-NLS-1$

    private final ExtensionRegistry registry;
    private final RegistryStrategyOSGI strategy;
    private final Object token;

    public EclipseBundleListener(ExtensionRegistry registry, Object key, RegistryStrategyOSGI strategy) {
        this.registry = registry;
        this.token = key;
        this.strategy = strategy;
    }

    public void processBundles(Bundle[] bundles) {
        for (Bundle bundle : bundles) {
            if (isBundleResolved(bundle)) {
                addBundle(bundle);
            } else {
                removeBundle(bundle);
            }
        }
    }

    private boolean isBundleResolved(Bundle bundle) {
        return (bundle.getState() & (Bundle.RESOLVED | Bundle.ACTIVE | Bundle.STARTING | Bundle.STOPPING)) != 0;
    }

    private void removeBundle(Bundle bundle) {
        long timestamp = 0;
        if (strategy.checkContributionsTimestamp()) {
            URL pluginManifest = getExtensionURL(bundle, false);
            if (pluginManifest != null) {
                timestamp = strategy.getExtendedTimestamp(bundle, pluginManifest);
            }
        }
        registry.remove(Long.toString(bundle.getBundleId()), timestamp);
    }

    static public URL getExtensionURL(Bundle bundle, boolean report) {
        // bail out if the bundle does not have a symbolic name
        if (bundle.getSymbolicName() == null) {
            return null;
        }

        boolean isFragment = OSGIUtils.getDefault().isFragment(bundle);
        String manifestName = isFragment ? FRAGMENT_MANIFEST : PLUGIN_MANIFEST;
        URL extensionURL = bundle.getEntry(manifestName);
        if (extensionURL == null) {
            return null;
        }

        // If the bundle is not a singleton, then it is not added
        if (!isSingleton(bundle)) {
            if (report && !isGeneratedManifest(bundle)) {
                String message = NLS.bind(RegistryMessages.parse_nonSingleton, bundle.getSymbolicName());
                RuntimeLog.log(new Status(IStatus.WARNING, RegistryMessages.OWNER_NAME, 0, message, null));
            }
            return null;
        }
        if (!isFragment) {
            return extensionURL;
        }

        // If the bundle is a fragment being added to a non singleton host, then it is
        // not added
        Bundle[] hosts = OSGIUtils.getDefault().getHosts(bundle);
        if (hosts == null) {
            return null; // should never happen?
        }

        if (isSingleton(hosts[0])) {
            return extensionURL;
        }

        if (report) {
            // if the host is not a singleton we always report the error; even if the host
            // has a generated manifest
            String message = NLS.bind(RegistryMessages.parse_nonSingletonFragment, bundle.getSymbolicName(),
                hosts[0].getSymbolicName());
            RuntimeLog.log(new Status(IStatus.WARNING, RegistryMessages.OWNER_NAME, 0, message, null));
        }
        return null;
    }

    private static boolean isGeneratedManifest(Bundle bundle) {
        return bundle.getHeaders("").get("Generated-from") != null; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void addBundle(Bundle bundle) {
        // if the given bundle already exists in the registry then return.
        // note that this does not work for update cases.
        IContributor contributor = ContributorFactoryOSGi.createContributor(bundle);
        if (registry.hasContributor(contributor)) {
            return;
        }
        URL pluginManifest = getExtensionURL(bundle, true);
        if (pluginManifest == null) {
            return;
        }
        InputStream is;
        try {
            is = new BufferedInputStream(pluginManifest.openStream());
        } catch (IOException ex) {
            is = null;
        }
        if (is == null) {
            return;
        }

        ResourceBundle translationBundle = null;
        try {
            translationBundle = ResourceTranslator.getResourceBundle(bundle);
        } catch (MissingResourceException e) {
            // Ignore the exception
        }
        long timestamp = 0;
        if (strategy.checkContributionsTimestamp()) {
            timestamp = strategy.getExtendedTimestamp(bundle, pluginManifest);
        }
        registry.addContribution(is, contributor, true, pluginManifest.getPath(), translationBundle, token, timestamp);
    }

    private static boolean isSingleton(Bundle bundle) {
        Dictionary<?, ?> allHeaders = bundle.getHeaders(""); //$NON-NLS-1$
        String symbolicNameHeader = (String) allHeaders.get(Constants.BUNDLE_SYMBOLICNAME);
        try {
            if (symbolicNameHeader != null) {
                ManifestElement[] symbolicNameElements
                    = ManifestElement.parseHeader(Constants.BUNDLE_SYMBOLICNAME, symbolicNameHeader);
                if (symbolicNameElements.length > 0) {
                    String singleton = symbolicNameElements[0].getDirective(Constants.SINGLETON_DIRECTIVE);
                    if (singleton == null) {
                        singleton = symbolicNameElements[0].getAttribute(Constants.SINGLETON_DIRECTIVE);
                    }

                    if (!"true".equalsIgnoreCase(singleton)) { //$NON-NLS-1$
                        String manifestVersion = (String) allHeaders.get(Constants.BUNDLE_MANIFESTVERSION);
                        if (manifestVersion == null) {// the header was not defined for previous versions of the bundle
                            // 3.0 bundles without a singleton attributes are still being accepted
                            if (OSGIUtils.getDefault().getBundle(symbolicNameElements[0].getValue()) == bundle) {
                                return true;
                            }
                        }
                        return false;
                    }
                }
            }
        } catch (BundleException e1) {
            // This can't happen because the fwk would have rejected the bundle
        }
        return true;
    }
}
