/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime;

/**
 * The extension registry holds the master list of all discovered namespaces,
 * extension points and extensions.
 * <p>
 * The extension registry can be queried, by name, for extension points and
 * extensions.
 * </p>
 * <p>
 * The various objects that describe the contents of the extension registry
 * ({@link IExtensionPoint}, {@link IExtension}, and
 * {@link IConfigurationElement}) are intended for relatively short-term use.
 * Clients that deal with these objects must be aware that they may become
 * invalid if the declaring plug-in is updated or uninstalled. If this happens,
 * all methods on these object except <code>isValid()</code> will throw
 * {@link InvalidRegistryObjectException}. Code in a
 * plug-in that has declared that it is not dynamic aware (or not declared
 * anything) can safely ignore this issue, since the registry would not be
 * modified while it is active. However, code in a plug-in that declares that it
 * is dynamic aware must be careful if it accesses extension registry objects,
 * because it's at risk if plug-in are removed. Similarly, tools that analyze or
 * display the extension registry are vulnerable. Client code can pre-test for
 * invalid objects by calling <code>isValid()</code>, which never throws this
 * exception. However, pre-tests are usually not sufficient because of the
 * possibility of the extension registry object becoming invalid as a result of
 * a concurrent activity. At-risk clients must treat
 * <code>InvalidRegistryObjectException</code> as if it were a checked
 * exception. Also, such clients should probably register a listener with the
 * extension registry so that they receive notification of any changes to the
 * registry.
 * </p>
 * <p>
 * Extensions and extension points are declared by generic entities called
 * <cite>namespaces</cite>. The only fact known about namespaces is that they
 * have unique string-based identifiers. One example of a namespace is a
 * plug-in, for which the namespace id is the plug-in id.
 * </p>
 * <p>
 * This interface can be used without OSGi running.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IExtensionRegistry {

    /**
     * Returns all configuration elements from all extensions configured into the
     * identified extension point. Returns an empty array if the extension point
     * does not exist, has no extensions configured, or none of the extensions
     * contain configuration elements.
     *
     * @param namespace the namespace for the extension point (e.g.
     * <code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources"</code>)
     * @param extensionPointName the simple identifier of the extension point (e.g.
     * <code>"builders"</code>)
     * @return the configuration elements
     */
    IConfigurationElement[] getConfigurationElementsFor(String namespace, String extensionPointName);

    /**
     * Returns the extension point in this extension registry with the given
     * namespace and extension point simple identifier, or <code>null</code> if
     * there is no such extension point.
     *
     * @param namespace the namespace for the given extension point (e.g.
     * <code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources"</code>)
     * @param extensionPointName the simple identifier of the extension point (e.g.
     * <code>"builders"</code>)
     * @return the extension point, or <code>null</code>
     */
    IExtensionPoint getExtensionPoint(String namespace, String extensionPointName);

    /**
     * Returns all extensions declared in the given namespace. Returns an empty
     * array if no extensions are declared in the namespace.
     *
     * @param namespace the namespace for the extensions (e.g.
     * <code>"com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources"</code>)
     * @return the extensions in this registry declared in the given namespace
     */
    IExtension[] getExtensions(String namespace);

    /**
     * Returns all namespaces currently used by extensions and extension points in
     * this registry. Returns an empty array if there are no known
     * extensions/extension points in this registry.
     * <p>
     * The fully-qualified name of an extension point or an extension consist of a
     * namespace and a simple name (much like a qualified Java class name consist of
     * a package name and a class name). The simple names are presumed to be unique
     * in the namespace.
     * </p>
     *
     * @return all namespaces known to this registry
     */
    String[] getNamespaces();

}
