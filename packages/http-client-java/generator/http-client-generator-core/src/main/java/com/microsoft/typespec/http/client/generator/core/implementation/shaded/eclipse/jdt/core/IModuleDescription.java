/*******************************************************************************
 * Copyright (c) 2017, 2019 IBM Corporation.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.jdt.core;

/**
 * Represents a Java module descriptor. The module description could either come from source or binary.
 * A simple module looks like the following:
 * 
 * <pre>
 * module my.module {
 *     exports my.pack1;
 *     exports my.pack2;
 * 
 *     requires java.sql;
 * }
 * </pre>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.14
 */
public interface IModuleDescription extends IMember, IAnnotatable {

    /**
     * @return true if this module is a system module, else false
     * @since 3.20
     */
    default boolean isSystemModule() {
        return false;
    }
}
