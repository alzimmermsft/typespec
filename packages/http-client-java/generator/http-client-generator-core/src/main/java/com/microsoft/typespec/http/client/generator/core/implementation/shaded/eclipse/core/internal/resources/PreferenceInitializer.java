/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
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
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Ingo Mohr - Issue #166 - Add Preference to Turn Off Warning-Check for Project Specific Encoding
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.resources;

/**
 * @since 3.1
 */
public class PreferenceInitializer {

    // internal preference keys
    public static final String PREF_OPERATIONS_PER_SNAPSHOT = "snapshots.operations"; //$NON-NLS-1$
    public static final String PREF_DELTA_EXPIRATION = "delta.expiration"; //$NON-NLS-1$

    // DEFAULTS
    public static final boolean PREF_AUTO_BUILDING_DEFAULT = true;
    public static final int PREF_MAX_BUILD_ITERATIONS_DEFAULT = 10;
    public final static long PREF_SNAPSHOT_INTERVAL_DEFAULT = 5 * 60 * 1000L; // 5 min
    public static final int PREF_OPERATIONS_PER_SNAPSHOT_DEFAULT = 100;
    public static final boolean PREF_APPLY_FILE_STATE_POLICY_DEFAULT = true;
    public static final long PREF_FILE_STATE_LONGEVITY_DEFAULT = 7 * 24 * 3600 * 1000L; // 7 days
    public static final long PREF_MAX_FILE_STATE_SIZE_DEFAULT = 1024 * 1024L; // 1 MB
    public static final boolean PREF_KEEP_DERIVED_STATE_DEFAULT = false;
    public static final int PREF_MAX_FILE_STATES_DEFAULT = 50;
    public static final long PREF_DELTA_EXPIRATION_DEFAULT = 30 * 24 * 3600 * 1000L; // 30 days

    /**
     * @since 3.13
     */
    public static final int PREF_MAX_CONCURRENT_BUILDS_DEFAULT = 1;

    public PreferenceInitializer() {
        super();
    }

}
