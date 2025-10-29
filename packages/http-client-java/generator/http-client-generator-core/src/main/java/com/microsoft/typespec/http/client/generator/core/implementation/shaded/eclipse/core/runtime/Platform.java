/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     Gunnar Wagenknecht <gunnar@wagenknecht.org> - Fix for bug 265445
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - Fix for bug 265532
 *     Lars Vogel<Lars.Vogel@vogella.com> - Bug 478768
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime.InternalPlatform;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences.IPreferencesService;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.datalocation.Location;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.service.environment.EnvironmentInfo;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.Bundle;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.FrameworkUtil;
import java.nio.charset.Charset;

/**
 * The central class of the Eclipse Platform Runtime. This class cannot
 * be instantiated or subclassed by clients; all functionality is provided
 * by static methods. Features include:
 * <ul>
 * <li>the platform registry of installed plug-ins</li>
 * <li>the platform adapter manager</li>
 * <li>the platform log</li>
 * <li>the authorization info management</li>
 * </ul>
 * <p>
 * Most users don't have to worry about Platform's lifecycle. However, if your
 * code can call methods of this class when Platform is not running, it becomes
 * necessary to check {@link #isRunning()} before making the call. A runtime
 * exception might be thrown or incorrect result might be returned if a method
 * from this class is called while Platform is not running.
 * </p>
 */
public final class Platform {

    /**
     * Convenience class to query for the current OS.
     *
     * @since 3.30
     */
    public static final class OS {
        private OS() {
            // avoid instantiation
        }

        /**
         * @param osString the identifier for the OS (use one of the constants that
         * start with <code>OS_</code> in the <code>Platform</code>
         * class).
         *
         * @return <code>true</code> if the current platform is the one specified as a
         * parameter, <code>false</code> in all other cases.
         * @see Platform#getOS()
         * @since 3.30
         */
        public static boolean is(String osString) {
            return Platform.getOS().equals(osString);
        }

        /**
         * @return <code>true</code> if the current OS is Windows
         */
        public static boolean isWindows() {
            return is(OS_WIN32);
        }

        /**
         * @return <code>true</code> if the current OS is Linux
         */
        public static boolean isLinux() {
            return is(OS_LINUX);
        }

        /**
         * @return <code>true</code> if the current OS is MacOSX
         */
        public static boolean isMac() {
            return is(OS_MACOSX);
        }

    }

    /**
     * The unique identifier constant (value
     * "<code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime</code>")
     * of the Core Runtime (pseudo-) plug-in.
     */
    public static final String PI_RUNTIME
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime"; //$NON-NLS-1$

    /**
     * Constant (value "line.separator") name of the preference used for storing
     * the line separator.
     *
     * @since 3.1
     */
    public static final String PREF_LINE_SEPARATOR = "line.separator"; //$NON-NLS-1$

    /**
     * Status code constant (value 2) indicating an error occurred while running a plug-in.
     */
    public static final int PLUGIN_ERROR = 2;

    /**
     * Constant string (value {@code win32}) indicating the platform is running on a
     * Window 32-bit operating system (e.g., Windows 98, NT, 2000).
     * <p>
     * Note this constant has been moved from the deprecated
     * org.eclipse.core.boot.BootLoader class and its value has not changed.
     * </p>
     *
     * @since 3.0
     */
    public static final String OS_WIN32 = "win32";//$NON-NLS-1$

    /**
     * Constant string (value {@code linux} indicating the platform is running on a
     * Linux-based operating system.
     * <p>
     * Note this constant has been moved from the deprecated
     * org.eclipse.core.boot.BootLoader class and its value has not changed.
     * </p>
     *
     * @since 3.0
     */
    public static final String OS_LINUX = "linux";//$NON-NLS-1$

    /**
     * Constant string (value {@code macosx}) indicating the platform is running on
     * a Mac OS X operating system.
     * <p>
     * Note this constant has been moved from the deprecated
     * org.eclipse.core.boot.BootLoader class and its value has not changed.
     * </p>
     *
     * @since 3.0
     */
    public static final String OS_MACOSX = "macosx";//$NON-NLS-1$

