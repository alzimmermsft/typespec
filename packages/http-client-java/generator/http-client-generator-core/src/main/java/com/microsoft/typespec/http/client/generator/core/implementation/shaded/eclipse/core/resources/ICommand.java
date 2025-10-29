/*******************************************************************************
 *  Copyright (c) 2000, 2010 IBM Corporation and others.
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
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.resources;

import java.util.Map;

/**
 * A builder command names a builder and supplies a table of
 * name-value argument pairs.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICommand {

    /**
     * Returns a table of the arguments for this command, or <code>null</code>
     * if there are no arguments. The argument names and values are both strings.
     *
     * @return a table of command arguments (key type : <code>String</code>
     * value type : <code>String</code>), or <code>null</code>
     * @see #setArguments(Map)
     */
    Map<String, String> getArguments();

    /**
     * Returns the name of the builder to run for this command, or
     * <code>null</code> if the name has not been set.
     *
     * @return the name of the builder, or <code>null</code> if not set
     */
    String getBuilderName();

    /**
     * Sets this command's arguments to be the given table of name-values
     * pairs, or to <code>null</code> if there are no arguments. The argument
     * names and values are both strings.
     * <p>
     * Individual builders specify their argument expectations.
     * </p>
     *
     * @param args a table of command arguments (keys and values must
     * both be of type <code>String</code>), or <code>null</code>
     * @see #getArguments()
     */
    void setArguments(Map<String, String> args);

}
