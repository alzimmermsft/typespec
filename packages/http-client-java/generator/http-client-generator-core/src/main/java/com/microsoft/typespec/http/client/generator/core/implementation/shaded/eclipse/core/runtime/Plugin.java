/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 476403, 478769, 490586
 *     Christoph Läubrich - remove reference to InternalPlatform.getDefault().log
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime.InternalPlatform;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.DefaultScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IEclipsePreferences;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IPreferencesService;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.InstanceScope;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleActivator;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.BundleContext;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkUtil;
import java.io.OutputStream;

/**
 * The abstract superclass of all plug-in runtime class implementations. A
 * plug-in subclasses this class and overrides the appropriate life cycle
 * methods in order to react to the life cycle requests automatically issued by
 * the platform. For compatibility reasons, the methods called for those life
 * cycle events vary, please see the "Constructors and life cycle methods"
 * section below.
 *
 * <p>
 * Conceptually, the plug-in runtime class represents the entire plug-in rather
 * than an implementation of any one particular extension the plug-in declares.
 * A plug-in is not required to explicitly specify a plug-in runtime class; if
 * none is specified, the plug-in will be given a default plug-in runtime object
 * that ignores all life cycle requests (it still provides access to the
 * corresponding plug-in descriptor).
 * </p>
 * <p>
 * In the case of more complex plug-ins, it may be desirable to define a
 * concrete subclass of <code>Plugin</code>. However, just subclassing
 * <code>Plugin</code> is not sufficient. The name of the class must be
 * explicitly configured in the plug-in's manifest (<code>plugin.xml</code>)
 * file with the class attribute of the <code>&lt;plugin&gt;</code> element
 * markup.
 * </p>
 * <p>
 * Instances of plug-in runtime classes are automatically created by the
 * platform in the course of plug-in activation. For compatibility reasons, the
 * constructor used to create plug-in instances varies, please see the
 * "Constructors and life cycle methods" section below.
 * </p>
 * <p>
 * The concept of bundles underlies plug-ins. However it is safe to regard
 * plug-ins and bundles as synonyms.
 * </p>
 * <p>
 * <b>Clients must never explicitly instantiate a plug-in runtime class</b>.
 * </p>
 * <p>
 * A typical implementation pattern for plug-in runtime classes is to provide a
 * static convenience method to gain access to a plug-in's runtime object. This
 * way, code in other parts of the plug-in implementation without direct access
 * to the plug-in runtime object can easily obtain a reference to it, and thence
 * to any plug-in-wide resources recorded on it. An example for Eclipse 3.0
 * follows:
 * </p>
 *
 * <pre>
 * package myplugin;
 *
 * public class MyPluginClass extends Plugin {
 *     private static MyPluginClass instance;
 *
 *     public static MyPluginClass getInstance() {
 *         return instance;
 *     }
 *
 *     public void MyPluginClass() {
 *         super();
 *         instance = this;
 *         // ... other initialization
 *     }
 *     // ... other methods
 * }
 * </pre>
 * 
 * <p>
 * In the above example, a call to <code>MyPluginClass.getInstance()</code> will
 * always return an initialized instance of <code>MyPluginClass</code>.
 * </p>
 * <p>
 * <b>Constructors and life cycle methods</b>
 * </p>
 * <p>
 * If the plugin.xml of a plug-in indicates &lt;?eclipse version="3.0"?&gt; and
 * its prerequisite list includes
 * <code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime</code>, the
 * default constructor of the plug-in class is used and
 * {@link #start(BundleContext)} and {@link #stop(BundleContext)} are called as
 * life cycle methods.
 * </p>
 * <p>
 * Since Eclipse 3.0 APIs of the Plugin class can be called only when the Plugin
 * is in an active state, i.e., after it was started up and before it is
 * shutdown. In particular, it means that Plugin APIs should not be called from
 * overrides of {@link #Plugin()}.
 * </p>
 */
public abstract class Plugin implements BundleActivator {

    /**
     * The bundle associated this plug-in
     */
    private Bundle bundle;

    private volatile IPath stateLocation;

    /**
     * The preference object for this plug-in; initially <code>null</code>
     * meaning not yet created and initialized.
     *
     * @since 2.0
     * @deprecated
     */
    @Deprecated
    private Preferences preferences = null;

    /**
     * Creates a new plug-in runtime object. This method is called by the platform
     * if this class is used as a <code>BundleActivator</code>. This method is not
     * needed/used if this plug-in requires the org.eclipse.core.runtime.compatibility plug-in.
     * Subclasses of <code>Plugin</code>
     * must call this method first in their constructors.
     *
     * The resultant instance is not managed by the runtime and
     * so should be remembered by the client (typically using a Singleton pattern).
     * <b>Clients must never explicitly call this method.</b>
     * <p>
     * Note: The class loader typically has monitors acquired during invocation of this method. It is
     * strongly recommended that this method avoid synchronized blocks or other thread locking mechanisms,
     * as this would lead to deadlock vulnerability.
     * </p>
     *
     * @since 3.0
     */
    public Plugin() {
    }

    /**
     * Returns the log for this plug-in. If no such log exists, one is created.
     * <b>Hint: </b> instead of caling this method, consider using
     * {@link ILog#of(Class)} instead that is independent from implementing a
     * {@link Plugin}
     *
     * @return the log for this plug-in
     */
    public final ILog getLog() {
        return ILog.of(getBundle());
    }

