/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.registry;

public interface IRegistryConstants {

    /**
     * The unique identifier constant (value
     * "<code>com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime</code>") of
     * the Core Runtime (pseudo-)
     * plug-in.
     */
    String RUNTIME_NAME
        = "com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime"; //$NON-NLS-1$
    String PROP_DEFAULT_REGISTRY = "eclipse.createRegistry"; //$NON-NLS-1$

    /**
     * Specific error code supplied to the Status objects
     */
    int PLUGIN_ERROR = 1;
}
