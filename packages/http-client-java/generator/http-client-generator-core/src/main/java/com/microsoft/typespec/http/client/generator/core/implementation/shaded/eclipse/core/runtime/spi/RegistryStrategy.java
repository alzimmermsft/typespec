/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.spi;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.registry.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;
import java.io.File;
import java.util.*;
import javax.xml.parsers.SAXParserFactory;

/**
 * This is the basic registry strategy. It describes how the registry does
 * logging, message translation, extra start/stop processing, event scheduling,
 * caching, and debugging.
 * <p>
 * In this strategy:
 * </p>
 * <ul>
 * <li>Logging is done onto <code>System.out</code>;</li>
 * <li>The translation routine assumes that keys are prefixed with
 * <code>'%'/</code>;</li>
 * <li>Caching is enabled and doesn't use state or time stamp validation;</li>
 * <li>Standard Java class loading is used to create executable extensions.</li>
 * </ul>
 * <p>
 * This class can be used without OSGi running.
 * </p>
 * <p>
 * This class can be overridden and/or instantiated by clients.
 * </p>
 *
 * @since org.eclipse.equinox.registry 3.2
 */
public class RegistryStrategy {

    private SAXParserFactory theXMLParserFactory = null;

    /**
     * Array of file system directories to store cache files; might be
     * <code>null</code>
     */
    private final File[] storageDirs;

    /**
     * Constructor for this default registry strategy.
     * <p>
     * The strategy sequentially checks the array of storage directories to discover
     * the location of the registry cache formed by previous invocations of the
     * extension registry. Once found, the location is used to store registry cache.
     * If this value is <code>null</code> then caching of the registry content is
     * disabled.
     * </p>
     * <p>
     * The cache read-only array is an array the same length as the storage
     * directory array. It contains boolean values indicating whether or not each
     * storage directory is read-only. If the value at an index is <code>true</code>
     * then the location at the corresponding index in the storage directories array
     * is read-only; if <code>false</code> then the cache location is read-write.
     * The array can be <code>null</code> if the <code>storageDirs</code> parameter
     * is <code>null</code>.
     * </p>
     *
     * @param storageDirs array of file system directories, or <code>null</code>
     */
    public RegistryStrategy(File[] storageDirs) {
        this.storageDirs = storageDirs;
    }

    /**
     * Returns the possible registry cache location identified by the index.
     *
     * @param index index of the possible registry location
     * @return potential registry cache location
     */
    public final File getStorage(int index) {
        if (storageDirs != null) {
            return storageDirs[index];
        }
        return null;
    }

    /**
     * Override this method to provide customized logging functionality to the
     * registry. The method adds a log entry based on the supplied status.
     * <p>
     * This method writes a message to <code>System.out</code> in the following
     * format:
     * </p>
     *
     * <pre>
     * [Error|Warning|Log]: Main error message
     * [Error|Warning|Log]: Child error message 1
     * 	...
     * [Error|Warning|Log]: Child error message N
     * </pre>
     *
     * @param status the status to log
     */
    public void log(IStatus status) {
        RegistrySupport.log(status, null);
    }

    /**
     * Override this method to provide additional processing performed when the
     * registry is created and started. Overrides should call
     * <code>super.onStart()</code> at the beginning of the processing.
     *
     * @param registry the extension registry being started
     * @param loadedFromCache true is registry contents was loaded from cache when
     * the registry was created
     *
     * @since 3.4
     */
    public void onStart(IExtensionRegistry registry, boolean loadedFromCache) {
        // The default implementation
    }

    /**
     * Override this method to provide additional processing to be performed just
     * before the registry is stopped. Overrides should call
     * <code>super.onStop()</code> at the end of the processing.
     *
     * @param registry the extension registry being stopped
     */
    public void onStop(IExtensionRegistry registry) {
        // The default implementation
    }

    /**
     * Creates an executable extension. Override this method to supply an
     * alternative processing for the creation of executable extensions.
     * <p>
     * This method receives the contributor of the executable extension and,
     * possibly, an optional contributor name if specified by the executable
     * extension. The overridden contributor name might be <code>null</code>.
     * </p>
     * <p>
     * In this implementation registry attempts to instantiate the class specified
     * via the class name (must not be <code>null</code>) using standard Java
     * reflection mechanism. This method assumes that such class has a default
     * constructor with no arguments.
     * </p>
     *
     * @param contributor the contributor of this executable extension
     * @param className the name of the class to be instantiated
     * @param overridenContributorName the contributor to be used, or
     * <code>null</code> if not specified
     * @return the object created, or <code>null</code>
     * @throws CoreException if there was a problem creating the executable
     * extension
     * @see IConfigurationElement#createExecutableExtension(String)
     * @see IExecutableExtension
     */
    public Object createExecutableExtension(RegistryContributor contributor, String className,
        String overridenContributorName) throws CoreException {
        Object result = null;
        Class<?> classInstance = null;
        try {
            classInstance = Class.forName(className);
        } catch (ClassNotFoundException e1) {
            String message = NLS.bind(RegistryMessages.exExt_findClassError, contributor.getActualName(), className);
            throw new CoreException(
                new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, IRegistryConstants.PLUGIN_ERROR, message, e1));
        }

