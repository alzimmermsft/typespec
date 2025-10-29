/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
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

package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.loader;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.osgi.internal.framework.EquinoxConfiguration;

public class EquinoxClassLoader extends ModuleClassLoader {
    static {
        try {
            ClassLoader.registerAsParallelCapable();
        } catch (Throwable t) {
            // ignore all exceptions; substrate native image fails here
        }
    }
    private final EquinoxConfiguration configuration;

    /**
     * Constructs a new DefaultClassLoader.
     * 
     * @param parent the parent classloader
     * @param configuration the equinox configuration
     */
    public EquinoxClassLoader(ClassLoader parent, EquinoxConfiguration configuration) {
        super(parent);
        this.configuration = configuration;
    }

    @Override
    protected final EquinoxConfiguration getConfiguration() {
        return configuration;
    }
}
