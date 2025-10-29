/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Sergey Prigogin (Google) - use parameterized types (bug 442021)
 *******************************************************************************/
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime;

/**
 * An abstract superclass implementing the <code>IAdaptable</code> interface.
 * <code>getAdapter</code> invocations are directed to the platform's adapter
 * manager.
 *
 * <pre>
 *     public &lt;T&gt; T getAdapter(Class&lt;T&gt; adapter) {
 *         IAdapterManager manager = ...;//lookup the IAdapterManager service
 *         return manager.getAdapter(this, adapter);
 *     }
 * </pre>
 * 
 * <p>
 * This class can be used without OSGi running.
 * </p>
 * <p>
 * Clients may subclass.
 * </p>
 *
 */
public abstract class PlatformObject {
    /**
     * Constructs a new platform object.
     */
    public PlatformObject() {
        super();
    }
}