    /**
     * Returns the location in the local file system of the
     * plug-in state area for this plug-in.
     * If the plug-in state area did not exist prior to this call,
     * it is created.
     * <p>
     * The plug-in state area is a file directory within the
     * platform's metadata area where a plug-in is free to create files.
     * The content and structure of this area is defined by the plug-in,
     * and the particular plug-in is solely responsible for any files
     * it puts there. It is recommended for plug-in preference settings and
     * other configuration parameters.
     * </p>
     * 
     * @throws IllegalStateException when the system is running with no data area (-data @none),
     * or when a data area has not been set yet.
     * @return a local file system path
     * XXX Investigate the usage of a service factory (see also platform.getStateLocation)
     */
    public final IPath getStateLocation() throws IllegalStateException {
        if (stateLocation == null) {
            // cache the value to avoid repeated java.io.File.mkdirs()
            // does not matter if the value is computed twice in parallel
            stateLocation = InternalPlatform.getDefault().getStateLocation(getBundle(), true);
        }
        return stateLocation;
    }

    /**
     * Returns the preference store for this plug-in.
     * <p>
     * Note that if an error occurs reading the preference store from disk, an empty
     * preference store is quietly created, initialized with defaults, and returned.
     * </p>
     * <p>
     * Calling this method may cause the preference store to be created and
     * initialized. Subclasses which reimplement the
     * <code>initializeDefaultPluginPreferences</code> method have this opportunity
     * to initialize preference default values, just prior to processing override
     * default values imposed externally to this plug-in (specified for the product,
     * or at platform start up).
     * </p>
     * <p>
     * After settings in the preference store are changed (for example, with
     * <code>Preferences.setValue</code> or <code>setToDefault</code>),
     * <code>savePluginPreferences</code> should be called to store the changed
     * values back to disk. Otherwise the changes will be lost on plug-in shutdown.
     * </p>
     *
     * @return the preference store
     * @see #savePluginPreferences()
     * @see Preferences#setValue(String, String)
     * @see Preferences#setToDefault(String)
     * @since 2.0
     * @deprecated Replaced by {@link IEclipsePreferences}. Preferences are now
     * stored according to scopes in the {@link IPreferencesService}.
     * The return value of this method corresponds to a combination of
     * the {@link InstanceScope} and the {@link DefaultScope}. To set
     * preferences for your plug-in, use
     * <code>InstanceScope.INSTANCE.getNode(&lt;yourPluginId&gt;)</code>.
     * To set default preferences for your plug-in, use
     * <code>DefaultScope.INSTANCE.getNode(&lt;yourPluginId&gt;)</code>.
     * To lookup an integer preference value for your plug-in, use
     * <code>Platform.getPreferencesService().getInt(&lt;yourPluginId&gt;, &lt;preferenceKey&gt;, &lt;defaultValue&gt;,
     * null)</code>.
     * Similar methods exist on {@link IPreferencesService} for
     * obtaining other kinds of preference values (strings, booleans,
     * etc).
     */
    @Deprecated
    public final Preferences getPluginPreferences() {
        final Bundle bundleCopy = getBundle();
        if (preferences != null) {
            if (InternalPlatform.DEBUG_PLUGIN_PREFERENCES) {
                InternalPlatform.message("Plugin preferences already loaded for: " + bundleCopy.getSymbolicName()); //$NON-NLS-1$
            }
            return preferences;
        }

        if (InternalPlatform.DEBUG_PLUGIN_PREFERENCES) {
            InternalPlatform.message("Loading preferences for plugin: " + bundleCopy.getSymbolicName()); //$NON-NLS-1$
        }

        // Performance: isolate PreferenceForwarder into an inner class so that it mere presence
        // won't force the PreferenceForwarder class to be loaded (which triggers Preferences plugin
        // activation).
        final Preferences[] preferencesCopy = new Preferences[1];
        Runnable innerCall = () -> preferencesCopy[0]
            = new org.eclipse.core.internal.preferences.legacy.PreferenceForwarder(this, bundleCopy.getSymbolicName());

        innerCall.run();
        preferences = preferencesCopy[0];
        return preferences;
    }

    /**
     * Saves preferences settings for this plug-in. Does nothing if the preference
     * store does not need saving.
     * <p>
     * Plug-in preferences are <b>not</b> saved automatically on plug-in shutdown.
     * </p>
     *
     * @see Preferences#store(OutputStream, String)
     * @see Preferences#needsSaving()
     * @since 2.0
     * @deprecated Replaced by InstanceScope.getNode(&lt;bundleId&gt;).flush()
     */
    @Deprecated
    public final void savePluginPreferences() {
    }

    /**
     * Returns a string representation of the plug-in, suitable
     * for debugging purposes only.
     */
    @Override
    public String toString() {
        Bundle myBundle = getBundle();
        if (myBundle == null) {
            return ""; //$NON-NLS-1$
        }
        String name = myBundle.getSymbolicName();
        return name == null ? String.valueOf(myBundle.getBundleId()) : name;
    }

    /**
     * Returns the bundle associated with this plug-in.
     *
     * @return the associated bundle
     * @since 3.0
     */
    public final Bundle getBundle() {
        if (bundle != null) {
            return bundle;
        }
        return FrameworkUtil.getBundle(getClass());
    }
}
