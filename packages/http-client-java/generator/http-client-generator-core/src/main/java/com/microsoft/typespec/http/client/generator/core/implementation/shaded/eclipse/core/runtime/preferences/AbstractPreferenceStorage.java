/*******************************************************************************
 * Copyright (c) 2011, 2015 IBM Corporation and others.
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

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.service.prefs.BackingStoreException;

import java.util.Properties;

/**
 * Abstract class which can be used to help provide an alternate storage
 * mechanism for Eclipse preferences. Clients can over-ride this class and
 * implement the appropriate methods to read/persist preferences.
 *
 * @since 3.5
 */
public abstract class AbstractPreferenceStorage {

    /**
     * Return a <code>java.util.Properties</code> object containing the preference
     * key/value pairs for the preference node with the specified path, and its
     * children.
     * <p>
     * The table keys consist of an optional child node path and separator, followed
     * by the property key. The table values are the values of the properties.
     * </p>
     *
     * <pre>
     *     [childNodePath/]propertyKey=propertyValue
     * </pre>
     * 
     * <p>
     * Note: Whether they are absolute or relative, the paths in the returned
     * Properties object are always interpreted as relative to the node specified by
     * nodePath.
     * </p>
     *
     * @param nodePath the absolute path of the preference node
     * @return a <code>java.util.Properties</code> object or <code>null</code>
     * @throws BackingStoreException if there was a problem loading the properties
     */
    public abstract Properties load(String nodePath) throws BackingStoreException;

    /**
     * Save the given <code>java.util.Properties</code> object which represents
     * preference key/value pairs for the preference node represented by the given
     * path.
     * <p>
     * Clients are reminded that if the given properties object is empty then the
     * preference node has been removed and they should react accordingly (e.g. for
     * instance by removing the file on disk)
     * </p>
     *
     * @param nodePath the absolute path of the preference node
     * @param properties the <code>java.util.Properties</code> object to store
     * @throws BackingStoreException if there was a problem saving the properties
     */
    public abstract void save(String nodePath, Properties properties) throws BackingStoreException;

    /**
     * Return a string array containing the names of the children for the node with
     * the given path. If there are no children then an empty array is returned. One
     * example where this method is commonly called, is at the scope root when
     * discovering the initial children.
     *
     * @param nodePath the path for the preference node
     * @return the array of children names
     * @throws BackingStoreException if there was a problem retrieving the child
     * names
     */
    public abstract String[] childrenNames(String nodePath) throws BackingStoreException;

    /**
     * Callback to inform the client that the preference node with the specified
     * path has been deleted and the client should react accordingly and make the
     * appropriate changes to the storage. (e.g. delete the file/information
     * associated with that node)
     *
     * @param nodePath the absolute path of the preference node
     */
    public abstract void removed(String nodePath);
}
