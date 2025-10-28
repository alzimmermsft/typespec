/*******************************************************************************
 * Copyright (c) 2005, 2025 IBM Corporation and others.
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
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.registry.IRegistryConstants;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IExtensionRegistry;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.RegistryFactory;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.spi.IRegistryProvider;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.spi.RegistryStrategy;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.localization.LocaleProvider;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.util.tracker.ServiceTracker;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import javax.xml.parsers.SAXParserFactory;

/**
 * The registry strategy that can be used in OSGi world. It provides the
 * following functionality:
 * <ul>
 * <li>Translation is done with ResourceTranslator</li>
 * <li>Registry is filled with information stored in plugin.xml / fragment.xml
 * of OSGi bundles</li>
 * <li>Uses bunlde-based class loading to create executable extensions</li>
 * <li>Performs registry validation based on the time stamps of the plugin.xml /
 * fragment.xml files</li>
 * <li>XML parser is obtained via an OSGi service</li>
 * </ul>
 *
 * @see RegistryFactory#setDefaultRegistryProvider(IRegistryProvider)
 * @since org.eclipse.equinox.registry 3.2
 */

public class RegistryStrategyOSGI extends RegistryStrategy {

    /**
     * Registry access key
     */
    private final Object token;

    /**
     * Debug extension registry
     */
    protected boolean DEBUG;

    /**
     * Tracker for the XML parser service
     */
    private ServiceTracker<?, ?> xmlTracker = null;

    /**
     * Tracker for the LocaleProvider service
     */
    private ServiceTracker<?, ?> localeTracker = null;

    /**
     * Value of the query "should we track contributions timestamps" is cached in
     * this variable
     */
    private final boolean trackTimestamp;

    /**
     * @param theStorageDir - array of file system directories to store cache files;
     * might be null
     * @param cacheReadOnly - array of read only attributes. True: cache at this
     * location is read only; false: cache is read/write
     * @param key - control key for the registry (should be the same key
     * as used in the RegistryManager#createExtensionRegistry()
     * of this registry
     */
    public RegistryStrategyOSGI(File[] theStorageDir, boolean[] cacheReadOnly, Object key) {
        super(theStorageDir);
        token = key;

        // Only do timestamp calculations if osgi.checkConfiguration is set to "true"
        // (typically,
        // this implies -dev mode)
        BundleContext context = Activator.getContext();
        if (context != null) {
            trackTimestamp = "true".equalsIgnoreCase(context.getProperty(IRegistryConstants.PROP_CHECK_CONFIG)); //$NON-NLS-1$
        } else {
            trackTimestamp = false;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // Start / stop extra processing: adding bundle listener; fill registry if not
    ///////////////////////////////////////////////////////////////////////////////////// filled
    ///////////////////////////////////////////////////////////////////////////////////// from
    ///////////////////////////////////////////////////////////////////////////////////// cache

    /**
     * Listening to the bundle events.
     */
    private EclipseBundleListener pluginBundleListener = null;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.spi.RegistryStrategy#onStart(com.microsoft.typespec.http.client.generator.core.
     * implementation.shaded.eclipse.core.
     * runtime.IExtensionRegistry, boolean)
     */
    @Override
    public void onStart(IExtensionRegistry registry, boolean loadedFromCache) {
        super.onStart(registry, loadedFromCache);

        if (!(registry instanceof ExtensionRegistry)) {
            return;
        }
        // register a listener to catch new bundle installations/resolutions.
        pluginBundleListener = new EclipseBundleListener((ExtensionRegistry) registry, token, this);
        Activator.getContext().addBundleListener(pluginBundleListener);

        // populate the registry with all the currently installed bundles.
        // There is a small window here while processBundles is being
        // called where the pluginBundleListener may receive a BundleEvent
        // to add/remove a bundle from the registry. This is ok since
        // the registry is a synchronized object and will not add the
        // same bundle twice.
        if (!loadedFromCache) {
            pluginBundleListener.processBundles(Activator.getContext().getBundles());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.core.runtime.spi.RegistryStrategy#onStop(com.microsoft.typespec.http.client.generator.core.
     * implementation.shaded.eclipse.core.runtime
     * .IExtensionRegistry)
     */
    @Override
    public void onStop(IExtensionRegistry registry) {
        if (pluginBundleListener != null) {
            Activator.getContext().removeBundleListener(pluginBundleListener);
        }
        if (xmlTracker != null) {
            xmlTracker.close();
            xmlTracker = null;
        }
        if (localeTracker != null) {
            localeTracker.close();
            localeTracker = null;
        }
        super.onStop(registry);
    }

    public boolean checkContributionsTimestamp() {
        return trackTimestamp;
    }

    public long getExtendedTimestamp(Bundle bundle, URL pluginManifest) {
        if (pluginManifest == null) {
            return 0;
        }
        try {
            return pluginManifest.openConnection().getLastModified() + bundle.getBundleId();
        } catch (IOException e) {
            if (debug()) {
                System.out.println("Unable to obtain timestamp for the bundle " + bundle.getSymbolicName()); //$NON-NLS-1$
                e.printStackTrace();
            }
            return 0;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.spi.RegistryStrategy#getXMLParser()
     */
    @Override
    public SAXParserFactory getXMLParser() {
        if (xmlTracker == null) {
            xmlTracker = new ServiceTracker<>(Activator.getContext(), SAXParserFactory.class.getName(), null);
            xmlTracker.open();
        }
        return (SAXParserFactory) xmlTracker.getService();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.spi.RegistryStrategy#getLocale()
     */
    @Override
    public String getLocale() {
        if (localeTracker == null) {
            localeTracker = new ServiceTracker<>(Activator.getContext(), LocaleProvider.class.getName(), null);
            localeTracker.open();
        }
        LocaleProvider localeProvider = (LocaleProvider) localeTracker.getService();
        if (localeProvider != null) {
            Locale currentLocale = localeProvider.getLocale();
            if (currentLocale != null) {
                return currentLocale.toString();
            }
        }
        return super.getLocale();
    }
}
