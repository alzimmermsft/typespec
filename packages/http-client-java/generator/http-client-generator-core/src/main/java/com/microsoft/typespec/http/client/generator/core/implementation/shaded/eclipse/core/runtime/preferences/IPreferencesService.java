/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.preferences;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.CoreException;

import java.io.InputStream;

/**
 * The preference service provides facilities for dealing with the default scope
 * precedence lookup order, querying the preference store for values using this
 * order, accessing the root of the preference store node hierarchy, and
 * importing/exporting preferences.
 * <p>
 * The default-default preference search look-up order as defined by the
 * platform is: project, instance, configuration, default.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @since 3.0
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPreferencesService {

    /**
     * Return the root node of the Eclipse preference hierarchy.
     *
     * @return the root of the hierarchy
     */
    IEclipsePreferences getRootNode();

    /**
     * Read from the given input stream and create a node hierarchy representing the
     * preferences and their values. The given input stream must not be
     * <code>null</code>. The result of this function is suitable for passing as an
     * argument to {@link #applyPreferences(IExportedPreferences)}.
     *
     * @param input the input stream to read from
     * @return the node hierarchy representing the stream contents
     * @throws IllegalArgumentException if the given stream is null
     * @throws CoreException if there are problems reading the
     * preferences
     */
    IExportedPreferences readPreferences(InputStream input) throws CoreException;

    /**
     * Return a list of filters which match the given tree and is a subset of the
     * given filter list. If the specified list of filters is <code>null</code>,
     * empty, or there are no matches, then return an empty list.
     *
     * @param node the tree to match against
     * @param filters the list of filters to match against
     * @return the array of matching transfers
     * @throws CoreException if there are problems during matching
     * @see IPreferenceFilter
     * @since 3.1
     */
    IPreferenceFilter[] matches(IEclipsePreferences node, IPreferenceFilter[] filters) throws CoreException;
}