        try {
            result = classInstance.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            String message
                = NLS.bind(RegistryMessages.exExt_instantiateClassError, contributor.getActualName(), className);
            throw new CoreException(
                new Status(IStatus.ERROR, RegistryMessages.OWNER_NAME, IRegistryConstants.PLUGIN_ERROR, message, e));
        }
        return result;
    }

    /**
     * Override this method to customize scheduling of an extension registry event.
     * Note that this method <strong>must</strong> make the following call to
     * actually process the event:
     *
     * <pre>
     * <code>
     * 	RegistryStrategy.processChangeEvent(listeners, deltas, registry);
     * </code>
     * </pre>
     * 
     * <p>
     * In the default implementation, the method registry events are executed in a
     * queue on a separate thread (i.e. asynchronously, sequentially).
     * </p>
     *
     * @param listeners the list of active listeners (thread safe); may not be
     * <code>null</code>
     * @param deltas the registry deltas (thread safe); may not be
     * <code>null</code>
     * @param registry the extension registry (NOT thread safe); may not be
     * <code>null</code>
     */
    public void scheduleChangeEvent(Object[] listeners, Map<String, ?> deltas, Object registry) {
        ((ExtensionRegistry) registry).scheduleChangeEvent(listeners, deltas);
    }

    /**
     * This method performs actual processing of the registry change event. It
     * should only be used by overrides of the RegistryStrategy.scheduleChangeEvent.
     * It will return <code>null</code> if an unexpected registry type was
     * encountered.
     *
     * @param listeners the list of active listeners; may not be <code>null</code>
     * @param deltas the extension registry deltas; may not be <code>null</code>
     * @param registry the extension registry; may not be <code>null</code>
     * @return status of the operation or <code>null</code>
     */
    public final static IStatus processChangeEvent(Object[] listeners, Map<String, ?> deltas, Object registry) {
        if (registry instanceof ExtensionRegistry) {
            return ((ExtensionRegistry) registry).processChangeEvent(listeners, deltas);
        }
        return null;
    }

    /**
     * Override this method to specify debug requirements to the registry. In the
     * default implementation this method returns <code>false</code> indicating that
     * debug functionality is turned off.
     * <p>
     * Note that in a general case the extension registry plug-in doesn't depend on
     * OSGI and therefore cannot use Eclipse .options files to discover debug
     * options.
     * </p>
     *
     * @return <code>true</code> if debug logging and validation should be performed
     * and <code>false</code> otherwise
     */
    public boolean debug() {
        return false;
    }

    /**
     * Returns the parser used by the registry to parse descriptions of extension
     * points and extensions. This method must not return <code>null</code>.
     *
     * @return this strategy's parser
     * @see org.eclipse.core.runtime.IExtensionRegistry#addContribution(java.io.InputStream,
     * IContributor, boolean, String, ResourceBundle, Object)
     */
    public SAXParserFactory getXMLParser() {
        if (theXMLParserFactory == null) {
            theXMLParserFactory = SAXParserFactory.newInstance();
            try {
                // force org.xml.sax.SAXParseException for any DOCTYPE:
                theXMLParserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); //$NON-NLS-1$
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return theXMLParserFactory;
    }

    /**
     * Returns the current locale for the extension registry with enabled
     * multi-language support.
     * <p>
     * The default implementation assumes that there is a single system wide locale,
     * equivalent to {@link Locale#getDefault()}.
     * </p>
     * <p>
     * The result of this method should not be retained or passed to other threads.
     * The current locale can change any time and may be different for each thread.
     * </p>
     * <p>
     * This method can be overridden by subclasses that wish to provide a way to
     * change the default locale.
     * </p>
     * <p>
     * This method is only used if multi-language support is enabled.
     * </p>
     *
     * @see IExtensionRegistry#isMultiLanguage()
     * @return the default locale
     * @since org.eclipse.equinox.registry 3.5
     */
    public String getLocale() {
        return Locale.getDefault().toString();
    }
}
