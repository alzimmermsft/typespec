/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
package com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.expressions;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IAdaptable;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.eclipse.core.runtime.IAdapterManager;
import java.util.Iterator;

/**
 * Objects that are adaptable to <code>IIterable</code> can be used
 * as the default variable in an iterate expression.
 * 
 * @param <T> the type of elements from this iterator
 *
 * @see IAdaptable
 * @see IAdapterManager
 *
 * @since 3.3
 */
public interface IIterable<T> {

    /**
     * Returns an iterator to iterate over the elements.
     *
     * @return an iterator
     */
    Iterator<T> iterator();
}