    private static final Charset SYSTEM_CHARSET;

    static {
        Charset result = null;
        try {
            // JEP 400: Java 17+ populates this system property.
            String encoding = System.getProperty("native.encoding"); //$NON-NLS-1$
            if (encoding != null && !encoding.isBlank()) {
                result = Charset.forName(encoding);
            } else {
                // JVM internal property, works on older JVM's too
                encoding = System.getProperty("sun.jnu.encoding"); //$NON-NLS-1$
                if (encoding != null && !encoding.isBlank()) {
                    result = Charset.forName(encoding);
                }
            }
        } catch (Exception e) {
            // We have no log at this moment, so just print to std error
            e.printStackTrace();
        }
        if (result == null) {
            // This is always UTF-8 on Java >= 18.
            result = Charset.defaultCharset();
        }
        SYSTEM_CHARSET = result;
    }

    /**
     * Private constructor to block instance creation.
     */
    private Platform() {
        super();
    }

    /**
     * Adds the given log listener to the notification list of the platform.
     * <p>
     * Once registered, a listener starts receiving notification as entries
     * are added to plug-in logs via <code>ILog.log()</code>. The listener continues to
     * receive notifications until it is replaced or removed.
     * </p>
     *
     * @param listener the listener to register
     * @see ILog#addLogListener(ILogListener)
     * @see #removeLogListener(ILogListener)
     */
    public static void addLogListener(ILogListener listener) {
        InternalPlatform.getDefault().addLogListener(listener);
    }

    /**
     * Returns the location of the platform working directory.
     * <p>
     * Callers of this method should consider using <code>getInstanceLocation</code>
     * instead. In various, typically non IDE-related configurations of Eclipse, the platform
     * working directory may not be on the local file system. As such, the more general
     * form of this location is as a URL.
     * </p><p>
     * Alternatively, instead of calling <code>getInstanceLocation</code> clients are
     * able to acquire the {@link Location} service (with the type {@link Location#INSTANCE_FILTER})
     * and then change the resulting URL to a path. See the javadoc for <code>getInstanceLocation</code>
     * for more details.
     * </p>
     * 
     * @return the location of the platform
     */
    public static IPath getLocation() throws IllegalStateException {
        return InternalPlatform.getDefault().getLocation();
    }

    /**
     * Removes the indicated (identical) log listener from the notification list
     * of the platform. If no such listener exists, no action is taken.
     *
     * @param listener the listener to de-register
     * @see ILog#removeLogListener(ILogListener)
     * @see #addLogListener(ILogListener)
     */
    public static void removeLogListener(ILogListener listener) {
        InternalPlatform.getDefault().removeLogListener(listener);
    }

    /**
     * Returns the extension registry for this platform.
     * <p>
     * Note this method is purely a convenience and {@link RegistryFactory#getRegistry()}
     * should generally be used instead.
     * </p>
     * 
     * @return the extension registry
     * @see IExtensionRegistry
     * @since 3.0
     */
    public static IExtensionRegistry getExtensionRegistry() {
        return RegistryFactory.getRegistry();
    }

    /**
     * Returns the log for the given bundle. If no such log exists, one is created.
     *
     * @param bundle the bundle whose log is returned
     * @return the log for the given bundle
     * @since 3.0
     */
    public static ILog getLog(Bundle bundle) {
        return ILog.of(bundle);
    }

    /**
     * Returns the log for the bundle of the given class. If no such log exists, one
     * is created.
     *
     * @param clazz the class in a bundle whose log is returned
     * @return the log for the bundle to which the bundle belongs
     *
     * @since 3.16
     */
    public static ILog getLog(Class<?> clazz) {
        return ILog.of(clazz);
    }

