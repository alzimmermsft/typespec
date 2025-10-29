/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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

/**
 * A factory method for the creation of the registry objects.
 */
public class RegistryObjectFactory {

    // The extension registry that this element factory works in
    protected ExtensionRegistry registry;

    public RegistryObjectFactory(ExtensionRegistry registry) {
        this.registry = registry;
    }

}
