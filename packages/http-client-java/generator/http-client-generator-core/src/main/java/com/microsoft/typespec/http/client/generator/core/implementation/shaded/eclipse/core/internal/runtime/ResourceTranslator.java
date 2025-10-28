/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.runtime;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.ManifestElement;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * This class can only be used if OSGi plugin is available.
 */
public class ResourceTranslator {

    public static ResourceBundle getResourceBundle(Bundle bundle) throws MissingResourceException {
        return getResourceBundle(bundle, null);
    }

    private static ResourceBundle getResourceBundle(Bundle bundle, String language) throws MissingResourceException {
        if (hasRuntime21(bundle)) {
            Locale locale = (language == null) ? Locale.getDefault() : new Locale(language);
            return ResourceBundle.getBundle("plugin", locale, createTempClassloader(bundle)); //$NON-NLS-1$
        }
        return Activator.getLocalization(bundle, language);
    }

    private static boolean hasRuntime21(Bundle b) {
        try {
            ManifestElement[] prereqs
                = ManifestElement.parseHeader(Constants.REQUIRE_BUNDLE, b.getHeaders("").get(Constants.REQUIRE_BUNDLE)); //$NON-NLS-1$
            if (prereqs == null) {
                return false;
            }
            for (ManifestElement prereq : prereqs) {
                if ("2.1".equals(prereq.getAttribute(Constants.BUNDLE_VERSION_ATTRIBUTE)) //$NON-NLS-1$
                    && "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime" //$NON-NLS-1$
                        .equals(prereq.getValue())) {
                    return true;
                }
            }
        } catch (BundleException e) {
            return false;
        }
        return false;
    }

    private static ClassLoader createTempClassloader(Bundle b) {
        ArrayList<URL> classpath = new ArrayList<>();
        addClasspathEntries(b, classpath);
        addBundleRoot(b, classpath);
        addFragments(b, classpath);
        URL[] urls = new URL[classpath.size()];
        return new URLClassLoader(classpath.toArray(urls));
    }

    private static void addFragments(Bundle host, ArrayList<URL> classpath) {
        Activator activator = Activator.getDefault();
        if (activator == null) {
            return;
        }
        Bundle[] fragments = activator.getFragments(host);
        if (fragments == null) {
            return;
        }

        for (Bundle fragment : fragments) {
            addClasspathEntries(fragment, classpath);
        }
    }

    private static void addClasspathEntries(Bundle b, ArrayList<URL> classpath) {
        ManifestElement[] classpathElements;
        try {
            classpathElements = ManifestElement.parseHeader(Constants.BUNDLE_CLASSPATH,
                b.getHeaders("").get(Constants.BUNDLE_CLASSPATH)); //$NON-NLS-1$
            if (classpathElements == null) {
                return;
            }
            for (ManifestElement classpathElement : classpathElements) {
                URL classpathEntry = b.getEntry(classpathElement.getValue());
                if (classpathEntry != null) {
                    classpath.add(classpathEntry);
                }
            }
        } catch (BundleException e) {
            // ignore
        }
    }

    private static void addBundleRoot(Bundle b, ArrayList<URL> classpath) {
        classpath.add(b.getEntry("/")); //$NON-NLS-1$
    }

}
