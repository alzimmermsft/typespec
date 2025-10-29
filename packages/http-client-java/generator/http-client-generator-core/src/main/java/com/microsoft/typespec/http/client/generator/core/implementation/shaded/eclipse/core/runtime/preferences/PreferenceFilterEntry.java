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
 *     IBM Corporation - initial API and implementation
 *     Semion Chichelnitsky (semion@il.ibm.com) - bug 208564
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences;

/**
 * Class which represents and preference filter entry to be used during
 * preference import/export (for example).
 *
 * @since 3.1
 * @see org.eclipse.core.runtime.preferences.IPreferenceFilter
 */
public final class PreferenceFilterEntry {

    private final String key;

    /**
     * Constructor for the class. Create a new preference filter entry with the
     * given key. The key must <em>not</em> be <code>null</code> or empty.
     *
     * @param key the name of the preference key
     */
    public PreferenceFilterEntry(String key) {
        super();
        if (key == null || key.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.key = key;
    }

    /**
     * Return the name of the preference key for this filter entry. It will
     * <em>not</em> return <code>null</code> or the empty string.
     *
     * @return the name of the preference key
     */
    public String getKey() {
        return key;
    }

}