    /**
     * Returns the string name of the current system architecture.
     * The value is a user-defined string if the architecture is
     * specified on the command line, otherwise it is the value
     * returned by <code>java.lang.System.getProperty("os.arch")</code>.
     * <p>
     * Clients are also able to acquire the {@link EnvironmentInfo} service and query it for
     * the operating-system architecture.
     * </p>
     * 
     * @return the string name of the current system architecture
     * @since 3.0
     */
    public static String getOSArch() {
        return InternalPlatform.getDefault().getOSArch();
    }

    /**
     * Returns the string name of the current operating system for use in finding
     * files whose path starts with <code>$os$</code>. <code>OS_UNKNOWN</code> is
     * returned if the operating system cannot be determined.
     * The value may indicate one of the operating systems known to the platform
     * (as specified in <code>knownOSValues</code>) or a user-defined string if
     * the operating system name is specified on the command line.
     * <p>
     * Clients are also able to acquire the {@link EnvironmentInfo} service and query it for
     * the operating-system.
     * </p>
     * 
     * @return the string name of the current operating system
     * @since 3.0
     */
    public static String getOS() {
        return InternalPlatform.getDefault().getOS();
    }

    /**
     * Return the interface into the preference mechanism. The returned
     * object can be used for such operations as searching for preference
     * values across multiple scopes and preference import/export.
     * <p>
     * Clients are also able to acquire the {@link IPreferencesService} service via
     * OSGi mechanisms and use it for preference functions.
     * </p>
     * 
     * @return an object to interface into the preference mechanism
     * @since 3.0
     */
    public static IPreferencesService getPreferencesService() {
        return null;
    }

    /**
     * Returns the resolved bundle with the specified symbolic name that has the
     * highest version. If no resolved bundles are installed that have the specified
     * symbolic name then null is returned.
     * <p>
     * Clients are also able to acquire the
     * {@link com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.packageadmin.PackageAdmin}
     * service and query it for
     * the bundle with the specified symbolic name. Clients can ask the service for
     * all bundles with that particular name and then determine the one with the
     * highest version. Note that clients may want to filter the results based on
     * the state of the bundles.
     * </p>
     * <p>
     * Note that looking up a Bundle by its symbolic name is less efficient than
     * looking up a Bundle by a class loaded by that bundle. Callers should consider
     * whether or not it is more appropriate to use
     * {@link FrameworkUtil#getBundle(Class)} instead.
     * </p>
     * <p>
     * Note also that if the purpose of looking up the Bundle in order to log a
     * message, then it would be more appropriate to use the direct
     * {@link #getLog(Class)} instead
     * </p>
     *
     * @param symbolicName the symbolic name of the bundle to be returned.
     * @return the bundle that has the specified symbolic name with the highest
     * version, or <code>null</code> if no bundle is found.
     * @since 3.0
     */
    public static Bundle getBundle(String symbolicName) {
        return InternalPlatform.getDefault().getBundle(symbolicName);
    }

    /**
     * Retrieves the system encoding ({@link Charset}) based on the locale set in
     * the current user environment.
     * <p>
     * Note: the return value is <b>not</b> influenced by the
     * {@code -Dfile.encoding} system property and is <b>not</b> meant to be used
     * for file encoding in general (there is a workspace specific
     * <code>IContainer.getDefaultCharset()</code> API for that).
     * <p>
     * This method should be used if the <b>original</b> system encoding is required
     * (which is not necessarily the encoding used by JVM to save files). It can for
     * example be used to properly encode <a href=
     * "https://docs.oracle.com/en/java/javase/18/docs/specs/man/java.html#java-command-line-argument-files">Java
     * Command-Line Argument Files</a> or to encode other platform specific data.
     *
     * @return system encoding, never null. In case the detection fails, returns
     * {@link Charset#defaultCharset()}, which is always {@code UTF-8} on
     * Java 18 and later.
     * @see <a href="https://openjdk.java.net/jeps/400">JEP 400</a>
     * @since 3.26
     */
    public static Charset getSystemCharset() {
        return SYSTEM_CHARSET;
    }
}
