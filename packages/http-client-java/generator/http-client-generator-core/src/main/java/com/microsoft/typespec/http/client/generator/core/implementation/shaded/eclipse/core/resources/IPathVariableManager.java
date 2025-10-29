/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Project Path Variable Support
 *******************************************************************************/

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.*;
import java.net.URI;

/**
 * Manages a collection of path variables and resolves paths containing a
 * variable reference.
 * <p>
 * A path variable is a pair of non-null elements (name,value) where name is
 * a case-sensitive string (containing only letters, digits and the underscore
 * character, and not starting with a digit), and value is an absolute
 * <code>IPath</code> object.
 * </p>
 * <p>
 * Path variables allow for the creation of relative paths whose exact
 * location in the file system depends on the value of a variable. A variable
 * reference may only appear as the first segment of a relative path.
 * </p>
 *
 * @see org.eclipse.core.runtime.IPath
 * @since 2.1
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IPathVariableManager {

    /**
     * Returns the value of the path variable with the given name. If there is
     * no variable defined with the given name, returns <code>null</code>.
     *
     * @param name the name of the variable to return the value for
     * @return the value for the variable, or <code>null</code> if there is no
     * variable defined with the given name
     * @deprecated use {@link #getURIValue(String)} instead.
     */
    @Deprecated
    IPath getValue(String name);

    /**
     * Returns the value of the path variable with the given name. If there is
     * no variable defined with the given name, returns <code>null</code>.
     *
     * @param name the name of the variable to return the value for
     * @return the value for the variable, or <code>null</code> if there is no
     * variable defined with the given name
     * @since 3.6
     */
    URI getURIValue(String name);

    // Should be added for 3.6
    // public String[] getPathVariableNames(String name);

    /**
     * Resolves a relative <code>URI</code> object potentially containing a
     * variable reference as its first segment, replacing the variable reference
     * (if any) with the variable's value (which is a concrete absolute URI).
     * If the given URI is absolute or has a non- <code>null</code> device then
     * no variable substitution is done and that URI is returned as is. If the
     * given URI is relative and has a <code>null</code> device, but the first
     * segment does not correspond to a defined variable, then the URI is
     * returned as is.
     * <p>
     * If the given URI is <code>null</code> then <code>null</code> will be
     * returned. In all other cases the result will be non-<code>null</code>.
     * </p>
     *
     * @param uri the URI to be resolved
     * @return the resolved URI or <code>null</code>
     * @since 3.2
     */
    URI resolveURI(URI uri);

    /**
     * Resolves a relative <code>IPath</code> object potentially containing a
     * variable reference as its first segment, replacing the variable reference
     * (if any) with the variable's value (which is a concrete absolute path).
     * If the given path is absolute or has a non- <code>null</code> device then
     * no variable substitution is done and that path is returned as is. If the
     * given path is relative and has a <code>null</code> device, but the first
     * segment does not correspond to a defined variable, then the path is
     * returned as is.
     * <p>
     * If the given path is <code>null</code> then <code>null</code> will be
     * returned. In all other cases the result will be non-<code>null</code>.
     * </p>
     *
     * <p>
     * For example, consider the following collection of path variables:
     * </p>
     * <ul>
     * <li>TEMP = c:/temp</li>
     * <li>BACKUP = /tmp/backup</li>
     * </ul>
     * <p>The following paths would be resolved as:
     * <p>c:/bin =&gt; c:/bin</p>
     * <p>c:TEMP =&gt; c:TEMP</p>
     * <p>/TEMP =&gt; /TEMP</p>
     * <p>TEMP =&gt; c:/temp</p>
     * <p>TEMP/foo =&gt; c:/temp/foo</p>
     * <p>BACKUP =&gt; /tmp/backup</p>
     * <p>BACKUP/bar.txt =&gt; /tmp/backup/bar.txt</p>
     * <p>SOMEPATH/foo =&gt; SOMEPATH/foo</p>
     *
     * @param path the path to be resolved
     * @return the resolved path or <code>null</code>
     * @deprecated use {@link #resolveURI(URI)} instead.
     */
    @Deprecated
    IPath resolvePath(IPath path);

    /**
     * Validates the given path as the value for a path variable. A path
     * variable value must be a valid path that is absolute.
     *
     * @param path a possibly valid path variable value
     * @return a status object with code <code>IStatus.OK</code> if the given
     * path is a valid path variable value, otherwise a status object indicating
     * what is wrong with the value
     * @see IPath#isValidPath(String)
     * @see IStatus#OK
     */
    IStatus validateValue(IPath path);

}
