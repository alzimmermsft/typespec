/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.internal.jobs;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.osgi.framework.*;

/**
 * The Jobs plugin class.
 */
public class JobActivator implements BundleActivator {

    // $NON-NLS-1$

    /**
     * The bundle associated this plug-in
     */
    private static BundleContext bundleContext;

    static BundleContext getContext() {
        return bundleContext;
    }

}
