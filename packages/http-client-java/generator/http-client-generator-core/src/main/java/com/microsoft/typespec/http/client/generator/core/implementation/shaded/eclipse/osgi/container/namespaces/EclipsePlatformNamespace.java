/*******************************************************************************
 * Copyright (c) 2012, 2013 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.container.namespaces;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.resource.Namespace;

/**
 * Eclipse Platform and Requirement Namespace.
 *
 * <p>
 * This class defines the names for the attributes and directives for this
 * namespace. All unspecified capability attributes are of type {@code String}
 * and are used as arbitrary matching attributes for the capability. The values
 * associated with the specified directive and attribute keys are of type
 * {@code String}, unless otherwise indicated.
 *
 * @Immutable
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 3.10
 */
public class EclipsePlatformNamespace extends Namespace {

    /**
     * Namespace name for the eclipse platform. Unlike typical name spaces this
     * namespace is not intended to be used as an attribute.
     */
    public static final String ECLIPSE_PLATFORM_NAMESPACE = "eclipse.platform"; //$NON-NLS-1$

}
