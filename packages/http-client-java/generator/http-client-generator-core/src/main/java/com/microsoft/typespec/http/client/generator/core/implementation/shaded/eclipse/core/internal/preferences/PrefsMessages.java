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
 * IBM - Initial API and implementation
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.preferences;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.jobs.JobMessages;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.util.NLS;

// Runtime plugin message catalog
public class PrefsMessages extends NLS {
    /**
     * The unique identifier constant of this plug-in.
     */
    public static final String OWNER_NAME
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.equinox.preferences"; //$NON-NLS-1$

    private static final String BUNDLE_NAME
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.preferences.messages"; //$NON-NLS-1$

    // Preferences
    public static String preferences_loadException;
    public static String preferences_removedNode;
    public static String childrenNames2;

    public static String PreferenceMetadata_e_null_default_value;
    public static String PreferenceMetadata_e_null_description;
    public static String PreferenceMetadata_e_null_identifier;
    public static String PreferenceMetadata_e_null_name;
    public static String PreferenceMetadata_e_null_value_type;

    static {
        // load message values from bundle file
        reloadMessages();
    }

    public static void reloadMessages() {
        NLS.initializeMessages(BUNDLE_NAME, PrefsMessages.class);
    }

    /**
     * Print a debug message to the console. Pre-pend the message with the current
     * date and the name of the current thread.
     */
    public static void message(String message) {
        JobMessages.message(message);
    }
}
